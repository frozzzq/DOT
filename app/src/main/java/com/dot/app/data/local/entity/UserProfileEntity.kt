package com.dot.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dot.app.domain.model.ContraceptiveMethod
import com.dot.app.domain.model.CycleRegularity
import com.dot.app.domain.model.UserProfile
import java.time.LocalDate

/**
 * Entidad Room para persistir la configuración base de la usuaria (RF-01).
 */
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int = UserProfile.SINGLETON_PROFILE_ID,

    @ColumnInfo(name = "last_period_start_date")
    val lastPeriodStartDate: LocalDate,

    @ColumnInfo(name = "last_period_end_date")
    val lastPeriodEndDate: LocalDate? = null,

    @ColumnInfo(name = "is_currently_menstruating")
    val isCurrentlyMenstruating: Boolean = false,

    @ColumnInfo(name = "average_period_duration")
    val averagePeriodDuration: Int,

    @ColumnInfo(name = "average_cycle_length")
    val averageCycleLength: Int,

    @ColumnInfo(name = "regularity")
    val regularity: CycleRegularity,

    @ColumnInfo(name = "contraceptive_method")
    val contraceptiveMethod: ContraceptiveMethod,

    @ColumnInfo(name = "is_onboarding_completed")
    val isOnboardingCompleted: Boolean = true
)
