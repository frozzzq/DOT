package com.dot.app.domain.model

import java.time.LocalDate

/**
 * Modelo de dominio para el registro diario de flujo, síntomas, anticonceptivos y actividad sexual (RF-02).
 */
data class DailyLog(
    val date: LocalDate,
    val flowLevel: FlowLevel = FlowLevel.NONE,
    val symptoms: List<Symptom> = emptyList(),
    val hadSexualActivity: Boolean = false,
    val protectionUsed: ProtectionMethod = ProtectionMethod.NONE,
    val tookContraceptivePill: Boolean = false,
    val tookEmergencyPill: Boolean = false,
    val notes: String? = null
) {
    val hasMenstrualBleeding: Boolean
        get() = flowLevel == FlowLevel.LIGHT ||
            flowLevel == FlowLevel.MODERATE ||
            flowLevel == FlowLevel.HEAVY
}
