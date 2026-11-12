package com.dot.app.domain.model

import java.time.LocalDate

/**
 * Nivel de confianza y contexto fisiológico asociado a la predicción actual.
 */
enum class PredictionConfidenceContext {
    STANDARD_REGULAR,
    EXPANDED_IRREGULAR,
    RECALCULATED_EMERGENCY_CONTRACEPTION,
    HORMONAL_CONTRACEPTION_CONTROLLED
}

/**
 * Resultado del cálculo contextual de ciclo (RF-04).
 *
 * En lugar de exponer un único día rígido que cause ansiedad ante variaciones normales,
 * define una ventana estimada ([estimatedWindowStart] a [estimatedWindowEnd]) junto con
 * la fecha central de referencia y el contexto fisiológico activo.
 */
data class CyclePrediction(
    val referenceCycleStartDate: LocalDate,
    val currentDayOfCycle: Int,
    val baselineExpectedDate: LocalDate,
    val estimatedWindowStart: LocalDate,
    val estimatedWindowEnd: LocalDate,
    val expectedBleedingDurationDays: Int,
    val confidenceContext: PredictionConfidenceContext,
    val activeEmergencyContraception: EmergencyContraceptionLog? = null,
    val daysPastBaselineDate: Int = 0,
    val daysPastExpandedWindow: Int = 0
) {
    val isWithinExpectedWindow: Boolean
        get() = daysPastExpandedWindow <= 0
}
