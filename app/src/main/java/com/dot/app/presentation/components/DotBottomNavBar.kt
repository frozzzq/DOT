package com.dot.app.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.dot.app.presentation.theme.DotBlushPink
import com.dot.app.presentation.theme.DotDeepSlate
import com.dot.app.presentation.theme.DotMutedText
import com.dot.app.presentation.theme.DotRoseStrong
import com.dot.app.presentation.theme.DotWhite

enum class DotMainTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    TODAY(
        route = "tab_today",
        label = "Hoy",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    TIPS(
        route = "tab_tips",
        label = "Consejos",
        selectedIcon = Icons.Filled.MenuBook,
        unselectedIcon = Icons.Outlined.MenuBook
    ),
    FORUM(
        route = "tab_forum",
        label = "Foro",
        selectedIcon = Icons.Filled.Forum,
        unselectedIcon = Icons.Outlined.Forum
    ),
    CYCLES(
        route = "tab_cycles",
        label = "Ciclos",
        selectedIcon = Icons.Filled.Analytics,
        unselectedIcon = Icons.Outlined.Analytics
    )
}

@Composable
fun DotBottomNavBar(
    currentTab: DotMainTab,
    onTabSelected: (DotMainTab) -> Unit
) {
    NavigationBar(
        containerColor = DotWhite,
        tonalElevation = 6.dp
    ) {
        DotMainTab.entries.forEach { tab ->
            val selected = tab == currentTab
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DotRoseStrong,
                    selectedTextColor = DotRoseStrong,
                    indicatorColor = DotBlushPink.copy(alpha = 0.65f),
                    unselectedIconColor = DotMutedText,
                    unselectedTextColor = DotDeepSlate.copy(alpha = 0.7f)
                )
            )
        }
    }
}
