package com.dot.app.domain.usecase

import com.dot.app.domain.model.ContraceptiveMethod
import com.dot.app.domain.model.Cycle
import com.dot.app.domain.model.CyclePrediction
import com.dot.app.domain.model.CycleRegularity
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
 * Calcula la ventana estimada del próximo sangrado tomando en cuenta:
 * 1. El historial de ciclos no alterados (para no sesgar el promedio habitual, RF-07).
 * 2. El grado de regularidad percibida o variabilidad histórica de la usuaria.
 * 3. La ingesta de anticoncepción hormonal de emergencia durante el ciclo activo (RF-03),
 *    ampliando la ventana esperada según la fase de administración y el compuesto activo
 *    (Levonorgestrel o Acetato de ulipristal) en lugar de marcar un retraso rígido.
 */
class CalculateContextualCycleUseCase @Inject constructor() {

    operator fun invoke(
        userProfile: UserProfile,
        historicalCycles: List<Cycle> = emptyList(),
        recentEmergencyEvents: List<EmergencyContraceptionLog> = emptyList(),
        evaluationDate: LocalDate = LocalDate.now()
    ): CyclePrediction {
        val currentCycleStart = historicalCycles
            .maxByOrNull { it.startDate }
            ?.startDate
            ?: userProfile.lastPeriodStartDate

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

        return CyclePrediction(
            referenceCycleStartDate = currentCycleStart,
            currentDayOfCycle = currentDayOfCycle,
            baselineExpectedDate = baselineExpectedDate,
            estimatedWindowStart = estimatedWindowStart,
            estimatedWindowEnd = estimatedWindowEnd,
            expectedBleedingDurationDays = userProfile.averagePeriodDuration,
            confidenceContext = confidenceContext,
            activeEmergencyContraception = activeEmergencyEvent,
            daysPastBaselineDate = daysPastBaseline,
            daysPastExpandedWindow = daysPastExpandedWindow
        )
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

            // Si la toma ocurrió en fase folicular (antes de la ovulación estimada), puede
            // adelantar el sangrado por deprivación o posponer la ovulación entre 7 y 10 días.
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
    }
}
