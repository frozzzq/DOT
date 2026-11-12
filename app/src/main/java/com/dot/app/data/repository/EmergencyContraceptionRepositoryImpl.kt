package com.dot.app.data.repository

import com.dot.app.data.local.dao.EmergencyContraceptionDao
import com.dot.app.data.mapper.toDomain
import com.dot.app.data.mapper.toEntity
import com.dot.app.domain.model.EmergencyContraceptionLog
import com.dot.app.domain.repository.EmergencyContraceptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class EmergencyContraceptionRepositoryImpl @Inject constructor(
    private val emergencyContraceptionDao: EmergencyContraceptionDao
) : EmergencyContraceptionRepository {

    override fun observeAllEvents(): Flow<List<EmergencyContraceptionLog>> =
        emergencyContraceptionDao.observeAllEvents().map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun getEventsSince(sinceDate: LocalDate): List<EmergencyContraceptionLog> =
        emergencyContraceptionDao.getEventsSince(sinceDate).map { it.toDomain() }

    override suspend fun upsertEvent(event: EmergencyContraceptionLog): Long =
        emergencyContraceptionDao.upsertEvent(event.toEntity())

    override suspend fun deleteEvent(eventId: Long) {
        emergencyContraceptionDao.deleteEventById(eventId)
    }
}
