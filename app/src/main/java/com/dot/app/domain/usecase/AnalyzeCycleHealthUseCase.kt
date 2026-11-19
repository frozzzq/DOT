package com.dot.app.domain.usecase

import com.dot.app.domain.model.Cycle
import com.dot.app.domain.model.CycleClassification
import com.dot.app.domain.model.CycleHealthAnalysis
import com.dot.app.domain.model.CycleRegularity
import com.dot.app.domain.model.EvaluatedCycle
import com.dot.app.domain.model.UserProfile
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Analizador clínico del historial menstrual basado en parámetros FIGO / ACOG (RF-07).
 *
 * Clasifica cada ciclo (Normal/Regular, Irregular, Anómalo, Alterado por Postday o En curso)
 * y determina con lenguaje empático y objetivo si conviene sugerir valoración ginecológica.
 */
class AnalyzeCycleHealthUseCase @Inject constructor() {

    operator fun invoke(
        userProfile: UserProfile?,
        cycles: List<Cycle>,
        evaluationDate: LocalDate = LocalDate.now()
    ): CycleHealthAnalysis {
        val sortedCycles = cycles.sortedByDescending { it.startDate }
        val defaultBleeding = userProfile?.averagePeriodDuration ?: 5
        val defaultCycleLen = userProfile?.averageCycleLength ?: 28

        val evaluatedCycles = sortedCycles.mapIndexed { index, cycle ->
            val isLatestOngoing = index == 0 && cycle.endDate == null && cycle.actualCycleLengthDays == null
            val effectiveLength = when {
                cycle.actualCycleLengthDays != null -> cycle.actualCycleLengthDays
                cycle.endDate != null -> max(1, ChronoUnit.DAYS.between(cycle.startDate, cycle.endDate).toInt() + 1)
                else -> max(1, ChronoUnit.DAYS.between(cycle.startDate, evaluationDate).toInt() + 1)
            }
            val effectiveBleeding = cycle.bleedingDurationDays ?: defaultBleeding

            val (classification, note) = classifyIndividualCycle(
                cycle = cycle,
                effectiveLength = effectiveLength,
                effectiveBleeding = effectiveBleeding,
                isOngoing = isLatestOngoing
            )

            EvaluatedCycle(
                cycle = cycle,
                effectiveLengthDays = effectiveLength,
                effectiveBleedingDays = effectiveBleeding,
                classification = classification,
                clinicalNote = note
            )
        }

        // Ciclos completados no alterados por Postday para métricas reales (RF-07)
        val completedNonAltered = evaluatedCycles.filter {
            it.classification != CycleClassification.IN_PROGRESS &&
                !it.cycle.isAlteredByEmergencyContraception
        }

        val avgCycleLength = if (completedNonAltered.isNotEmpty()) {
            completedNonAltered.map { it.effectiveLengthDays }.average().roundToInt()
        } else {
            defaultCycleLen
        }

        val avgBleeding = if (evaluatedCycles.isNotEmpty()) {
            evaluatedCycles.map { it.effectiveBleedingDays }.average().roundToInt()
        } else {
            defaultBleeding
        }

        val variationDays = if (completedNonAltered.size >= 2) {
            val lengths = completedNonAltered.map { it.effectiveLengthDays }
            (lengths.maxOrNull() ?: 0) - (lengths.minOrNull() ?: 0)
        } else {
            0
        }

        val specialistReasons = mutableListOf<String>()

        val anomalousCount = evaluatedCycles.count { it.classification == CycleClassification.ANOMALOUS }
        val irregularCount = evaluatedCycles.count { it.classification == CycleClassification.IRREGULAR }
        val prolongedBleedingCount = evaluatedCycles.count { it.effectiveBleedingDays > MAX_NORMAL_BLEEDING_DAYS }
        val ongoingCycle = evaluatedCycles.firstOrNull { it.classification == CycleClassification.IN_PROGRESS }

        if (ongoingCycle != null && ongoingCycle.effectiveLengthDays >= AMENORRHEA_DAYS_THRESHOLD) {
            specialistReasons.add("Han pasado más de 90 días sin registro de un nuevo sangrado menstrual.")
        }
        if (anomalousCount >= 1) {
            specialistReasons.add("Se registró al menos un ciclo con duración menor a 21 días o mayor a 45 días.")
        }
        if (prolongedBleedingCount >= 1) {
            specialistReasons.add("Se registró sangrado menstrual prolongado (mayor a 8 días).")
        }
        if (variationDays > MAX_NORMAL_VARIATION_DAYS && completedNonAltered.size >= 2) {
            specialistReasons.add("La variación entre tu ciclo más corto y el más largo es de $variationDays días (lo habitual es hasta 9 días).")
        }
        if (irregularCount >= 3) {
            specialistReasons.add("Presentas varios ciclos consecutivos fuera del rango típico de 24 a 38 días.")
        }

        val overallClassification = when {
            anomalousCount >= 1 ||
                prolongedBleedingCount >= 1 ||
                (ongoingCycle != null && ongoingCycle.effectiveLengthDays >= AMENORRHEA_DAYS_THRESHOLD) ->
                CycleClassification.ANOMALOUS

            variationDays > MAX_NORMAL_VARIATION_DAYS ||
                irregularCount >= 2 ||
                (completedNonAltered.isEmpty() && userProfile?.regularity == CycleRegularity.IRREGULAR) ->
                CycleClassification.IRREGULAR

            else -> CycleClassification.NORMAL_REGULAR
        }

        val shouldConsult = specialistReasons.isNotEmpty()

        val (headline, summary) = when (overallClassification) {
            CycleClassification.ANOMALOUS ->
                "Te recomendamos valoración médica" to
                    "Tus registros muestran duraciones o sangrados fuera de los parámetros clínicos habituales (24–38 días de ciclo y hasta 8 días de sangrado). Consultar a una ginecóloga con este historial te dará claridad y tranquilidad."

            CycleClassification.IRREGULAR ->
                "Ciclos con variabilidad (Irregulares)" to
                    "Tus ciclos presentan variaciones notables entre un mes y otro. Esto puede ocurrir por estrés, cambios hormonales o SOP. Continúa registrando y compártelo en tu próximo chequeo."

            else ->
                "Ciclos dentro de rangos normales" to
                    "Tu duración promedio ($avgCycleLength días) y sangrado ($avgBleeding días) se mantienen dentro de los parámetros saludables establecidos por guías clínicas internacionales (FIGO: 24–38 días)."
        }

        return CycleHealthAnalysis(
            averageCycleLengthDays = avgCycleLength,
            averageBleedingDays = avgBleeding,
            cycleVariationDays = variationDays,
            completedCyclesCount = completedNonAltered.size,
            overallClassification = overallClassification,
            statusHeadline = headline,
            clinicalSummary = summary,
            shouldConsultSpecialist = shouldConsult,
            specialistReasons = specialistReasons,
            evaluatedCycles = evaluatedCycles
        )
    }

