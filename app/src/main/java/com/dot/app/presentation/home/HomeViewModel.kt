package com.dot.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dot.app.domain.model.Cycle
import com.dot.app.domain.model.CyclePrediction
import com.dot.app.domain.model.DailyLog
import com.dot.app.domain.model.EmpatheticMessage
import com.dot.app.domain.model.FlowLevel
import com.dot.app.domain.model.UserProfile
import com.dot.app.domain.repository.AuthRepository
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
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val userName: String = "",
    val userProfile: UserProfile? = null,
    val cyclePrediction: CyclePrediction? = null,
    val empatheticMessage: EmpatheticMessage? = null,
    val todayLog: DailyLog? = null,
    val evaluationDate: LocalDate = LocalDate.now()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    authRepository: AuthRepository,
    private val userProfileRepository: UserProfileRepository,
    private val cycleRepository: CycleRepository,
    emergencyContraceptionRepository: EmergencyContraceptionRepository,
    private val dailyLogRepository: DailyLogRepository,
    private val calculateContextualCycleUseCase: CalculateContextualCycleUseCase,
    private val generateEmpatheticMessageUseCase: GenerateEmpatheticMessageUseCase
) : ViewModel() {

    private val today: LocalDate = LocalDate.now()

    val uiState: StateFlow<HomeUiState> = combine(
        authRepository.observeCurrentAccount(),
        userProfileRepository.observeUserProfile(),
        cycleRepository.observeAllCycles(),
        emergencyContraceptionRepository.observeAllEvents(),
        dailyLogRepository.observeLogsBetween(today.minusDays(14), today)
    ) { account, profile, cycles, emergencyEvents, logs ->
        val firstName = account?.name?.trim()?.split(" ")?.firstOrNull().orEmpty()
        if (profile == null) {
            HomeUiState(
                isLoading = false,
                userName = firstName,
                userProfile = null,
                evaluationDate = today
            )
        } else {
            val prediction = calculateContextualCycleUseCase(
                userProfile = profile,
                historicalCycles = cycles,
                recentEmergencyEvents = emergencyEvents,
                dailyLogs = logs,
                evaluationDate = today
            )
            val empatheticMessage = generateEmpatheticMessageUseCase(
                prediction = prediction,
                evaluationDate = today
            )
            val todayLog = logs.firstOrNull { it.date == today }

            HomeUiState(
                isLoading = false,
                userName = firstName,
                userProfile = profile,
                cyclePrediction = prediction,
                empatheticMessage = empatheticMessage,
                todayLog = todayLog,
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

    /**
     * Acción rápida de 1 toque desde "Hoy" para iniciar un nuevo periodo hoy
     * o marcar que el sangrado terminó hoy.
     */
    fun togglePeriodToday() {
        val state = uiState.value
        val prediction = state.cyclePrediction ?: return
        val profile = state.userProfile ?: return

        viewModelScope.launch(Dispatchers.IO) {
            val existingTodayLog = state.todayLog
            if (prediction.isCurrentlyMenstruating) {
                // Marcar fin de periodo hoy
                val updatedLog = (existingTodayLog ?: DailyLog(date = today)).copy(
                    flowLevel = FlowLevel.NONE
                )
                dailyLogRepository.upsertDailyLog(updatedLog)

                val bleedingDays = ChronoUnit.DAYS.between(prediction.referenceCycleStartDate, today)
                    .toInt()
                    .coerceIn(1, 12)
                val latestCycle = cycleRepository.getLatestCycle()
                if (latestCycle != null) {
                    cycleRepository.upsertCycle(latestCycle.copy(bleedingDurationDays = bleedingDays))
                }
                userProfileRepository.saveOrUpdateUserProfile(
                    profile.copy(
                        isCurrentlyMenstruating = false,
                        lastPeriodEndDate = today.minusDays(1).let {
                            if (it.isBefore(profile.lastPeriodStartDate)) profile.lastPeriodStartDate else it
                        }
                    )
                )
            } else {
                // Iniciar periodo hoy
                val updatedLog = (existingTodayLog ?: DailyLog(date = today)).copy(
                    flowLevel = FlowLevel.MODERATE
                )
                dailyLogRepository.upsertDailyLog(updatedLog)

                val latestCycle = cycleRepository.getLatestCycle()
                if (latestCycle != null) {
                    val daysSinceStart = ChronoUnit.DAYS.between(latestCycle.startDate, today).toInt()
                    if (daysSinceStart >= 15) {
                        cycleRepository.upsertCycle(
                            latestCycle.copy(
                                endDate = today.minusDays(1),
                                actualCycleLengthDays = daysSinceStart
                            )
                        )
                        cycleRepository.upsertCycle(
                            Cycle(
                                startDate = today,
                                bleedingDurationDays = profile.averagePeriodDuration
                            )
                        )
                    }
                } else {
                    cycleRepository.upsertCycle(
                        Cycle(startDate = today, bleedingDurationDays = profile.averagePeriodDuration)
                    )
                }

                userProfileRepository.saveOrUpdateUserProfile(
                    profile.copy(
                        lastPeriodStartDate = today,
                        lastPeriodEndDate = null,
                        isCurrentlyMenstruating = true
                    )
                )
            }
        }
    }

    /**
     * Registro rápido de pastilla anticonceptiva diaria con 1 toque desde Hoy.
     */
    fun toggleDailyPillTakenToday() {
        val existingTodayLog = uiState.value.todayLog
        viewModelScope.launch(Dispatchers.IO) {
            val base = existingTodayLog ?: DailyLog(date = today)
            dailyLogRepository.upsertDailyLog(
                base.copy(tookContraceptivePill = !base.tookContraceptivePill)
            )
        }
    }
}
