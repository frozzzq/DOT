package com.dot.app.domain.repository

import com.dot.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    fun observeUserProfile(): Flow<UserProfile?>
    suspend fun getUserProfile(): UserProfile?
    suspend fun saveOrUpdateUserProfile(profile: UserProfile)
}
