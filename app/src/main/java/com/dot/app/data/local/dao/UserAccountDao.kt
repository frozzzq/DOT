package com.dot.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dot.app.data.local.entity.UserAccountEntity

@Dao
interface UserAccountDao {

    @Query("SELECT * FROM user_accounts WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UserAccountEntity?

    @Query("SELECT * FROM user_accounts WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): UserAccountEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAccount(account: UserAccountEntity): Long
}
