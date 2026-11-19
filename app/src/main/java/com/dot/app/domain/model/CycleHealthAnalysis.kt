package com.dot.app.domain.model

/**
 * Evaluación individual de un ciclo dentro del historial (RF-07).
 */
data class EvaluatedCycle(
    val cycle: Cycle,
    val effectiveLengthDays: Int,
    val effectiveBleedingDays: Int,
    val classification: CycleClassification,
    val clinicalNote: String
)

/**
 * Análisis clínico consolidado del historial de ciclos basado en criterios FIGO / ACOG (RF-07).
 */
data class CycleHealthAnalysis(
    val averageCycleLengthDays: Int,
    val averageBleedingDays: Int,
    val cycleVariationDays: Int,
    val completedCyclesCount: Int,
    val overallClassification: CycleClassification,
    val statusHeadline: String,
    val clinicalSummary: String,
    val shouldConsultSpecialist: Boolean,
    val specialistReasons: List<String>,
    val evaluatedCycles: List<EvaluatedCycle>
)
