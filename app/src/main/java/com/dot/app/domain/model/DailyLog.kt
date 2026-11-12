package com.dot.app.domain.model

import java.time.LocalDate

/**
 * Modelo de dominio para el registro diario de flujo, síntomas y actividad sexual (RF-02).
 */
data class DailyLog(
    val date: LocalDate,
    val flowLevel: FlowLevel = FlowLevel.NONE,
    val symptoms: List<Symptom> = emptyList(),
    val hadSexualActivity: Boolean = false,
    val protectionUsed: ProtectionMethod = ProtectionMethod.NONE,
    val notes: String? = null
)
