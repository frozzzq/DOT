package com.dot.app.domain.repository

import com.dot.app.domain.model.Cycle
import kotlinx.coroutines.flow.Flow

interface CycleRepository {
    fun observeAllCycles(): Flow<List<Cycle>>
    suspend fun getCompletedNonAlteredCycles(limit: Int = 6): List<Cycle>
    suspend fun getLatestCycle(): Cycle?
    suspend fun upsertCycle(cycle: Cycle): Long
    suspend fun deleteCycle(cycleId: Long)
}
