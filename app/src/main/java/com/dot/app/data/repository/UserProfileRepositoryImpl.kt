package com.dot.app.data.repository

import com.dot.app.data.local.dao.UserProfileDao
import com.dot.app.data.mapper.toDomain
import com.dot.app.data.mapper.toEntity
import com.dot.app.domain.model.UserProfile
import com.dot.app.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserProfileRepositoryImpl @Inject constructor(
    private val userProfileDao: UserProfileDao
) : UserProfileRepository {

    override fun observeUserProfile(): Flow<UserProfile?> =
        userProfileDao.observeUserProfile().map { it?.toDomain() }

    override suspend fun getUserProfile(): UserProfile? =
        userProfileDao.getUserProfile()?.toDomain()

    override suspend fun saveOrUpdateUserProfile(profile: UserProfile) {
        userProfileDao.upsertUserProfile(profile.toEntity())
    }
}
