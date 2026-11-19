package com.dot.app.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dot.app.core.notifications.DotNotificationHelper
import com.dot.app.domain.model.NotificationReminder
import com.dot.app.domain.model.ReminderType
import com.dot.app.domain.repository.AuthRepository
import com.dot.app.domain.repository.NotificationReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val userName: String = "",
    val userEmail: String = "",
    val reminders: List<NotificationReminder> = emptyList(),
    val discreetMode: Boolean = false,
    val feedbackMessage: String? = null,
    val loggedOut: Boolean = false
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val reminderRepository: NotificationReminderRepository,
    private val authRepository: AuthRepository,
    private val notificationHelper: DotNotificationHelper
) : ViewModel() {

    private val transientState = MutableStateFlow(TransientUi())

    val uiState: StateFlow<NotificationsUiState> = combine(
        reminderRepository.observeReminders(),
        authRepository.observeCurrentAccount(),
        transientState
    ) { reminders, account, transient ->
        NotificationsUiState(
            userName = account?.name.orEmpty(),
            userEmail = account?.email.orEmpty(),
            reminders = reminders,
            discreetMode = reminders.firstOrNull()?.discreetMode ?: false,
            feedbackMessage = transient.feedback,
            loggedOut = transient.loggedOut
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NotificationsUiState()
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            reminderRepository.ensureDefaultRemindersInitialized()
        }
    }

    fun onToggleReminder(reminder: NotificationReminder, enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            reminderRepository.upsertReminder(reminder.copy(isEnabled = enabled))
            transientState.update {
                it.copy(
                    feedback = if (enabled) {
                        "Recordatorio activado para las ${formatTime(reminder.hour, reminder.minute)}."
                    } else {
                        "Recordatorio desactivado."
                    }
                )
            }
        }
    }

    fun onUpdateReminderHour(reminder: NotificationReminder, deltaHour: Int) {
        val nextHour = (reminder.hour + deltaHour + 24) % 24
        viewModelScope.launch(Dispatchers.IO) {
            reminderRepository.upsertReminder(reminder.copy(hour = nextHour))
        }
    }

    fun onUpdateReminderMinute(reminder: NotificationReminder, deltaMinute: Int) {
        val nextMinute = (reminder.minute + deltaMinute + 60) % 60
        viewModelScope.launch(Dispatchers.IO) {
            reminderRepository.upsertReminder(reminder.copy(minute = nextMinute))
        }
    }

    fun onToggleDiscreetMode(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            reminderRepository.setDiscreetModeForAll(enabled)
            transientState.update {
                it.copy(
                    feedback = if (enabled) {
                        "Modo discreto activado: tus alertas no mostrarán palabras sensibles."
                    } else {
                        "Modo discreto desactivado."
                    }
                )
            }
        }
    }

    fun sendTestNotification(type: ReminderType = ReminderType.DAILY_CONTRACEPTIVE_PILL) {
        val discreet = uiState.value.discreetMode
        val sent = notificationHelper.showReminderNotification(type = type, discreetMode = discreet)
        transientState.update {
            it.copy(
                feedback = if (sent) {
                    "Notificación de prueba enviada. Revisa tu barra de notificaciones."
                } else {
                    "Activa el permiso de notificaciones en tu dispositivo para recibir avisos."
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            authRepository.logout()
            transientState.update { it.copy(loggedOut = true) }
        }
    }

    private fun formatTime(hour: Int, minute: Int): String =
        String.format("%02d:%02d", hour, minute)

    private data class TransientUi(
        val feedback: String? = null,
        val loggedOut: Boolean = false
    )
}
