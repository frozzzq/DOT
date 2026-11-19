package com.dot.app.core.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dot.app.MainActivity
import com.dot.app.domain.model.ReminderType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper encargado de crear canales de notificación y emitir avisos locales
 * con opción de Modo Discreto para proteger la privacidad en pantalla bloqueada.
 */
@Singleton
class DotNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun ensureChannelsCreated() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val pillChannel = NotificationChannel(
                CHANNEL_PILL,
                "Recordatorios de anticonceptivos",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas diarias para no olvidar tu pastilla anticonceptiva."
            }

            val cycleChannel = NotificationChannel(
                CHANNEL_CYCLE,
                "Seguimiento menstrual y síntomas",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Avisos empáticos sobre tu ventana menstrual y registro diario."
            }

            manager.createNotificationChannel(pillChannel)
            manager.createNotificationChannel(cycleChannel)
        }
    }

    fun showReminderNotification(
        type: ReminderType,
        discreetMode: Boolean = false
    ): Boolean {
        ensureChannelsCreated()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return false
        }

        val (title, body) = buildNotificationCopy(type, discreetMode)
        val channelId = if (type == ReminderType.DAILY_CONTRACEPTIVE_PILL) CHANNEL_PILL else CHANNEL_CYCLE

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            type.ordinal,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(
                if (type == ReminderType.DAILY_CONTRACEPTIVE_PILL) {
                    NotificationCompat.PRIORITY_HIGH
                } else {
                    NotificationCompat.PRIORITY_DEFAULT
                }
            )
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_BASE + type.ordinal, notification)
        return true
    }

    private fun buildNotificationCopy(
        type: ReminderType,
        discreetMode: Boolean
    ): Pair<String, String> {
        if (discreetMode) {
            return when (type) {
                ReminderType.DAILY_CONTRACEPTIVE_PILL ->
                    "Dot • Recordatorio diario" to "Es momento de completar tu rutina programada de hoy."
                ReminderType.PERIOD_UPCOMING ->
                    "Dot • Aviso de calendario" to "Revisa tu resumen de los próximos días en Dot."
                ReminderType.PERIOD_LOG_REMINDER ->
                    "Dot • Actualización pendiente" to "Tómate un momento para actualizar tu registro de hoy."
                ReminderType.DAILY_SYMPTOMS_CHECKIN ->
                    "Dot • Check-in diario" to "¿Cómo te sentiste hoy? Guarda tu nota rápida."
            }
        }

        return when (type) {
            ReminderType.DAILY_CONTRACEPTIVE_PILL ->
                "Hora de tu pastilla anticonceptiva" to "Toma tu dosis diaria a la misma hora para mantener su máxima efectividad."
            ReminderType.PERIOD_UPCOMING ->
                "Tu ventana menstrual se acerca" to "Tu periodo podría comenzar en los próximos días. Prepárate con calma."
            ReminderType.PERIOD_LOG_REMINDER ->
                "No olvides registrar tu periodo" to "Anota el inicio o fin de tu menstruación para mantener tu calendario preciso."
            ReminderType.DAILY_SYMPTOMS_CHECKIN ->
                "¿Cómo te sientes hoy?" to "Registra tus síntomas, energía o flujo en menos de un minuto."
        }
    }

    companion object {
        const val CHANNEL_PILL = "dot_pill_reminders"
        const val CHANNEL_CYCLE = "dot_cycle_reminders"
        private const val NOTIFICATION_ID_BASE = 4100
    }
}
