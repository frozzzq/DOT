package com.dot.app.data.repository

import com.dot.app.data.local.dao.DailyLogDao
import com.dot.app.data.mapper.toDomain
import com.dot.app.data.mapper.toEntity
import com.dot.app.domain.model.DailyLog
import com.dot.app.domain.repository.DailyLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class DailyLogRepositoryImpl @Inject constructor(
    private val dailyLogDao: DailyLogDao
) : DailyLogRepository {

    override fun observeLogsBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<DailyLog>> =
        dailyLogDao.observeLogsBetween(startDate, endDate).map { list ->
            list.map { it.toDomain() }
        }

    override fun observeLogByDate(date: LocalDate): Flow<DailyLog?> =
        dailyLogDao.observeLogByDate(date).map { it?.toDomain() }

    override suspend fun upsertDailyLog(log: DailyLog) {
        dailyLogDao.upsertDailyLog(log.toEntity())
    }

    override suspend fun deleteDailyLog(date: LocalDate) {
        dailyLogDao.deleteDailyLogByDate(date)
    }
}
