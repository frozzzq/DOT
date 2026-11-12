package com.dot.app.presentation.emergency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dot.app.domain.model.CyclePrediction
import com.dot.app.domain.model.EmergencyCompoundType
import com.dot.app.domain.model.EmergencyContraceptionLog
import com.dot.app.domain.model.EmpatheticMessage
import com.dot.app.domain.repository.CycleRepository
import com.dot.app.domain.repository.EmergencyContraceptionRepository
import com.dot.app.domain.repository.UserProfileRepository
import com.dot.app.domain.usecase.CalculateContextualCycleUseCase
import com.dot.app.domain.usecase.GenerateEmpatheticMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

data class EmergencyUiState(
    val editingEventId: Long = 0L,
    val takenDate: LocalDate = LocalDate.now(),
    val includeApproximateTime: Boolean = true,
    val approximateTime: LocalTime = LocalTime.of(12, 0),
    val compoundType: EmergencyCompoundType = EmergencyCompoundType.LEVONORGESTREL,
    val priorProtectionUsed: Boolean = false,
    val hadSubsequentIntercourse: Boolean = false,
    val notes: String = "",
    val isSaving: Boolean = false,
    val showPostSaveInfoCard: Boolean = false,
    val recalculatedPrediction: CyclePrediction? = null,
    val empatheticMessage: EmpatheticMessage? = null,
    val recordedEvents: List<EmergencyContraceptionLog> = emptyList(),
    val validationMessage: String? = null
)

/**
 * ViewModel para el módulo de Anticoncepción de Emergencia (RF-03, RF-04, RF-05, RF-06, RF-08).
 * Registra o edita la ingesta de anticoncepción hormonal de emergencia, marca el ciclo
 * actual como alterado para no sesgar promedios históricos (RF-07) y dispara de inmediato
 * el recálculo contextual del ciclo y la mensajería empática.
 */
@HiltViewModel
class EmergencyViewModel @Inject constructor(
    private val emergencyContraceptionRepository: EmergencyContraceptionRepository,
    private val userProfileRepository: UserProfileRepository,
    private val cycleRepository: CycleRepository,
    private val calculateContextualCycleUseCase: CalculateContextualCycleUseCase,
    private val generateEmpatheticMessageUseCase: GenerateEmpatheticMessageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmergencyUiState())
    val uiState: StateFlow<EmergencyUiState> = _uiState.asStateFlow()

    init {
        observeRecordedEvents()
    }

    private fun observeRecordedEvents() {
        viewModelScope.launch(Dispatchers.IO) {
            emergencyContraceptionRepository.observeAllEvents().collect { events ->
                _uiState.update { it.copy(recordedEvents = events) }
            }
        }
    }

    fun onTakenDateChanged(date: LocalDate) {
        if (date.isAfter(LocalDate.now())) {
            _uiState.update {
                it.copy(validationMessage = "La fecha de la toma no puede ser posterior al día de hoy.")
            }
            return
        }
        _uiState.update {
            it.copy(
                takenDate = date,
                validationMessage = null
            )
        }
    }

    fun onIncludeTimeChanged(include: Boolean) {
        _uiState.update { it.copy(includeApproximateTime = include) }
    }

    fun onApproximateTimeChanged(hour: Int, minute: Int) {
        val safeHour = hour.coerceIn(0, 23)
        val safeMinute = minute.coerceIn(0, 59)
        _uiState.update {
            it.copy(approximateTime = LocalTime.of(safeHour, safeMinute))
        }
    }

    fun onCompoundTypeSelected(type: EmergencyCompoundType) {
        _uiState.update { it.copy(compoundType = type) }
    }

    fun onPriorProtectionChanged(used: Boolean) {
        _uiState.update { it.copy(priorProtectionUsed = used) }
    }

    fun onSubsequentIntercourseChanged(hadIntercourse: Boolean) {
        _uiState.update { it.copy(hadSubsequentIntercourse = hadIntercourse) }
    }

    fun onNotesChanged(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun selectEventForEdit(event: EmergencyContraceptionLog) {
        _uiState.update {
            it.copy(
                editingEventId = event.id,
                takenDate = event.takenDate,
                includeApproximateTime = event.approximateTime != null,
                approximateTime = event.approximateTime ?: LocalTime.of(12, 0),
                compoundType = event.compoundType,
                priorProtectionUsed = event.priorProtectionUsed,
                hadSubsequentIntercourse = event.hadSubsequentIntercourse,
                notes = event.notes.orEmpty(),
                showPostSaveInfoCard = false,
                validationMessage = null
            )
        }
    }

    fun deleteEmergencyEvent(eventId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            emergencyContraceptionRepository.deleteEvent(eventId)
            recalculateContext()
        }
    }

    fun saveEmergencyContraceptionEvent() {
        val currentState = _uiState.value

        if (currentState.takenDate.isAfter(LocalDate.now())) {
            _uiState.update {
                it.copy(validationMessage = "Por favor selecciona una fecha válida (hoy o anterior).")
            }
            return
        }

        _uiState.update { it.copy(isSaving = true, validationMessage = null) }

        viewModelScope.launch(Dispatchers.IO) {
            val event = EmergencyContraceptionLog(
                id = currentState.editingEventId,
                takenDate = currentState.takenDate,
                approximateTime = if (currentState.includeApproximateTime) currentState.approximateTime else null,
                compoundType = currentState.compoundType,
                priorProtectionUsed = currentState.priorProtectionUsed,
                hadSubsequentIntercourse = currentState.hadSubsequentIntercourse,
                notes = currentState.notes.trim().ifBlank { null }
            )

            emergencyContraceptionRepository.upsertEvent(event)

            // Marcar el ciclo actual como alterado por anticoncepción de emergencia (RF-07)
            val latestCycle = cycleRepository.getLatestCycle()
            if (latestCycle != null && !currentState.takenDate.isBefore(latestCycle.startDate)) {
                cycleRepository.upsertCycle(
                    latestCycle.copy(isAlteredByEmergencyContraception = true)
                )
            }

            recalculateContext(showCard = true)
        }
    }

    private suspend fun recalculateContext(showCard: Boolean = false) {
        val profile = userProfileRepository.getUserProfile()
        val allCycles = cycleRepository.observeAllCycles().first()
        val allEvents = emergencyContraceptionRepository.observeAllEvents().first()

        val prediction = profile?.let {
            calculateContextualCycleUseCase(
                userProfile = it,
                historicalCycles = allCycles,
                recentEmergencyEvents = allEvents,
                evaluationDate = LocalDate.now()
            )
        }
        val message = prediction?.let {
            generateEmpatheticMessageUseCase(
                prediction = it,
                evaluationDate = LocalDate.now()
            )
        }

        _uiState.update {
            it.copy(
                isSaving = false,
                editingEventId = 0L,
                showPostSaveInfoCard = showCard,
                recalculatedPrediction = prediction,
                empatheticMessage = message
            )
        }
    }
}
