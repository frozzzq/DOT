package com.dot.app.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dot.app.domain.model.ContraceptiveMethod
import com.dot.app.domain.model.Cycle
import com.dot.app.domain.model.CycleRegularity
import com.dot.app.domain.model.UserProfile
import com.dot.app.domain.repository.CycleRepository
import com.dot.app.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class OnboardingUiState(
    val lastPeriodStartDate: LocalDate = LocalDate.now().minusDays(14),
    val averagePeriodDuration: Int = 5,
    val averageCycleLength: Int = 28,
    val regularity: CycleRegularity = CycleRegularity.REGULAR,
    val contraceptiveMethod: ContraceptiveMethod = ContraceptiveMethod.NONE,
    val isSaving: Boolean = false,
    val isCompleted: Boolean = false,
    val validationMessage: String? = null
)

/**
 * ViewModel para el cuestionario inicial de Onboarding (RF-01).
 * Captura y valida la línea base fisiológica de la usuaria y persiste el perfil
 * junto con el ciclo inicial en Room de manera local (Offline-First, RNF-04).
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val cycleRepository: CycleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onLastPeriodDateChanged(newDate: LocalDate) {
        if (newDate.isAfter(LocalDate.now())) {
            _uiState.update {
                it.copy(validationMessage = "La fecha del último periodo no puede ser posterior al día de hoy.")
            }
            return
        }
        _uiState.update {
            it.copy(
                lastPeriodStartDate = newDate,
                validationMessage = null
            )
        }
    }

    fun onPeriodDurationChanged(days: Int) {
        val coerced = days.coerceIn(MIN_BLEEDING_DAYS, MAX_BLEEDING_DAYS)
        _uiState.update {
            it.copy(
                averagePeriodDuration = coerced,
                validationMessage = null
            )
        }
    }

    fun onCycleLengthChanged(days: Int) {
        val coerced = days.coerceIn(MIN_CYCLE_DAYS, MAX_CYCLE_DAYS)
        _uiState.update {
            it.copy(
                averageCycleLength = coerced,
                validationMessage = null
            )
        }
    }

    fun onRegularitySelected(regularity: CycleRegularity) {
        _uiState.update {
            it.copy(
                regularity = regularity,
                validationMessage = null
            )
        }
    }

    fun onContraceptiveMethodSelected(method: ContraceptiveMethod) {
        _uiState.update {
            it.copy(
                contraceptiveMethod = method,
                validationMessage = null
            )
        }
    }

    fun completeOnboarding() {
        val currentState = _uiState.value

        if (currentState.lastPeriodStartDate.isAfter(LocalDate.now())) {
            _uiState.update {
                it.copy(validationMessage = "Selecciona una fecha pasada o actual para tu último periodo.")
            }
            return
        }

        if (currentState.averagePeriodDuration !in MIN_BLEEDING_DAYS..MAX_BLEEDING_DAYS) {
            _uiState.update {
                it.copy(validationMessage = "La duración habitual del sangrado debe estar entre $MIN_BLEEDING_DAYS y $MAX_BLEEDING_DAYS días.")
            }
            return
        }

        if (currentState.averageCycleLength !in MIN_CYCLE_DAYS..MAX_CYCLE_DAYS) {
            _uiState.update {
                it.copy(validationMessage = "La duración promedio del ciclo debe estar entre $MIN_CYCLE_DAYS y $MAX_CYCLE_DAYS días.")
            }
            return
        }

        _uiState.update { it.copy(isSaving = true, validationMessage = null) }

        viewModelScope.launch(Dispatchers.IO) {
            val profile = UserProfile(
                lastPeriodStartDate = currentState.lastPeriodStartDate,
                averagePeriodDuration = currentState.averagePeriodDuration,
                averageCycleLength = currentState.averageCycleLength,
                regularity = currentState.regularity,
                contraceptiveMethod = currentState.contraceptiveMethod,
                isOnboardingCompleted = true
            )

            userProfileRepository.saveOrUpdateUserProfile(profile)

            // Crear el ciclo activo inicial si no existe
            val existingLatestCycle = cycleRepository.getLatestCycle()
            if (existingLatestCycle == null) {
                cycleRepository.upsertCycle(
                    Cycle(
                        startDate = currentState.lastPeriodStartDate,
                        bleedingDurationDays = currentState.averagePeriodDuration
                    )
                )
            }

            _uiState.update {
                it.copy(
                    isSaving = false,
                    isCompleted = true
                )
            }
        }
    }

    companion object {
        const val MIN_BLEEDING_DAYS = 1
        const val MAX_BLEEDING_DAYS = 12
        const val MIN_CYCLE_DAYS = 21
        const val MAX_CYCLE_DAYS = 45
    }
}
