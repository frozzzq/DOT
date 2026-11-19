package com.dot.app.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dot.app.domain.model.ContraceptiveMethod
import com.dot.app.domain.model.Cycle
import com.dot.app.domain.model.CycleRegularity
import com.dot.app.domain.model.DailyLog
import com.dot.app.domain.model.FlowLevel
import com.dot.app.domain.model.NotificationReminder
import com.dot.app.domain.model.ReminderType
import com.dot.app.domain.model.UserProfile
import com.dot.app.domain.repository.CycleRepository
import com.dot.app.domain.repository.DailyLogRepository
import com.dot.app.domain.repository.NotificationReminderRepository
import com.dot.app.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class OnboardingUiState(
    val currentStep: Int = 0,
    val totalSteps: Int = 5,
    val lastPeriodStartDate: LocalDate = LocalDate.now().minusDays(10),
    val isStillMenstruating: Boolean = false,
    val lastPeriodEndDate: LocalDate = LocalDate.now().minusDays(6),
    val averagePeriodDuration: Int = 5,
    val averageCycleLength: Int = 28,
    val regularity: CycleRegularity = CycleRegularity.REGULAR,
    val contraceptiveMethod: ContraceptiveMethod = ContraceptiveMethod.NONE,
    val enableDailyPillReminder: Boolean = false,
    val pillReminderHour: Int = 21,
    val pillReminderMinute: Int = 0,
    val enablePeriodReminder: Boolean = true,
    val isSaving: Boolean = false,
    val isCompleted: Boolean = false,
    val validationMessage: String? = null
)

