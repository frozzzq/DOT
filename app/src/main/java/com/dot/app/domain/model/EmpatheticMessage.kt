package com.dot.app.domain.model

import java.time.LocalDate

enum class MessageCategory {
    CYCLE_IN_PROGRESS_NORMAL,
    WITHIN_EXPECTED_WINDOW,
    EXPLAINABLE_VARIATION_EMERGENCY_PILL,
    EXPLAINABLE_VARIATION_IRREGULARITY,
    MEDICAL_CHECKUP_SUGGESTION
}

/**
 * Mensaje empático y contextual generado por la capa de dominio (RF-05, RNF-03).
 */
data class EmpatheticMessage(
    val title: String,
    val body: String,
    val reassuranceNote: String,
    val actionableGuidance: String? = null,
    val suggestedPregnancyTestDate: LocalDate? = null,
    val category: MessageCategory
)
