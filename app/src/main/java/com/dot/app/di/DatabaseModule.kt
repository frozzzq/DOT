package com.dot.app.di

import android.content.Context
import androidx.room.Room
import com.dot.app.data.local.DotDatabase
import com.dot.app.data.local.dao.CycleDao
import com.dot.app.data.local.dao.DailyLogDao
import com.dot.app.data.local.dao.EmergencyContraceptionDao
import com.dot.app.data.local.dao.ForumDao
import com.dot.app.data.local.dao.NotificationReminderDao
import com.dot.app.data.local.dao.UserAccountDao
import com.dot.app.data.local.dao.UserProfileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDotDatabase(
        @ApplicationContext context: Context
    ): DotDatabase {
        return Room.databaseBuilder(
            context,
            DotDatabase::class.java,
            DotDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideUserAccountDao(database: DotDatabase): UserAccountDao =
        database.userAccountDao()

    @Provides
    fun provideUserProfileDao(database: DotDatabase): UserProfileDao =
        database.userProfileDao()

    @Provides
    fun provideCycleDao(database: DotDatabase): CycleDao =
        database.cycleDao()

    @Provides
    fun provideDailyLogDao(database: DotDatabase): DailyLogDao =
        database.dailyLogDao()

    @Provides
    fun provideEmergencyContraceptionDao(database: DotDatabase): EmergencyContraceptionDao =
        database.emergencyContraceptionDao()

    @Provides
    fun provideNotificationReminderDao(database: DotDatabase): NotificationReminderDao =
        database.notificationReminderDao()

    @Provides
    fun provideForumDao(database: DotDatabase): ForumDao =
        database.forumDao()
}
