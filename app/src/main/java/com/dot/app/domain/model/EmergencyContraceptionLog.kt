package com.dot.app.domain.model

import java.time.LocalDate
import java.time.LocalTime

/**
 * Modelo de dominio para un evento de ingesta de anticoncepción hormonal de emergencia (RF-03).
 */
data class EmergencyContraceptionLog(
    val id: Long = 0L,
    val takenDate: LocalDate,
    val approximateTime: LocalTime? = null,
    val compoundType: EmergencyCompoundType = EmergencyCompoundType.LEVONORGESTREL,
    val priorProtectionUsed: Boolean = false,
    val hadSubsequentIntercourse: Boolean = false,
    val notes: String? = null
)
