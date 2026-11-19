package com.dot.app.domain.repository

import com.dot.app.domain.model.UserAccount
import kotlinx.coroutines.flow.Flow

sealed interface AuthResult {
    data class Success(val account: UserAccount) : AuthResult
    data class Error(val message: String) : AuthResult
}

interface AuthRepository {
    fun observeCurrentAccount(): Flow<UserAccount?>
    suspend fun getCurrentAccount(): UserAccount?
    suspend fun register(name: String, email: String, password: String): AuthResult
    suspend fun login(email: String, password: String): AuthResult
    suspend fun logout()
}
