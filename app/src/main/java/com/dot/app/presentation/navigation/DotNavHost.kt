package com.dot.app.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.dot.app.presentation.auth.AuthScreen
import com.dot.app.presentation.calendar.CycleCalendarScreen
import com.dot.app.presentation.components.DotBottomNavBar
import com.dot.app.presentation.components.DotMainTab
import com.dot.app.presentation.cycles.CyclesScreen
import com.dot.app.presentation.daily_log.DailyLogScreen
import com.dot.app.presentation.emergency.EmergencyContraceptionScreen
import com.dot.app.presentation.forum.ForumScreen
import com.dot.app.presentation.home.HomeDashboardScreen
import com.dot.app.presentation.notifications.NotificationsScreen
import com.dot.app.presentation.onboarding.OnboardingScreen
import com.dot.app.presentation.theme.DotBlushCanvas
import com.dot.app.presentation.theme.DotRoseStrong
import com.dot.app.presentation.tips.TipsScreen
import java.time.LocalDate

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
            CircularProgressIndicator(color = DotRoseStrong)
        }
        return
    }

    val initialRoute = remember {
        when (rootState) {
            is RootNavigationState.ReadyForMain -> DotDestination.MainShell.route
            is RootNavigationState.RequireOnboarding -> DotDestination.Onboarding.route
            else -> DotDestination.Auth.route
        }
    }

    NavHost(
        navController = navController,
        startDestination = initialRoute,
        modifier = modifier
    ) {
        composable(route = DotDestination.Auth.route) {
            AuthScreen(
                onNavigateToOnboarding = {
                    navController.navigate(DotDestination.Onboarding.route) {
                        popUpTo(DotDestination.Auth.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToHome = {
                    navController.navigate(DotDestination.MainShell.route) {
                        popUpTo(DotDestination.Auth.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = DotDestination.Onboarding.route) {
            OnboardingScreen(
                onOnboardingFinished = {
                    navController.navigate(DotDestination.MainShell.route) {
                        popUpTo(DotDestination.Onboarding.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = DotDestination.MainShell.route) {
            DotMainShell(
                onOpenCalendar = { navController.navigate(DotDestination.Calendar.route) },
                onOpenNotifications = { navController.navigate(DotDestination.Notifications.route) },
                onOpenDailyLog = { date -> navController.navigate(DotDestination.DailyLog.createRoute(date)) },
                onOpenEmergency = { navController.navigate(DotDestination.EmergencyContraception.route) }
            )
        }

        composable(route = DotDestination.Calendar.route) {
            CycleCalendarScreen(
                onNavigateBack = { navController.popBackStack() },
                onEditDate = { selectedDate ->
                    navController.navigate(DotDestination.DailyLog.createRoute(selectedDate))
                }
            )
        }

        composable(route = DotDestination.Notifications.route) {
            NotificationsScreen(
                onNavigateBack = { navController.popBackStack() },
                onLoggedOut = {
                    navController.navigate(DotDestination.Auth.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
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
                onNavigateBack = { navController.popBackStack() },
                onOpenEmergencyModule = {
                    navController.navigate(DotDestination.EmergencyContraception.route)
                }
            )
        }

        composable(route = DotDestination.EmergencyContraception.route) {
            EmergencyContraceptionScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun DotMainShell(
    onOpenCalendar: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenDailyLog: (LocalDate?) -> Unit,
    onOpenEmergency: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(DotMainTab.TODAY) }

    Scaffold(
        containerColor = DotBlushCanvas,
        bottomBar = {
            DotBottomNavBar(
                currentTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                DotMainTab.TODAY -> HomeDashboardScreen(
                    onOpenCalendar = onOpenCalendar,
                    onOpenNotifications = onOpenNotifications,
                    onNavigateToDailyLog = onOpenDailyLog,
                    onNavigateToEmergencyContraception = onOpenEmergency
                )

                DotMainTab.TIPS -> TipsScreen()

                DotMainTab.FORUM -> ForumScreen()

                DotMainTab.CYCLES -> CyclesScreen()
            }
        }
    }
}
