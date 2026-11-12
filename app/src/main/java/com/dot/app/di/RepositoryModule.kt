package com.dot.app.di

import com.dot.app.data.repository.CycleRepositoryImpl
import com.dot.app.data.repository.DailyLogRepositoryImpl
import com.dot.app.data.repository.EmergencyContraceptionRepositoryImpl
import com.dot.app.data.repository.UserProfileRepositoryImpl
import com.dot.app.domain.repository.CycleRepository
import com.dot.app.domain.repository.DailyLogRepository
import com.dot.app.domain.repository.EmergencyContraceptionRepository
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
}
