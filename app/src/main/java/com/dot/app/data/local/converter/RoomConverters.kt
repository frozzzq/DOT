package com.dot.app.data.local.converter

import androidx.room.TypeConverter
import com.dot.app.domain.model.ContraceptiveMethod
import com.dot.app.domain.model.CycleRegularity
import com.dot.app.domain.model.EmergencyCompoundType
import com.dot.app.domain.model.FlowLevel
import com.dot.app.domain.model.ForumCategory
import com.dot.app.domain.model.ProtectionMethod
import com.dot.app.domain.model.ReminderType
import com.dot.app.domain.model.Symptom
import java.time.LocalDate
import java.time.LocalTime

/**
 * Convertidores de tipos para almacenar fechas ISO-8601, horas y enumeraciones de dominio
 * de forma íntegra en SQLite mediante Room (RNF-06).
 */
class RoomConverters {

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromLocalTime(time: LocalTime?): String? = time?.toString()

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? = value?.let { LocalTime.parse(it) }

    @TypeConverter
    fun fromCycleRegularity(regularity: CycleRegularity): String = regularity.name

    @TypeConverter
    fun toCycleRegularity(value: String): CycleRegularity =
        runCatching { CycleRegularity.valueOf(value) }.getOrDefault(CycleRegularity.UNKNOWN)

    @TypeConverter
    fun fromContraceptiveMethod(method: ContraceptiveMethod): String = method.name

    @TypeConverter
    fun toContraceptiveMethod(value: String): ContraceptiveMethod =
        runCatching { ContraceptiveMethod.valueOf(value) }.getOrDefault(ContraceptiveMethod.NONE)

    @TypeConverter
    fun fromFlowLevel(flowLevel: FlowLevel): String = flowLevel.name

    @TypeConverter
    fun toFlowLevel(value: String): FlowLevel =
        runCatching { FlowLevel.valueOf(value) }.getOrDefault(FlowLevel.NONE)

    @TypeConverter
    fun fromProtectionMethod(method: ProtectionMethod): String = method.name

    @TypeConverter
    fun toProtectionMethod(value: String): ProtectionMethod =
        runCatching { ProtectionMethod.valueOf(value) }.getOrDefault(ProtectionMethod.NONE)

    @TypeConverter
    fun fromEmergencyCompoundType(type: EmergencyCompoundType): String = type.name

    @TypeConverter
    fun toEmergencyCompoundType(value: String): EmergencyCompoundType =
        runCatching { EmergencyCompoundType.valueOf(value) }.getOrDefault(EmergencyCompoundType.UNKNOWN)

    @TypeConverter
    fun fromReminderType(type: ReminderType): String = type.name

    @TypeConverter
    fun toReminderType(value: String): ReminderType =
        runCatching { ReminderType.valueOf(value) }.getOrDefault(ReminderType.PERIOD_LOG_REMINDER)

    @TypeConverter
    fun fromForumCategory(category: ForumCategory): String = category.name

    @TypeConverter
    fun toForumCategory(value: String): ForumCategory =
        runCatching { ForumCategory.valueOf(value) }.getOrDefault(ForumCategory.CYCLE)

    @TypeConverter
    fun fromSymptomList(symptoms: List<Symptom>): String =
        symptoms.joinToString(separator = ",") { it.name }

    @TypeConverter
    fun toSymptomList(serialized: String): List<Symptom> {
        if (serialized.isBlank()) return emptyList()
        return serialized.split(",").mapNotNull { token ->
            runCatching { Symptom.valueOf(token.trim()) }.getOrNull()
        }
    }
}
