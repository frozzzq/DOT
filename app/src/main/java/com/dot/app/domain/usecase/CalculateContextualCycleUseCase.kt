package com.dot.app.domain.usecase

import com.dot.app.domain.model.ContraceptiveMethod
import com.dot.app.domain.model.Cycle
import com.dot.app.domain.model.CyclePhase
import com.dot.app.domain.model.CyclePrediction
import com.dot.app.domain.model.CycleRegularity
import com.dot.app.domain.model.DailyLog
import com.dot.app.domain.model.EmergencyCompoundType
import com.dot.app.domain.model.EmergencyContraceptionLog
import com.dot.app.domain.model.PredictionConfidenceContext
import com.dot.app.domain.model.UserProfile
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Motor de predicción y recálculo contextual del ciclo menstrual (RF-04).
 *
 * El ciclo menstrual inicia el Día 1 (primer día de sangrado) y concluye el día previo
 * al inicio de la siguiente menstruación.
 */
class CalculateContextualCycleUseCase @Inject constructor() {

    operator fun invoke(
        userProfile: UserProfile,
        historicalCycles: List<Cycle> = emptyList(),
        recentEmergencyEvents: List<EmergencyContraceptionLog> = emptyList(),
        dailyLogs: List<DailyLog> = emptyList(),
        evaluationDate: LocalDate = LocalDate.now()
    ): CyclePrediction {
        val latestCycle = historicalCycles.maxByOrNull { it.startDate }
        val currentCycleStart = latestCycle?.startDate ?: userProfile.lastPeriodStartDate

        // Excluir ciclos alterados por anticoncepción de emergencia al calcular el promedio habitual (RF-07)
        val validHistoricalLengths = historicalCycles
            .filter { !it.isAlteredByEmergencyContraception && it.actualCycleLengthDays != null }
            .mapNotNull { it.actualCycleLengthDays }
            .filter { it in MIN_PHYSIOLOGICAL_CYCLE_DAYS..MAX_PHYSIOLOGICAL_CYCLE_DAYS }

        val effectiveCycleLength = if (validHistoricalLengths.isNotEmpty()) {
            validHistoricalLengths.average().roundToInt()
        } else {
            userProfile.averageCycleLength.coerceIn(
                MIN_PHYSIOLOGICAL_CYCLE_DAYS,
                MAX_PHYSIOLOGICAL_CYCLE_DAYS
            )
        }

        val baselineExpectedDate = currentCycleStart.plusDays(effectiveCycleLength.toLong())
        val currentDayOfCycle = max(
            1,
            ChronoUnit.DAYS.between(currentCycleStart, evaluationDate).toInt() + 1
        )

        // Determinar si hoy está en sangrado menstrual activo
        val todayLog = dailyLogs.firstOrNull { it.date == evaluationDate }
        val bleedingDuration = latestCycle?.bleedingDurationDays ?: userProfile.averagePeriodDuration
        val isWithinInitialBleedingDays = currentDayOfCycle <= bleedingDuration &&
            (userProfile.lastPeriodEndDate == null || !evaluationDate.isAfter(userProfile.lastPeriodEndDate))
        val isCurrentlyMenstruating = when {
            todayLog != null -> todayLog.hasMenstrualBleeding
            userProfile.isCurrentlyMenstruating && currentCycleStart == userProfile.lastPeriodStartDate &&
                currentDayOfCycle <= MAX_ACTIVE_BLEEDING_DAYS -> true
            else -> isWithinInitialBleedingDays
        }

        // Detectar si existe una toma de anticoncepción de emergencia dentro del ciclo actual
        val activeEmergencyEvent = recentEmergencyEvents
            .filter { !it.takenDate.isBefore(currentCycleStart) && !it.takenDate.isAfter(evaluationDate) }
            .maxByOrNull { it.takenDate }

        val (windowStartOffset, windowEndOffset, confidenceContext) = resolveContextualWindowOffsets(
            userProfile = userProfile,
            effectiveCycleLength = effectiveCycleLength,
            currentCycleStart = currentCycleStart,
            activeEmergencyEvent = activeEmergencyEvent
        )

        val estimatedWindowStart = baselineExpectedDate.plusDays(windowStartOffset.toLong())
        val estimatedWindowEnd = baselineExpectedDate.plusDays(windowEndOffset.toLong())

        val daysPastBaseline = max(
            0,
            ChronoUnit.DAYS.between(baselineExpectedDate, evaluationDate).toInt()
        )
        val daysPastExpandedWindow = max(
            0,
            ChronoUnit.DAYS.between(estimatedWindowEnd, evaluationDate).toInt()
        )

        // Estimación orientativa de ovulación y ventana fértil (solo cuando no hay supresión ovulatoria hormonal)
        val suppressesOvulation = userProfile.contraceptiveMethod in OVULATION_SUPPRESSING_METHODS
        val estimatedOvulationDayOffset = max(10, effectiveCycleLength - LUTEAL_PHASE_STANDARD_DAYS) - 1
        val estimatedOvulationDate = if (!suppressesOvulation) {
            currentCycleStart.plusDays(estimatedOvulationDayOffset.toLong())
        } else {
            null
        }
        val fertileStart = estimatedOvulationDate?.minusDays(5)
        val fertileEnd = estimatedOvulationDate?.plusDays(1)

        val currentPhase = resolveCurrentPhase(
            isCurrentlyMenstruating = isCurrentlyMenstruating,
            evaluationDate = evaluationDate,
            fertileStart = fertileStart,
            fertileEnd = fertileEnd,
            estimatedOvulationDate = estimatedOvulationDate,
            currentDayOfCycle = currentDayOfCycle,
            effectiveCycleLength = effectiveCycleLength
        )

        return CyclePrediction(
            referenceCycleStartDate = currentCycleStart,
            currentDayOfCycle = currentDayOfCycle,
            expectedCycleLengthDays = effectiveCycleLength,
            baselineExpectedDate = baselineExpectedDate,
            estimatedWindowStart = estimatedWindowStart,
            estimatedWindowEnd = estimatedWindowEnd,
            expectedBleedingDurationDays = bleedingDuration,
            currentPhase = currentPhase,
            isCurrentlyMenstruating = isCurrentlyMenstruating,
            estimatedOvulationDate = estimatedOvulationDate,
            fertileWindowStart = fertileStart,
            fertileWindowEnd = fertileEnd,
            confidenceContext = confidenceContext,
            activeEmergencyContraception = activeEmergencyEvent,
            daysPastBaselineDate = daysPastBaseline,
            daysPastExpandedWindow = daysPastExpandedWindow
        )
    }

