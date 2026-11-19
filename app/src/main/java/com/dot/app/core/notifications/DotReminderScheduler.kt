package com.dot.app.core.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.dot.app.domain.model.NotificationReminder
import com.dot.app.domain.model.ReminderType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Programador de alarmas locales para recordatorios diarios (pastillas, periodo, síntomas).
 */
@Singleton
class DotReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager: AlarmManager
        get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleOrCancel(reminder: NotificationReminder) {
        if (reminder.isEnabled) {
            scheduleDaily(reminder)
        } else {
            cancel(reminder.type)
        }
    }

    fun scheduleDaily(reminder: NotificationReminder) {
        val pendingIntent = buildPendingIntent(
            type = reminder.type,
            discreetMode = reminder.discreetMode
        )

        val triggerCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, reminder.hour.coerceIn(0, 23))
            set(Calendar.MINUTE, reminder.minute.coerceIn(0, 59))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerCalendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancel(type: ReminderType) {
        val pendingIntent = buildPendingIntent(type = type, discreetMode = false)
        alarmManager.cancel(pendingIntent)
    }

    private fun buildPendingIntent(
        type: ReminderType,
        discreetMode: Boolean
    ): PendingIntent {
        val intent = Intent(context, DotNotificationReceiver::class.java).apply {
            action = ACTION_SHOW_REMINDER
            putExtra(EXTRA_REMINDER_TYPE, type.name)
            putExtra(EXTRA_DISCREET_MODE, discreetMode)
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_BASE + type.ordinal,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val ACTION_SHOW_REMINDER = "com.dot.app.ACTION_SHOW_REMINDER"
        const val EXTRA_REMINDER_TYPE = "extra_reminder_type"
        const val EXTRA_DISCREET_MODE = "extra_discreet_mode"
        private const val REQUEST_CODE_BASE = 5200
    }
}
