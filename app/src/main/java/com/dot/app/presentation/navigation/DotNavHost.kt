package com.dot.app.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dot.app.presentation.daily_log.DailyLogScreen
import com.dot.app.presentation.emergency.EmergencyContraceptionScreen
import com.dot.app.presentation.home.HomeDashboardScreen
import com.dot.app.presentation.onboarding.OnboardingScreen

@Composable
fun DotNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    navigationViewModel: NavigationViewModel = hiltViewModel()
) {
    val rootState by navigationViewModel.navigationState.collectAsStateWithLifecycle()

    if (rootState is RootNavigationState.Loading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    // Fijar el destino inicial una vez evaluado Room para evitar recrear el NavHost
    val initialRoute = remember {
        when (rootState) {
            is RootNavigationState.ReadyForHome -> DotDestination.HomeDashboard.route
            else -> DotDestination.Onboarding.route
        }
    }

    NavHost(
        navController = navController,
        startDestination = initialRoute,
        modifier = modifier
    ) {
        composable(route = DotDestination.Onboarding.route) {
            OnboardingScreen(
                onOnboardingFinished = {
                    navController.navigate(DotDestination.HomeDashboard.route) {
                        popUpTo(DotDestination.Onboarding.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = DotDestination.HomeDashboard.route) {
            HomeDashboardScreen(
                onNavigateToDailyLog = { selectedDate ->
                    navController.navigate(DotDestination.DailyLog.createRoute(selectedDate))
                },
                onNavigateToEmergencyContraception = {
                    navController.navigate(DotDestination.EmergencyContraception.route)
                }
            )
        }

        composable(
            route = DotDestination.DailyLog.route,
            arguments = listOf(
                navArgument(DotDestination.DailyLog.ARG_DATE) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            DailyLogScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = DotDestination.EmergencyContraception.route) {
            EmergencyContraceptionScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
