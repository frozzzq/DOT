package com.dot.app.data.repository

import android.content.SharedPreferences
import android.util.Base64
import com.dot.app.data.local.dao.UserAccountDao
import com.dot.app.data.local.entity.UserAccountEntity
import com.dot.app.data.mapper.toDomain
import com.dot.app.domain.model.UserAccount
import com.dot.app.domain.repository.AuthRepository
import com.dot.app.domain.repository.AuthResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación local segura de autenticación con contraseñas con sal + SHA-256
 * y sesión cifrada mediante EncryptedSharedPreferences (RNF-01, RNF-04).
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val userAccountDao: UserAccountDao,
    private val securePrefs: SharedPreferences
) : AuthRepository {

    private val currentAccountFlow = MutableStateFlow<UserAccount?>(readSavedSession())

    override fun observeCurrentAccount(): Flow<UserAccount?> = currentAccountFlow.asStateFlow()

    override suspend fun getCurrentAccount(): UserAccount? {
        val cached = currentAccountFlow.value
        if (cached != null) return cached
        val restored = readSavedSession()
        currentAccountFlow.value = restored
        return restored
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String
    ): AuthResult {
        val cleanName = name.trim()
        val cleanEmail = email.trim().lowercase()

        if (cleanName.length < 2) {
            return AuthResult.Error("Escribe tu nombre o apodo (mínimo 2 caracteres).")
        }
        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            return AuthResult.Error("Ingresa un correo electrónico válido.")
        }
        if (password.length < 6) {
            return AuthResult.Error("La contraseña debe tener al menos 6 caracteres.")
        }

        val existing = userAccountDao.findByEmail(cleanEmail)
        if (existing != null) {
            return AuthResult.Error("Ya existe una cuenta registrada con este correo. Inicia sesión.")
        }

        val salt = generateSalt()
        val hash = hashPassword(password, salt)

        val insertedId = userAccountDao.insertAccount(
            UserAccountEntity(
                name = cleanName,
                email = cleanEmail,
                passwordHash = hash,
                passwordSalt = salt
            )
        )

        val account = UserAccount(id = insertedId, name = cleanName, email = cleanEmail)
        saveSession(account)
        currentAccountFlow.value = account
        return AuthResult.Success(account)
    }

    override suspend fun login(email: String, password: String): AuthResult {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank() || password.isBlank()) {
            return AuthResult.Error("Completa tu correo y contraseña para entrar.")
        }

        val entity = userAccountDao.findByEmail(cleanEmail)
            ?: return AuthResult.Error("No encontramos una cuenta con ese correo. Puedes crear una nueva.")

        val candidateHash = hashPassword(password, entity.passwordSalt)
        if (candidateHash != entity.passwordHash) {
            return AuthResult.Error("Contraseña incorrecta. Intenta nuevamente.")
        }

        val account = entity.toDomain()
        saveSession(account)
        currentAccountFlow.value = account
        return AuthResult.Success(account)
    }

    override suspend fun logout() {
        securePrefs.edit()
            .remove(KEY_SESSION_ID)
            .remove(KEY_SESSION_NAME)
            .remove(KEY_SESSION_EMAIL)
            .apply()
        currentAccountFlow.value = null
    }

    private fun saveSession(account: UserAccount) {
        securePrefs.edit()
            .putLong(KEY_SESSION_ID, account.id)
            .putString(KEY_SESSION_NAME, account.name)
            .putString(KEY_SESSION_EMAIL, account.email)
            .apply()
    }

    private fun readSavedSession(): UserAccount? {
        val id = securePrefs.getLong(KEY_SESSION_ID, -1L)
        val name = securePrefs.getString(KEY_SESSION_NAME, null)
        val email = securePrefs.getString(KEY_SESSION_EMAIL, null)
        return if (id > 0L && !name.isNullOrBlank() && !email.isNullOrBlank()) {
            UserAccount(id = id, name = name, email = email)
        } else {
            null
        }
    }

    private fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun hashPassword(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest("$salt:$password".toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    companion object {
        private const val KEY_SESSION_ID = "dot_session_account_id"
        private const val KEY_SESSION_NAME = "dot_session_account_name"
        private const val KEY_SESSION_EMAIL = "dot_session_account_email"
    }
}
