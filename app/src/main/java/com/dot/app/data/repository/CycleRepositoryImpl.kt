package com.dot.app.data.repository

import com.dot.app.data.local.dao.CycleDao
import com.dot.app.data.mapper.toDomain
import com.dot.app.data.mapper.toEntity
import com.dot.app.domain.model.Cycle
import com.dot.app.domain.repository.CycleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CycleRepositoryImpl @Inject constructor(
    private val cycleDao: CycleDao
) : CycleRepository {

    override fun observeAllCycles(): Flow<List<Cycle>> =
        cycleDao.observeAllCycles().map { list -> list.map { it.toDomain() } }

    override suspend fun getCompletedNonAlteredCycles(limit: Int): List<Cycle> =
        cycleDao.getCompletedNonAlteredCycles(limit).map { it.toDomain() }

    override suspend fun getLatestCycle(): Cycle? =
        cycleDao.getLatestCycle()?.toDomain()

    override suspend fun upsertCycle(cycle: Cycle): Long =
        cycleDao.upsertCycle(cycle.toEntity())

    override suspend fun deleteCycle(cycleId: Long) {
        cycleDao.deleteCycleById(cycleId)
    }
}
