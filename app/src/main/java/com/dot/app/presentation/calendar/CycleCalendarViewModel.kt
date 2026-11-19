package com.dot.app.presentation.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dot.app.domain.model.DailyLog
import com.dot.app.domain.repository.CycleRepository
import com.dot.app.domain.repository.DailyLogRepository
import com.dot.app.domain.repository.EmergencyContraceptionRepository
import com.dot.app.domain.repository.UserProfileRepository
import com.dot.app.domain.usecase.CalculateContextualCycleUseCase
import com.dot.app.domain.usecase.CalendarHighlights
import com.dot.app.domain.usecase.GetCycleCalendarDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class CalendarUiState(
    val months: List<YearMonth> = emptyList(),
    val currentMonthIndex: Int = 0,
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedDayLog: DailyLog? = null,
    val highlights: CalendarHighlights = CalendarHighlights(
        menstruationDays = emptySet(),
        predictedPeriodDays = emptySet(),
        fertileDays = emptySet(),
        ovulationDay = null,
        loggedDays = emptySet(),
        emergencyPillDays = emptySet()
    ),
    val today: LocalDate = LocalDate.now()
)

@HiltViewModel
class CycleCalendarViewModel @Inject constructor(
    userProfileRepository: UserProfileRepository,
    cycleRepository: CycleRepository,
    dailyLogRepository: DailyLogRepository,
    emergencyContraceptionRepository: EmergencyContraceptionRepository,
    private val calculateContextualCycleUseCase: CalculateContextualCycleUseCase,
    private val getCycleCalendarDataUseCase: GetCycleCalendarDataUseCase
) : ViewModel() {

    private val today: LocalDate = LocalDate.now()
    private val selectedDateFlow = MutableStateFlow(today)

    // 12 meses hacia atrás y 6 meses hacia adelante para deslizar suavemente de arriba a abajo
    private val monthsRange: List<YearMonth> = (-12L..6L).map { YearMonth.from(today).plusMonths(it) }
    private val currentMonthIndex: Int = 12

    val uiState: StateFlow<CalendarUiState> = combine(
        userProfileRepository.observeUserProfile(),
        cycleRepository.observeAllCycles(),
        dailyLogRepository.observeAllLogs(),
        emergencyContraceptionRepository.observeAllEvents(),
        selectedDateFlow
    ) { profile, cycles, logs, emergencyEvents, selectedDate ->
        val prediction = profile?.let {
            calculateContextualCycleUseCase(
                userProfile = it,
                historicalCycles = cycles,
                recentEmergencyEvents = emergencyEvents,
                dailyLogs = logs,
                evaluationDate = today
            )
        }
        val emergencyDates = emergencyEvents.map { it.takenDate }.toSet()
        val highlights = getCycleCalendarDataUseCase(
            userProfile = profile,
            cycles = cycles,
            dailyLogs = logs,
            prediction = prediction,
            emergencyDates = emergencyDates,
            today = today
        )
        val selectedDayLog = logs.firstOrNull { it.date == selectedDate }

        CalendarUiState(
            months = monthsRange,
            currentMonthIndex = currentMonthIndex,
            selectedDate = selectedDate,
            selectedDayLog = selectedDayLog,
            highlights = highlights,
            today = today
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CalendarUiState(
                months = monthsRange,
                currentMonthIndex = currentMonthIndex,
                selectedDate = today,
                today = today
            )
        )

    fun onDaySelected(date: LocalDate) {
        selectedDateFlow.value = date
    }
}
