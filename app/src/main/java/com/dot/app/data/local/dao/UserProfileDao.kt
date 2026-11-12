package com.dot.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.dot.app.data.local.entity.UserProfileEntity
import com.dot.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Query("SELECT * FROM user_profile WHERE id = :profileId LIMIT 1")
    fun observeUserProfile(profileId: Int = UserProfile.SINGLETON_PROFILE_ID): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = :profileId LIMIT 1")
    suspend fun getUserProfile(profileId: Int = UserProfile.SINGLETON_PROFILE_ID): UserProfileEntity?

    @Upsert
    suspend fun upsertUserProfile(profile: UserProfileEntity)
}