    private fun classifyIndividualCycle(
        cycle: Cycle,
        effectiveLength: Int,
        effectiveBleeding: Int,
        isOngoing: Boolean
    ): Pair<CycleClassification, String> {
        if (cycle.isAlteredByEmergencyContraception) {
            return CycleClassification.ALTERED_BY_EMERGENCY to
                "Ciclo influido por anticoncepción de emergencia (excluido del promedio habitual)."
        }

        if (isOngoing) {
            return if (effectiveLength > MAX_IRREGULAR_CYCLE_DAYS) {
                CycleClassification.ANOMALOUS to
                    "Ciclo en curso con $effectiveLength días; si no hay embarazo ni Postday, conviene consultar."
            } else if (effectiveBleeding > MAX_NORMAL_BLEEDING_DAYS) {
                CycleClassification.ANOMALOUS to
                    "Sangrado de $effectiveBleeding días (superior a 8 días). Recomendable evaluar."
            } else {
                CycleClassification.IN_PROGRESS to
                    "Ciclo actual en curso (Día $effectiveLength)."
            }
        }

        if (effectiveLength < MIN_IRREGULAR_CYCLE_DAYS ||
            effectiveLength > MAX_IRREGULAR_CYCLE_DAYS ||
            effectiveBleeding > MAX_NORMAL_BLEEDING_DAYS
        ) {
            val detail = when {
                effectiveBleeding > MAX_NORMAL_BLEEDING_DAYS ->
                    "Sangrado prolongado ($effectiveBleeding días)."
                effectiveLength < MIN_IRREGULAR_CYCLE_DAYS ->
                    "Ciclo muy corto ($effectiveLength días, menor a 21 días)."
                else ->
                    "Ciclo muy largo ($effectiveLength días, mayor a 45 días)."
            }
            return CycleClassification.ANOMALOUS to detail
        }

        if (effectiveLength !in MIN_NORMAL_CYCLE_DAYS..MAX_NORMAL_CYCLE_DAYS) {
            return CycleClassification.IRREGULAR to
                "Duración de $effectiveLength días, ligeramente fuera del rango estándar (24–38 días)."
        }

        return CycleClassification.NORMAL_REGULAR to
            "Duración saludable ($effectiveLength días) y sangrado habitual ($effectiveBleeding días)."
    }

    companion object {
        private const val MIN_NORMAL_CYCLE_DAYS = 24
        private const val MAX_NORMAL_CYCLE_DAYS = 38
        private const val MIN_IRREGULAR_CYCLE_DAYS = 21
        private const val MAX_IRREGULAR_CYCLE_DAYS = 45
        private const val MAX_NORMAL_BLEEDING_DAYS = 8
        private const val MAX_NORMAL_VARIATION_DAYS = 9
        private const val AMENORRHEA_DAYS_THRESHOLD = 90
    }
}