    private fun resolveCurrentPhase(
        isCurrentlyMenstruating: Boolean,
        evaluationDate: LocalDate,
        fertileStart: LocalDate?,
        fertileEnd: LocalDate?,
        estimatedOvulationDate: LocalDate?,
        currentDayOfCycle: Int,
        effectiveCycleLength: Int
    ): CyclePhase {
        if (isCurrentlyMenstruating) return CyclePhase.MENSTRUAL
        if (fertileStart != null && fertileEnd != null &&
            !evaluationDate.isBefore(fertileStart) && !evaluationDate.isAfter(fertileEnd)
        ) {
            return CyclePhase.OVULATION_FERTILE
        }
        val ovulationDay = max(10, effectiveCycleLength - LUTEAL_PHASE_STANDARD_DAYS)
        return if (estimatedOvulationDate != null && evaluationDate.isBefore(estimatedOvulationDate)) {
            CyclePhase.FOLLICULAR
        } else if (currentDayOfCycle < ovulationDay) {
            CyclePhase.FOLLICULAR
        } else {
            CyclePhase.LUTEAL
        }
    }

    private fun resolveContextualWindowOffsets(
        userProfile: UserProfile,
        effectiveCycleLength: Int,
        currentCycleStart: LocalDate,
        activeEmergencyEvent: EmergencyContraceptionLog?
    ): Triple<Int, Int, PredictionConfidenceContext> {
        if (activeEmergencyEvent != null) {
            val cycleDayAtIntake = ChronoUnit.DAYS.between(
                currentCycleStart,
                activeEmergencyEvent.takenDate
            ).toInt() + 1
            val estimatedOvulationDay = max(10, effectiveCycleLength - LUTEAL_PHASE_STANDARD_DAYS)

            val earlyMargin = if (cycleDayAtIntake <= estimatedOvulationDay) {
                -EARLY_BLEEDING_MARGIN_EMERGENCY_DAYS
            } else {
                -STANDARD_REGULAR_MARGIN_DAYS
            }

            val lateMargin = when (activeEmergencyEvent.compoundType) {
                EmergencyCompoundType.ULIPRISTAL_ACETATE -> ULIPRISTAL_LATE_MARGIN_DAYS
                EmergencyCompoundType.LEVONORGESTREL,
                EmergencyCompoundType.UNKNOWN -> LEVONORGESTREL_LATE_MARGIN_DAYS
            }

            return Triple(
                earlyMargin,
                lateMargin,
                PredictionConfidenceContext.RECALCULATED_EMERGENCY_CONTRACEPTION
            )
        }

        if (userProfile.contraceptiveMethod in HORMONAL_SCHEDULED_METHODS) {
            return Triple(
                -HORMONAL_CONTROLLED_MARGIN_DAYS,
                HORMONAL_CONTROLLED_MARGIN_DAYS,
                PredictionConfidenceContext.HORMONAL_CONTRACEPTION_CONTROLLED
            )
        }

        return when (userProfile.regularity) {
            CycleRegularity.REGULAR -> Triple(
                -STANDARD_REGULAR_MARGIN_DAYS,
                STANDARD_REGULAR_MARGIN_DAYS,
                PredictionConfidenceContext.STANDARD_REGULAR
            )
            CycleRegularity.IRREGULAR,
            CycleRegularity.UNKNOWN -> Triple(
                -IRREGULAR_EARLY_MARGIN_DAYS,
                IRREGULAR_LATE_MARGIN_DAYS,
                PredictionConfidenceContext.EXPANDED_IRREGULAR
            )
        }
    }

