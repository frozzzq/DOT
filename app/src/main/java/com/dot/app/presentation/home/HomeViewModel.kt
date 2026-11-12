package com.dot.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dot.app.domain.model.Cycle
import com.dot.app.domain.model.CyclePrediction
import com.dot.app.domain.model.DailyLog
import com.dot.app.domain.model.EmpatheticMessage
import com.dot.app.domain.model.UserProfile
import com.dot.app.domain.repository.CycleRepository
import com.dot.app.domain.repository.DailyLogRepository
import com.dot.app.domain.repository.EmergencyContraceptionRepository
import com.dot.app.domain.repository.UserProfileRepository
import com.dot.app.domain.usecase.CalculateContextualCycleUseCase
import com.dot.app.domain.usecase.GenerateEmpatheticMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val userProfile: UserProfile? = null,
    val cyclePrediction: CyclePrediction? = null,
    val empatheticMessage: EmpatheticMessage? = null,
    val todayLog: DailyLog? = null,
    val recentLogs: List<DailyLog> = emptyList(),
    val historicalCycles: List<Cycle> = emptyList(),
    val evaluationDate: LocalDate = LocalDate.now()
)

/**
 * ViewModel para la pantalla principal (Dashboard) de Dot (RF-04, RF-05, RF-07).
 * Combina de forma reactiva el perfil, historial de ciclos, eventos de anticoncepción
 * de emergencia y registros diarios para recalcular inmediatamente la proyección
 * y el mensaje empático ante cualquier cambio en la base de datos local.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    userProfileRepository: UserProfileRepository,
    cycleRepository: CycleRepository,
    emergencyContraceptionRepository: EmergencyContraceptionRepository,
    dailyLogRepository: DailyLogRepository,
    private val calculateContextualCycleUseCase: CalculateContextualCycleUseCase,
    private val generateEmpatheticMessageUseCase: GenerateEmpatheticMessageUseCase
) : ViewModel() {

    private val today: LocalDate = LocalDate.now()

    val uiState: StateFlow<HomeUiState> = combine(
        userProfileRepository.observeUserProfile(),
        cycleRepository.observeAllCycles(),
        emergencyContraceptionRepository.observeAllEvents(),
        dailyLogRepository.observeLogsBetween(today.minusDays(14), today)
    ) { profile, cycles, emergencyEvents, logs ->
        if (profile == null) {
            HomeUiState(
                isLoading = false,
                userProfile = null,
                evaluationDate = today
            )
        } else {
            val prediction = calculateContextualCycleUseCase(
                userProfile = profile,
                historicalCycles = cycles,
                recentEmergencyEvents = emergencyEvents,
                evaluationDate = today
            )
            val empatheticMessage = generateEmpatheticMessageUseCase(
                prediction = prediction,
                evaluationDate = today
            )
            val todayLog = logs.firstOrNull { it.date == today }

            HomeUiState(
                isLoading = false,
                userProfile = profile,
                cyclePrediction = prediction,
                empatheticMessage = empatheticMessage,
                todayLog = todayLog,
                recentLogs = logs,
                historicalCycles = cycles,
                evaluationDate = today
            )
        }
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(isLoading = true)
        )
}
