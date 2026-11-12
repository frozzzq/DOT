package com.dot.app.presentation.navigation

import java.time.LocalDate

sealed class DotDestination(val route: String) {
    data object Onboarding : DotDestination("onboarding")
    data object HomeDashboard : DotDestination("home_dashboard")
    data object EmergencyContraception : DotDestination("emergency_contraception")
    data object DailyLog : DotDestination("daily_log?date={date}") {
        const val ARG_DATE = "date"
        fun createRoute(date: LocalDate? = null): String {
            return if (date != null) {
                "daily_log?date=$date"
            } else {
                "daily_log"
            }
        }
    }
}
