package com.dot.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dot.app.domain.model.FlowLevel
import com.dot.app.domain.model.ProtectionMethod
import com.dot.app.domain.model.Symptom
import java.time.LocalDate

/**
 * Entidad Room para el registro diario de flujo menstrual, síntomas y actividad sexual (RF-02).
 */
@Entity(tableName = "daily_logs")
data class DailyLogEntity(
    @PrimaryKey
    @ColumnInfo(name = "date")
    val date: LocalDate,

    @ColumnInfo(name = "flow_level")
    val flowLevel: FlowLevel = FlowLevel.NONE,

    @ColumnInfo(name = "symptoms")
    val symptoms: List<Symptom> = emptyList(),

    @ColumnInfo(name = "had_sexual_activity")
    val hadSexualActivity: Boolean = false,

    @ColumnInfo(name = "protection_used")
    val protectionUsed: ProtectionMethod = ProtectionMethod.NONE,

    @ColumnInfo(name = "notes")
    val notes: String? = null
)
