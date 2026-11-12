package com.dot.app.domain.model

import java.time.LocalDate

/**
 * Modelo de dominio para el registro histórico de un ciclo menstrual (RF-07, RF-08).
 */
data class Cycle(
    val id: Long = 0L,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val bleedingDurationDays: Int? = null,
    val actualCycleLengthDays: Int? = null,
    val isAlteredByEmergencyContraception: Boolean = false,
    val contextNotes: String? = null
)
