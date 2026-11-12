package com.dot.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dot.app.data.local.converter.RoomConverters
import com.dot.app.data.local.dao.CycleDao
import com.dot.app.data.local.dao.DailyLogDao
import com.dot.app.data.local.dao.EmergencyContraceptionDao
import com.dot.app.data.local.dao.UserProfileDao
import com.dot.app.data.local.entity.CycleEntity
import com.dot.app.data.local.entity.DailyLogEntity
import com.dot.app.data.local.entity.EmergencyContraceptionEntity
import com.dot.app.data.local.entity.UserProfileEntity

/**
 * Base de datos relacional local Offline-First de Dot (RNF-04, RNF-06).
 */
@Database(
    entities = [
        UserProfileEntity::class,
        CycleEntity::class,
        DailyLogEntity::class,
        EmergencyContraceptionEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(RoomConverters::class)
abstract class DotDatabase : RoomDatabase() {

    abstract fun userProfileDao(): UserProfileDao
    abstract fun cycleDao(): CycleDao
    abstract fun dailyLogDao(): DailyLogDao
    abstract fun emergencyContraceptionDao(): EmergencyContraceptionDao

    companion object {
        const val DATABASE_NAME = "dot_health_offline.db"
    }
}