    companion object {
        private const val MIN_PHYSIOLOGICAL_CYCLE_DAYS = 21
        private const val MAX_PHYSIOLOGICAL_CYCLE_DAYS = 45
        private const val LUTEAL_PHASE_STANDARD_DAYS = 14
        private const val MAX_ACTIVE_BLEEDING_DAYS = 12

        private const val HORMONAL_CONTROLLED_MARGIN_DAYS = 1
        private const val STANDARD_REGULAR_MARGIN_DAYS = 2
        private const val IRREGULAR_EARLY_MARGIN_DAYS = 4
        private const val IRREGULAR_LATE_MARGIN_DAYS = 7

        private const val EARLY_BLEEDING_MARGIN_EMERGENCY_DAYS = 5
        private const val LEVONORGESTREL_LATE_MARGIN_DAYS = 7
        private const val ULIPRISTAL_LATE_MARGIN_DAYS = 10

        private val HORMONAL_SCHEDULED_METHODS = setOf(
            ContraceptiveMethod.COMBINED_ORAL_PILL,
            ContraceptiveMethod.VAGINAL_RING_OR_PATCH
        )

        private val OVULATION_SUPPRESSING_METHODS = setOf(
            ContraceptiveMethod.COMBINED_ORAL_PILL,
            ContraceptiveMethod.SUBDERMAL_IMPLANT,
            ContraceptiveMethod.INJECTION,
            ContraceptiveMethod.VAGINAL_RING_OR_PATCH
        )
    }
}
