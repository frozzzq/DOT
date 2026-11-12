package com.dot.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.dot.app.data.local.entity.EmergencyContraceptionEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface EmergencyContraceptionDao {

    @Query("SELECT * FROM emergency_contraception_logs ORDER BY taken_date DESC, id DESC")
    fun observeAllEvents(): Flow<List<EmergencyContraceptionEntity>>

    @Query("SELECT * FROM emergency_contraception_logs WHERE taken_date >= :sinceDate ORDER BY taken_date DESC")
    suspend fun getEventsSince(sinceDate: LocalDate): List<EmergencyContraceptionEntity>

    @Upsert
    suspend fun upsertEvent(event: EmergencyContraceptionEntity): Long

    @Query("DELETE FROM emergency_contraception_logs WHERE id = :eventId")
    suspend fun deleteEventById(eventId: Long)
}
