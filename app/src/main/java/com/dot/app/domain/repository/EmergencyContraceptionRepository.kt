package com.dot.app.domain.repository

import com.dot.app.domain.model.EmergencyContraceptionLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface EmergencyContraceptionRepository {
    fun observeAllEvents(): Flow<List<EmergencyContraceptionLog>>
    suspend fun getEventsSince(sinceDate: LocalDate): List<EmergencyContraceptionLog>
    suspend fun upsertEvent(event: EmergencyContraceptionLog): Long
    suspend fun deleteEvent(eventId: Long)
}
