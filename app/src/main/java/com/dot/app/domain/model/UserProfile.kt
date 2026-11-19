package com.dot.app.domain.model

import java.time.LocalDate

/**
 * Modelo de dominio que representa la línea base fisiológica capturada en el Onboarding (RF-01).
 */
data class UserProfile(
    val id: Int = SINGLETON_PROFILE_ID,
    val lastPeriodStartDate: LocalDate,
    val lastPeriodEndDate: LocalDate? = null,
    val isCurrentlyMenstruating: Boolean = false,
    val averagePeriodDuration: Int,
    val averageCycleLength: Int,
    val regularity: CycleRegularity,
    val contraceptiveMethod: ContraceptiveMethod,
    val isOnboardingCompleted: Boolean = true
) {
    companion object {
        const val SINGLETON_PROFILE_ID = 1
    }
}
