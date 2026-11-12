package com.dot.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dot.app.domain.model.EmergencyCompoundType
import java.time.LocalDate
import java.time.LocalTime

/**
 * Entidad Room para eventos de ingesta de anticoncepción de emergencia (RF-03).
 */
@Entity(
    tableName = "emergency_contraception_logs",
    indices = [Index(value = ["taken_date"])]
)
data class EmergencyContraceptionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "taken_date")
    val takenDate: LocalDate,

    @ColumnInfo(name = "approximate_time")
    val approximateTime: LocalTime? = null,

    @ColumnInfo(name = "compound_type")
    val compoundType: EmergencyCompoundType = EmergencyCompoundType.LEVONORGESTREL,

    @ColumnInfo(name = "prior_protection_used")
    val priorProtectionUsed: Boolean = false,

    @ColumnInfo(name = "had_subsequent_intercourse")
    val hadSubsequentIntercourse: Boolean = false,

    @ColumnInfo(name = "notes")
    val notes: String? = null
)
