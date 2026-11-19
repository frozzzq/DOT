package com.dot.app.domain.model

/**
 * Tipos de recordatorios del módulo de notificaciones.
 */
enum class ReminderType(
    val defaultTitle: String,
    val defaultDescription: String,
    val defaultHour: Int,
    val defaultMinute: Int
) {
    DAILY_CONTRACEPTIVE_PILL(
        defaultTitle = "Pastilla anticonceptiva diaria",
        defaultDescription = "Recibe un recordatorio diario a la misma hora para no olvidar tu toma.",
        defaultHour = 21,
        defaultMinute = 0
    ),
    PERIOD_UPCOMING(
        defaultTitle = "Aviso de próximo periodo",
        defaultDescription = "Te avisamos 2 días antes de tu ventana menstrual estimada.",
        defaultHour = 9,
        defaultMinute = 0
    ),
    PERIOD_LOG_REMINDER(
        defaultTitle = "Registrar inicio o fin de periodo",
        defaultDescription = "Recordatorio suave para mantener tu calendario y predicciones al día.",
        defaultHour = 20,
        defaultMinute = 0
    ),
    DAILY_SYMPTOMS_CHECKIN(
        defaultTitle = "Registro diario de síntomas",
        defaultDescription = "Un minuto al día para anotar cómo te sientes y conocer tus patrones.",
        defaultHour = 21,
        defaultMinute = 30
    )
}

data class NotificationReminder(
    val type: ReminderType,
    val isEnabled: Boolean,
    val hour: Int,
    val minute: Int,
    val discreetMode: Boolean = false
)
