package com.dot.app.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dot.app.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface RootNavigationState {
    data object Loading : RootNavigationState
    data object RequireOnboarding : RootNavigationState
    data object ReadyForHome : RootNavigationState
}

/**
 * ViewModel de navegación raíz que evalúa si existe un perfil de usuaria guardado en Room:
 * - Si está vacío o no completado -> inicia en [DotDestination.Onboarding]
 * - Si ya existe -> inicia en [DotDestination.HomeDashboard]
 */
@HiltViewModel
class NavigationViewModel @Inject constructor(
    userProfileRepository: UserProfileRepository
) : ViewModel() {

    val navigationState: StateFlow<RootNavigationState> = userProfileRepository
        .observeUserProfile()
        .map { profile ->
            if (profile != null && profile.isOnboardingCompleted) {
                RootNavigationState.ReadyForHome
            } else {
                RootNavigationState.RequireOnboarding
            }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RootNavigationState.Loading
        )
}