/**
 * ViewModel para el cuestionario guiado paso a paso (estilo Flo) — RF-01.
 *
 * Captura primer día de menstruación, último día (o si todavía está menstruando),
 * regularidad, duración del ciclo, método anticonceptivo y recordatorios iniciales.
 * Guarda automáticamente los días de menstruación en DailyLog y Cycle para que se
 * muestren resaltados en #B77076 dentro del calendario.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val cycleRepository: CycleRepository,
    private val dailyLogRepository: DailyLogRepository,
    private val notificationReminderRepository: NotificationReminderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun nextStep() {
        val state = _uiState.value
        if (state.currentStep == 0 && state.lastPeriodStartDate.isAfter(LocalDate.now())) {
            _uiState.update { it.copy(validationMessage = "La fecha de inicio no puede ser en el futuro.") }
            return
        }
        if (state.currentStep == 1 && !state.isStillMenstruating) {
            if (state.lastPeriodEndDate.isBefore(state.lastPeriodStartDate)) {
                _uiState.update { it.copy(validationMessage = "El último día no puede ser anterior al primer día.") }
                return
            }
            if (state.lastPeriodEndDate.isAfter(LocalDate.now())) {
                _uiState.update { it.copy(validationMessage = "El último día no puede ser posterior a hoy.") }
                return
            }
        }

        if (state.currentStep < state.totalSteps - 1) {
            _uiState.update { it.copy(currentStep = it.currentStep + 1, validationMessage = null) }
        } else {
            completeOnboarding()
        }
    }

    fun previousStep() {
        _uiState.update {
            if (it.currentStep > 0) it.copy(currentStep = it.currentStep - 1, validationMessage = null)
            else it
        }
    }

    fun onLastPeriodStartDateChanged(newDate: LocalDate) {
        val today = LocalDate.now()
        if (newDate.isAfter(today)) {
            _uiState.update { it.copy(validationMessage = "Elige hoy o una fecha pasada.") }
            return
        }

        val daysFromStartToToday = ChronoUnit.DAYS.between(newDate, today).toInt() + 1
        val autoStillBleeding = daysFromStartToToday in 1..5
        val suggestedEnd = newDate.plusDays(4).let { if (it.isAfter(today)) today else it }
        val duration = (ChronoUnit.DAYS.between(newDate, suggestedEnd).toInt() + 1)
            .coerceIn(MIN_BLEEDING_DAYS, MAX_BLEEDING_DAYS)

        _uiState.update {
            it.copy(
                lastPeriodStartDate = newDate,
                isStillMenstruating = autoStillBleeding,
                lastPeriodEndDate = suggestedEnd,
                averagePeriodDuration = duration,
                validationMessage = null
            )
        }
    }

    fun onStillMenstruatingChanged(stillMenstruating: Boolean) {
        val today = LocalDate.now()
        _uiState.update { current ->
            val start = current.lastPeriodStartDate
            if (stillMenstruating) {
                val daysSoFar = (ChronoUnit.DAYS.between(start, today).toInt() + 1)
                    .coerceIn(MIN_BLEEDING_DAYS, MAX_BLEEDING_DAYS)
                current.copy(
                    isStillMenstruating = true,
                    lastPeriodEndDate = today,
                    averagePeriodDuration = maxOf(daysSoFar, 4).coerceAtMost(MAX_BLEEDING_DAYS),
                    validationMessage = null
                )
            } else {
                val safeEnd = current.lastPeriodEndDate
                    .let { if (it.isBefore(start)) start.plusDays(3) else it }
                    .let { if (it.isAfter(today)) today else it }
                val duration = (ChronoUnit.DAYS.between(start, safeEnd).toInt() + 1)
                    .coerceIn(MIN_BLEEDING_DAYS, MAX_BLEEDING_DAYS)
                current.copy(
                    isStillMenstruating = false,
                    lastPeriodEndDate = safeEnd,
                    averagePeriodDuration = duration,
                    validationMessage = null
                )
            }
        }
    }

    fun onLastPeriodEndDateChanged(endDate: LocalDate) {
        val today = LocalDate.now()
        val start = _uiState.value.lastPeriodStartDate
        if (endDate.isBefore(start)) {
            _uiState.update { it.copy(validationMessage = "El último día debe ser igual o posterior al inicio.") }
            return
        }
        if (endDate.isAfter(today)) {
            _uiState.update { it.copy(validationMessage = "No puedes elegir una fecha futura.") }
            return
        }
        val duration = (ChronoUnit.DAYS.between(start, endDate).toInt() + 1)
            .coerceIn(MIN_BLEEDING_DAYS, MAX_BLEEDING_DAYS)
        _uiState.update {
            it.copy(
                isStillMenstruating = false,
                lastPeriodEndDate = endDate,
                averagePeriodDuration = duration,
                validationMessage = null
            )
        }
    }

    fun onBleedingDurationStepperChanged(days: Int) {
        val today = LocalDate.now()
        val coerced = days.coerceIn(MIN_BLEEDING_DAYS, MAX_BLEEDING_DAYS)
        _uiState.update { current ->
            val computedEnd = current.lastPeriodStartDate
                .plusDays((coerced - 1).toLong())
                .let { if (it.isAfter(today)) today else it }
            current.copy(
                averagePeriodDuration = coerced,
                lastPeriodEndDate = computedEnd,
                validationMessage = null
            )
        }
    }

    fun onCycleLengthChanged(days: Int) {
        _uiState.update {
            it.copy(
                averageCycleLength = days.coerceIn(MIN_CYCLE_DAYS, MAX_CYCLE_DAYS),
                validationMessage = null
            )
        }
    }

    fun onRegularitySelected(regularity: CycleRegularity) {
        _uiState.update { it.copy(regularity = regularity, validationMessage = null) }
    }

    fun onContraceptiveMethodSelected(method: ContraceptiveMethod) {
        val usesDailyPill = method == ContraceptiveMethod.COMBINED_ORAL_PILL ||
            method == ContraceptiveMethod.PROGESTIN_ONLY_PILL
        _uiState.update {
            it.copy(
                contraceptiveMethod = method,
                enableDailyPillReminder = usesDailyPill,
                validationMessage = null
            )
        }
    }

    fun onEnableDailyPillReminderChanged(enabled: Boolean) {
        _uiState.update { it.copy(enableDailyPillReminder = enabled) }
    }

    fun onPillReminderTimeChanged(hour: Int, minute: Int) {
        _uiState.update {
            it.copy(
                pillReminderHour = hour.coerceIn(0, 23),
                pillReminderMinute = minute.coerceIn(0, 59)
            )
        }
    }

    fun onEnablePeriodReminderChanged(enabled: Boolean) {
        _uiState.update { it.copy(enablePeriodReminder = enabled) }
    }

    fun completeOnboarding() {
        val currentState = _uiState.value
        val today = LocalDate.now()
        val startDate = currentState.lastPeriodStartDate
        val effectiveEndDate = if (currentState.isStillMenstruating) {
            today
        } else {
            currentState.lastPeriodEndDate.coerceInRange(startDate, today)
        }

        val recordedBleedingDays = (ChronoUnit.DAYS.between(startDate, effectiveEndDate).toInt() + 1)
            .coerceIn(MIN_BLEEDING_DAYS, MAX_BLEEDING_DAYS)
        val avgBleedingDays = if (currentState.isStillMenstruating) {
            maxOf(recordedBleedingDays, currentState.averagePeriodDuration)
                .coerceIn(MIN_BLEEDING_DAYS, MAX_BLEEDING_DAYS)
        } else {
            recordedBleedingDays
        }

        _uiState.update { it.copy(isSaving = true, validationMessage = null) }

        viewModelScope.launch(Dispatchers.IO) {
            val profile = UserProfile(
                lastPeriodStartDate = startDate,
                lastPeriodEndDate = if (currentState.isStillMenstruating) null else effectiveEndDate,
                isCurrentlyMenstruating = currentState.isStillMenstruating,
                averagePeriodDuration = avgBleedingDays,
                averageCycleLength = currentState.averageCycleLength,
                regularity = currentState.regularity,
                contraceptiveMethod = currentState.contraceptiveMethod,
                isOnboardingCompleted = true
            )
            userProfileRepository.saveOrUpdateUserProfile(profile)

            val existingLatestCycle = cycleRepository.getLatestCycle()
            if (existingLatestCycle == null) {
                cycleRepository.upsertCycle(
                    Cycle(
                        startDate = startDate,
                        bleedingDurationDays = avgBleedingDays
                    )
                )
            } else if (existingLatestCycle.startDate == startDate) {
                cycleRepository.upsertCycle(
                    existingLatestCycle.copy(bleedingDurationDays = avgBleedingDays)
                )
            }

            // Persistir cada día de sangrado del cuestionario inicial en DailyLog
            // para resaltarlo en #B77076 dentro del calendario
            val periodLogs = mutableListOf<DailyLog>()
            var cursor = startDate
            var dayIndex = 0
            while (!cursor.isAfter(effectiveEndDate) && dayIndex < MAX_BLEEDING_DAYS) {
                val flow = when {
                    dayIndex == 0 -> FlowLevel.MODERATE
                    dayIndex == 1 -> FlowLevel.HEAVY
                    cursor == effectiveEndDate && dayIndex >= 2 -> FlowLevel.LIGHT
                    else -> FlowLevel.MODERATE
                }
                periodLogs.add(DailyLog(date = cursor, flowLevel = flow))
                cursor = cursor.plusDays(1)
                dayIndex++
            }
            if (periodLogs.isNotEmpty()) {
                dailyLogRepository.upsertDailyLogs(periodLogs)
            }

            notificationReminderRepository.ensureDefaultRemindersInitialized(
                enablePillByDefault = currentState.enableDailyPillReminder
            )
            notificationReminderRepository.upsertReminder(
                NotificationReminder(
                    type = ReminderType.DAILY_CONTRACEPTIVE_PILL,
                    isEnabled = currentState.enableDailyPillReminder,
                    hour = currentState.pillReminderHour,
                    minute = currentState.pillReminderMinute,
                    discreetMode = false
                )
            )
            notificationReminderRepository.upsertReminder(
                NotificationReminder(
                    type = ReminderType.PERIOD_UPCOMING,
                    isEnabled = currentState.enablePeriodReminder,
                    hour = ReminderType.PERIOD_UPCOMING.defaultHour,
                    minute = ReminderType.PERIOD_UPCOMING.defaultMinute,
                    discreetMode = false
                )
            )
            notificationReminderRepository.upsertReminder(
                NotificationReminder(
                    type = ReminderType.PERIOD_LOG_REMINDER,
                    isEnabled = currentState.enablePeriodReminder,
                    hour = ReminderType.PERIOD_LOG_REMINDER.defaultHour,
                    minute = ReminderType.PERIOD_LOG_REMINDER.defaultMinute,
                    discreetMode = false
                )
            )

            _uiState.update { it.copy(isSaving = false, isCompleted = true) }
        }
    }

    private fun LocalDate.coerceInRange(min: LocalDate, max: LocalDate): LocalDate = when {
        this.isBefore(min) -> min
        this.isAfter(max) -> max
        else -> this
    }

    companion object {
        const val MIN_BLEEDING_DAYS = 1
        const val MAX_BLEEDING_DAYS = 12
        const val MIN_CYCLE_DAYS = 21
        const val MAX_CYCLE_DAYS = 45
    }
}
