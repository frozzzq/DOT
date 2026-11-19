package com.dot.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dot.app.domain.repository.AuthRepository
import com.dot.app.domain.repository.AuthResult
import com.dot.app.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AuthMode {
    LOGIN,
    REGISTER
}

sealed interface AuthNavigationTarget {
    data object None : AuthNavigationTarget
    data object GoToOnboarding : AuthNavigationTarget
    data object GoToHome : AuthNavigationTarget
}

data class AuthUiState(
    val mode: AuthMode = AuthMode.LOGIN,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val navigationTarget: AuthNavigationTarget = AuthNavigationTarget.None
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onModeSelected(mode: AuthMode) {
        _uiState.update { it.copy(mode = mode, errorMessage = null) }
    }

    fun onNameChanged(value: String) {
        _uiState.update { it.copy(name = value, errorMessage = null) }
    }

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value, errorMessage = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun submit() {
        val current = _uiState.value
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch(Dispatchers.IO) {
            val result = when (current.mode) {
                AuthMode.LOGIN -> authRepository.login(current.email, current.password)
                AuthMode.REGISTER -> authRepository.register(current.name, current.email, current.password)
            }

            when (result) {
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
                is AuthResult.Success -> {
                    val existingProfile = userProfileRepository.getUserProfile()
                    val destination = if (existingProfile != null && existingProfile.isOnboardingCompleted) {
                        AuthNavigationTarget.GoToHome
                    } else {
                        AuthNavigationTarget.GoToOnboarding
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            navigationTarget = destination
                        )
                    }
                }
            }
        }
    }
}
