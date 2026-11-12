package com.dot.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.dot.app.data.local.entity.CycleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CycleDao {

    @Query("SELECT * FROM cycles ORDER BY start_date DESC")
    fun observeAllCycles(): Flow<List<CycleEntity>>

    @Query(
        """
        SELECT * FROM cycles
        WHERE is_altered_by_emergency_contraception = 0
          AND actual_cycle_length_days IS NOT NULL
        ORDER BY start_date DESC
        LIMIT :limit
        """
    )
    suspend fun getCompletedNonAlteredCycles(limit: Int): List<CycleEntity>

    @Query("SELECT * FROM cycles ORDER BY start_date DESC LIMIT 1")
    suspend fun getLatestCycle(): CycleEntity?

    @Upsert
    suspend fun upsertCycle(cycle: CycleEntity): Long

    @Query("DELETE FROM cycles WHERE id = :cycleId")
    suspend fun deleteCycleById(cycleId: Long)
}
