package com.dot.app.presentation.daily_log

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dot.app.domain.model.Cycle
import com.dot.app.domain.model.DailyLog
import com.dot.app.domain.model.FlowLevel
import com.dot.app.domain.model.ProtectionMethod
import com.dot.app.domain.model.Symptom
import com.dot.app.domain.repository.CycleRepository
import com.dot.app.domain.repository.DailyLogRepository
import com.dot.app.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class DailyLogUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val isExistingRecord: Boolean = false,
    val flowLevel: FlowLevel = FlowLevel.NONE,
    val selectedSymptoms: Set<Symptom> = emptySet(),
    val hadSexualActivity: Boolean = false,
    val protectionUsed: ProtectionMethod = ProtectionMethod.NONE,
    val tookContraceptivePill: Boolean = false,
    val tookEmergencyPill: Boolean = false,
    val notes: String = "",
    val recentDates: List<LocalDate> = (0L..5L).map { LocalDate.now().minusDays(it) },
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val feedbackMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DailyLogViewModel @Inject constructor(
    private val dailyLogRepository: DailyLogRepository,
    private val cycleRepository: CycleRepository,
    private val userProfileRepository: UserProfileRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialDate: LocalDate = savedStateHandle.get<String>(ARG_DATE)
        ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        ?: LocalDate.now()

    private val selectedDateFlow = MutableStateFlow(initialDate)

    private val _uiState = MutableStateFlow(DailyLogUiState(selectedDate = initialDate))
    val uiState: StateFlow<DailyLogUiState> = _uiState.asStateFlow()

    init {
        observeLogForSelectedDate()
    }

    private fun observeLogForSelectedDate() {
        viewModelScope.launch(Dispatchers.IO) {
            selectedDateFlow
                .flatMapLatest { date -> dailyLogRepository.observeLogByDate(date) }
                .collectLatest { existingLog ->
                    val currentDate = selectedDateFlow.value
                    if (existingLog != null) {
                        _uiState.update {
                            it.copy(
                                selectedDate = currentDate,
                                isExistingRecord = true,
                                flowLevel = existingLog.flowLevel,
                                selectedSymptoms = existingLog.symptoms.toSet(),
                                hadSexualActivity = existingLog.hadSexualActivity,
                                protectionUsed = existingLog.protectionUsed,
                                tookContraceptivePill = existingLog.tookContraceptivePill,
                                tookEmergencyPill = existingLog.tookEmergencyPill,
                                notes = existingLog.notes.orEmpty()
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                selectedDate = currentDate,
                                isExistingRecord = false,
                                flowLevel = FlowLevel.NONE,
                                selectedSymptoms = emptySet(),
                                hadSexualActivity = false,
                                protectionUsed = ProtectionMethod.NONE,
                                tookContraceptivePill = false,
                                tookEmergencyPill = false,
                                notes = ""
                            )
                        }
                    }
                }
        }
    }

    fun onDateSelected(date: LocalDate) {
        if (date.isAfter(LocalDate.now())) {
            _uiState.update { it.copy(feedbackMessage = "Solo puedes registrar hasta el día de hoy.") }
            return
        }
        _uiState.update { it.copy(feedbackMessage = null) }
        selectedDateFlow.value = date
    }

    fun onFlowLevelSelected(flowLevel: FlowLevel) {
        _uiState.update { it.copy(flowLevel = flowLevel, feedbackMessage = null) }
    }

    fun onSymptomToggled(symptom: Symptom) {
        _uiState.update { current ->
            val updated = current.selectedSymptoms.toMutableSet()
            if (!updated.add(symptom)) updated.remove(symptom)
            current.copy(selectedSymptoms = updated, feedbackMessage = null)
        }
    }

    fun onQuickSexOptionSelected(hadSex: Boolean, protection: ProtectionMethod) {
        _uiState.update {
            it.copy(
                hadSexualActivity = hadSex,
                protectionUsed = if (hadSex) protection else ProtectionMethod.NONE,
                feedbackMessage = null
            )
        }
    }

    fun onTookContraceptivePillChanged(took: Boolean) {
        _uiState.update { it.copy(tookContraceptivePill = took, feedbackMessage = null) }
    }

    fun onTookEmergencyPillChanged(took: Boolean) {
        _uiState.update { it.copy(tookEmergencyPill = took, feedbackMessage = null) }
    }

    fun onNotesChanged(notes: String) {
        _uiState.update { it.copy(notes = notes, feedbackMessage = null) }
    }

    fun saveDailyLog() {
        val currentState = _uiState.value
        _uiState.update { it.copy(isSaving = true, feedbackMessage = null) }

        viewModelScope.launch(Dispatchers.IO) {
            val log = DailyLog(
                date = currentState.selectedDate,
                flowLevel = currentState.flowLevel,
                symptoms = currentState.selectedSymptoms.toList(),
                hadSexualActivity = currentState.hadSexualActivity,
                protectionUsed = if (currentState.hadSexualActivity) {
                    currentState.protectionUsed
                } else {
                    ProtectionMethod.NONE
                },
                tookContraceptivePill = currentState.tookContraceptivePill,
                tookEmergencyPill = currentState.tookEmergencyPill,
                notes = currentState.notes.trim().ifBlank { null }
            )

            dailyLogRepository.upsertDailyLog(log)

            if (currentState.flowLevel in ACTIVE_MENSTRUAL_FLOWS) {
                syncCycleStartIfNewPeriod(currentState.selectedDate)
            }

            _uiState.update {
                it.copy(
                    isSaving = false,
                    isExistingRecord = true,
                    savedSuccessfully = true,
                    feedbackMessage = "Registro guardado correctamente."
                )
            }
        }
    }

    fun deleteCurrentDateLog() {
        val dateToDelete = _uiState.value.selectedDate
        viewModelScope.launch(Dispatchers.IO) {
            dailyLogRepository.deleteDailyLog(dateToDelete)
            _uiState.update {
                it.copy(
                    isExistingRecord = false,
                    feedbackMessage = "Registro eliminado."
                )
            }
        }
    }

    private suspend fun syncCycleStartIfNewPeriod(bleedingDate: LocalDate) {
        val latestCycle = cycleRepository.getLatestCycle()
        val profile = userProfileRepository.getUserProfile()

        if (latestCycle != null) {
            val daysSinceStart = ChronoUnit.DAYS.between(latestCycle.startDate, bleedingDate).toInt()
            if (daysSinceStart >= MIN_DAYS_FOR_NEW_CYCLE) {
                cycleRepository.upsertCycle(
                    latestCycle.copy(
                        endDate = bleedingDate.minusDays(1),
                        actualCycleLengthDays = daysSinceStart
                    )
                )
                cycleRepository.upsertCycle(
                    Cycle(
                        startDate = bleedingDate,
                        bleedingDurationDays = profile?.averagePeriodDuration
                    )
                )
                if (profile != null && bleedingDate.isAfter(profile.lastPeriodStartDate)) {
                    userProfileRepository.saveOrUpdateUserProfile(
                        profile.copy(
                            lastPeriodStartDate = bleedingDate,
                            isCurrentlyMenstruating = bleedingDate == LocalDate.now()
                        )
                    )
                }
            }
        }
    }

    companion object {
        const val ARG_DATE = "date"
        private const val MIN_DAYS_FOR_NEW_CYCLE = 15

        private val ACTIVE_MENSTRUAL_FLOWS = setOf(
            FlowLevel.LIGHT,
            FlowLevel.MODERATE,
            FlowLevel.HEAVY
        )
    }
}
