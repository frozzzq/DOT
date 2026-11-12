package com.dot.app.domain.repository

import com.dot.app.domain.model.DailyLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface DailyLogRepository {
    fun observeLogsBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<DailyLog>>
    fun observeLogByDate(date: LocalDate): Flow<DailyLog?>
    suspend fun upsertDailyLog(log: DailyLog)
    suspend fun deleteDailyLog(date: LocalDate)
}
