package com.dot.app.di

import com.dot.app.data.repository.AuthRepositoryImpl
import com.dot.app.data.repository.CycleRepositoryImpl
import com.dot.app.data.repository.DailyLogRepositoryImpl
import com.dot.app.data.repository.EmergencyContraceptionRepositoryImpl
import com.dot.app.data.repository.ForumRepositoryImpl
import com.dot.app.data.repository.HealthArticleRepositoryImpl
import com.dot.app.data.repository.NotificationReminderRepositoryImpl
import com.dot.app.data.repository.UserProfileRepositoryImpl
import com.dot.app.domain.repository.AuthRepository
import com.dot.app.domain.repository.CycleRepository
import com.dot.app.domain.repository.DailyLogRepository
import com.dot.app.domain.repository.EmergencyContraceptionRepository
import com.dot.app.domain.repository.ForumRepository
import com.dot.app.domain.repository.HealthArticleRepository
import com.dot.app.domain.repository.NotificationReminderRepository
import com.dot.app.domain.repository.UserProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(
        impl: UserProfileRepositoryImpl
    ): UserProfileRepository

    @Binds
    @Singleton
    abstract fun bindCycleRepository(
        impl: CycleRepositoryImpl
    ): CycleRepository

    @Binds
    @Singleton
    abstract fun bindDailyLogRepository(
        impl: DailyLogRepositoryImpl
    ): DailyLogRepository

    @Binds
    @Singleton
    abstract fun bindEmergencyContraceptionRepository(
        impl: EmergencyContraceptionRepositoryImpl
    ): EmergencyContraceptionRepository

    @Binds
    @Singleton
    abstract fun bindNotificationReminderRepository(
        impl: NotificationReminderRepositoryImpl
    ): NotificationReminderRepository

    @Binds
    @Singleton
    abstract fun bindForumRepository(
        impl: ForumRepositoryImpl
    ): ForumRepository

    @Binds
    @Singleton
    abstract fun bindHealthArticleRepository(
        impl: HealthArticleRepositoryImpl
    ): HealthArticleRepository
}
