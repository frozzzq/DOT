package com.dot.app.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dot.app.domain.repository.AuthRepository
import com.dot.app.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface RootNavigationState {
    data object Loading : RootNavigationState
    data object RequireAuth : RootNavigationState
    data object RequireOnboarding : RootNavigationState
    data object ReadyForMain : RootNavigationState
}

/**
 * ViewModel raíz que controla el flujo de entrada:
 * 1. Sin sesión -> Pantalla de Iniciar sesión / Crear cuenta (Auth)
 * 2. Con sesión pero sin cuestionario completado -> Cuestionario inicial (Onboarding)
 * 3. Con sesión y cuestionario completado -> Pantalla principal con barra inferior ("Hoy")
 */
@HiltViewModel
class NavigationViewModel @Inject constructor(
    authRepository: AuthRepository,
    userProfileRepository: UserProfileRepository
) : ViewModel() {

    val navigationState: StateFlow<RootNavigationState> = combine(
        authRepository.observeCurrentAccount(),
        userProfileRepository.observeUserProfile()
    ) { account, profile ->
        when {
            account == null -> RootNavigationState.RequireAuth
            profile == null || !profile.isOnboardingCompleted -> RootNavigationState.RequireOnboarding
            else -> RootNavigationState.ReadyForMain
        }
    }
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RootNavigationState.Loading
        )
}
