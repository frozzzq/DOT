package com.dot.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Entidad Room para el historial y estadísticas de ciclos menstruales (RF-07, RF-08).
 */
@Entity(
    tableName = "cycles",
    indices = [Index(value = ["start_date"], unique = true)]
)
data class CycleEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "start_date")
    val startDate: LocalDate,

    @ColumnInfo(name = "end_date")
    val endDate: LocalDate? = null,

    @ColumnInfo(name = "bleeding_duration_days")
    val bleedingDurationDays: Int? = null,

    @ColumnInfo(name = "actual_cycle_length_days")
    val actualCycleLengthDays: Int? = null,

    @ColumnInfo(name = "is_altered_by_emergency_contraception")
    val isAlteredByEmergencyContraception: Boolean = false,

    @ColumnInfo(name = "context_notes")
    val contextNotes: String? = null
)
