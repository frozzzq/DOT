package com.dot.app.presentation.cycles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dot.app.domain.model.Cycle
import com.dot.app.domain.model.CycleHealthAnalysis
import com.dot.app.domain.model.DailyLog
import com.dot.app.domain.model.FlowLevel
import com.dot.app.domain.repository.CycleRepository
import com.dot.app.domain.repository.DailyLogRepository
import com.dot.app.domain.repository.UserProfileRepository
import com.dot.app.domain.usecase.AnalyzeCycleHealthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class CyclesUiState(
    val isLoading: Boolean = true,
    val analysis: CycleHealthAnalysis? = null,
    val showAddCycleDialog: Boolean = false
)

@HiltViewModel
class CyclesViewModel @Inject constructor(
    userProfileRepository: UserProfileRepository,
    private val cycleRepository: CycleRepository,
    private val dailyLogRepository: DailyLogRepository,
    private val analyzeCycleHealthUseCase: AnalyzeCycleHealthUseCase
) : ViewModel() {

    private val showAddDialogFlow = MutableStateFlow(false)

    val uiState: StateFlow<CyclesUiState> = combine(
        userProfileRepository.observeUserProfile(),
        cycleRepository.observeAllCycles(),
        showAddDialogFlow
    ) { profile, cycles, showDialog ->
        val analysis = analyzeCycleHealthUseCase(
            userProfile = profile,
            cycles = cycles,
            evaluationDate = LocalDate.now()
        )
        CyclesUiState(
            isLoading = false,
            analysis = analysis,
            showAddCycleDialog = showDialog
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CyclesUiState(isLoading = true)
        )

    fun onShowAddCycleDialog(show: Boolean) {
        showAddDialogFlow.value = show
    }

    /**
     * Permite agregar un ciclo pasado al historial y actualizar automáticamente
     * la línea de tiempo y los días de sangrado en el calendario (RF-07, RF-08).
     */
    fun addHistoricalCycle(
        startDate: LocalDate,
        cycleLengthDays: Int,
        bleedingDays: Int,
        alteredByEmergency: Boolean
    ) {
        val safeCycleLength = cycleLengthDays.coerceIn(15, 120)
        val safeBleeding = bleedingDays.coerceIn(1, 14)
        val endDate = startDate.plusDays((safeCycleLength - 1).toLong())
        val today = LocalDate.now()

        viewModelScope.launch(Dispatchers.IO) {
            val isClosed = endDate.isBefore(today)
            cycleRepository.upsertCycle(
                Cycle(
                    startDate = startDate,
                    endDate = if (isClosed) endDate else null,
                    bleedingDurationDays = safeBleeding,
                    actualCycleLengthDays = if (isClosed) safeCycleLength else null,
                    isAlteredByEmergencyContraception = alteredByEmergency
                )
            )

            // Marcar esos días de sangrado en DailyLogs para que también aparezcan en #B77076 en el calendario
            val logs = (0 until safeBleeding).mapNotNull { offset ->
                val day = startDate.plusDays(offset.toLong())
                if (!day.isAfter(today)) {
                    DailyLog(date = day, flowLevel = FlowLevel.MODERATE)
                } else {
                    null
                }
            }
            if (logs.isNotEmpty()) {
                dailyLogRepository.upsertDailyLogs(logs)
            }

            showAddDialogFlow.update { false }
        }
    }

    fun deleteCycle(cycleId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            cycleRepository.deleteCycle(cycleId)
        }
    }
}
