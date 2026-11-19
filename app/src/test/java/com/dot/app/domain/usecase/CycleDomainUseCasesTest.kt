package com.dot.app.domain.usecase

import com.dot.app.domain.model.ContraceptiveMethod
import com.dot.app.domain.model.Cycle
import com.dot.app.domain.model.CycleClassification
import com.dot.app.domain.model.CyclePhase
import com.dot.app.domain.model.CycleRegularity
import com.dot.app.domain.model.DailyLog
import com.dot.app.domain.model.EmergencyCompoundType
import com.dot.app.domain.model.EmergencyContraceptionLog
import com.dot.app.domain.model.FlowLevel
import com.dot.app.domain.model.PredictionConfidenceContext
import com.dot.app.domain.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CycleDomainUseCasesTest {

    private val calculateCycle = CalculateContextualCycleUseCase()
    private val analyzeHealth = AnalyzeCycleHealthUseCase()
    private val calendarData = GetCycleCalendarDataUseCase()

    @Test
    fun `cycle starts on day 1 of bleeding and highlights saved onboarding period days`() {
        val today = LocalDate.of(2026, 11, 19)
        val start = today.minusDays(9) // 10 nov -> Día 10 del ciclo el 19 nov
        val end = start.plusDays(4)    // 14 nov (5 días de sangrado)

        val profile = UserProfile(
            lastPeriodStartDate = start,
            lastPeriodEndDate = end,
            isCurrentlyMenstruating = false,
            averagePeriodDuration = 5,
            averageCycleLength = 28,
            regularity = CycleRegularity.REGULAR,
            contraceptiveMethod = ContraceptiveMethod.NONE
        )

        val prediction = calculateCycle(
            userProfile = profile,
            evaluationDate = today
        )

        assertEquals(10, prediction.currentDayOfCycle)
        assertFalse(prediction.isCurrentlyMenstruating)
        assertEquals(CyclePhase.OVULATION_FERTILE, prediction.currentPhase)

        val highlights = calendarData(
            userProfile = profile,
            cycles = listOf(Cycle(startDate = start, bleedingDurationDays = 5)),
            dailyLogs = emptyList(),
            prediction = prediction,
            today = today
        )

        assertTrue(start in highlights.menstruationDays)
        assertTrue(end in highlights.menstruationDays)
        assertEquals(5, highlights.menstruationDays.size)
    }

    @Test
    fun `emergency contraception expands prediction window without false alarm`() {
        val today = LocalDate.of(2026, 11, 19)
        val start = today.minusDays(20)
        val profile = UserProfile(
            lastPeriodStartDate = start,
            averagePeriodDuration = 5,
            averageCycleLength = 28,
            regularity = CycleRegularity.REGULAR,
            contraceptiveMethod = ContraceptiveMethod.NONE
        )

        val emergency = EmergencyContraceptionLog(
            id = 1L,
            takenDate = start.plusDays(12),
            compoundType = EmergencyCompoundType.ULIPRISTAL_ACETATE
        )

        val prediction = calculateCycle(
            userProfile = profile,
            recentEmergencyEvents = listOf(emergency),
            evaluationDate = today
        )

        assertEquals(
            PredictionConfidenceContext.RECALCULATED_EMERGENCY_CONTRACEPTION,
            prediction.confidenceContext
        )
        assertEquals(prediction.baselineExpectedDate.plusDays(10), prediction.estimatedWindowEnd)
    }

    @Test
    fun `cycle health analysis classifies regular vs anomalous and recommends specialist`() {
        val today = LocalDate.of(2026, 11, 19)
        val profile = UserProfile(
            lastPeriodStartDate = today.minusDays(5),
            averagePeriodDuration = 5,
            averageCycleLength = 28,
            regularity = CycleRegularity.REGULAR,
            contraceptiveMethod = ContraceptiveMethod.NONE
        )

        val regularCycles = listOf(
            Cycle(id = 1, startDate = today.minusDays(5), bleedingDurationDays = 5),
            Cycle(id = 2, startDate = today.minusDays(34), endDate = today.minusDays(6), bleedingDurationDays = 5, actualCycleLengthDays = 29),
            Cycle(id = 3, startDate = today.minusDays(62), endDate = today.minusDays(35), bleedingDurationDays = 5, actualCycleLengthDays = 28)
        )

        val regularResult = analyzeHealth(profile, regularCycles, today)
        assertEquals(CycleClassification.NORMAL_REGULAR, regularResult.overallClassification)
        assertFalse(regularResult.shouldConsultSpecialist)

        val anomalousCycles = listOf(
            Cycle(id = 1, startDate = today.minusDays(5), bleedingDurationDays = 10),
            Cycle(id = 2, startDate = today.minusDays(60), endDate = today.minusDays(6), bleedingDurationDays = 6, actualCycleLengthDays = 55)
        )

        val anomalousResult = analyzeHealth(profile, anomalousCycles, today)
        assertEquals(CycleClassification.ANOMALOUS, anomalousResult.overallClassification)
        assertTrue(anomalousResult.shouldConsultSpecialist)
        assertTrue(anomalousResult.specialistReasons.isNotEmpty())
    }
}
