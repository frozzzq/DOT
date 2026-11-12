package com.dot.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightEmpatheticColorScheme = lightColorScheme(
    primary = SoftTerracottaPrimary,
    onPrimary = OnSoftTerracottaPrimary,
    primaryContainer = SoftPeachPrimaryContainer,
    onPrimaryContainer = OnSoftPeachPrimaryContainer,
    secondary = CalmSageSecondary,
    onSecondary = OnCalmSageSecondary,
    secondaryContainer = SoftSageSecondaryContainer,
    onSecondaryContainer = OnSoftSageSecondaryContainer,
    tertiary = WarmLavenderTertiary,
    onTertiary = OnWarmLavenderTertiary,
    tertiaryContainer = SoftLavenderTertiaryContainer,
    onTertiaryContainer = OnSoftLavenderTertiaryContainer,
    background = WarmCreamBackground,
    onBackground = OnWarmCreamBackground,
    surface = WarmIvorySurface,
    onSurface = OnWarmIvorySurface,
    surfaceVariant = WarmSandSurfaceVariant,
    onSurfaceVariant = OnWarmSandSurfaceVariant,
    outline = SoftOutline,
    outlineVariant = SoftOutlineVariant,
    error = CalmAmberNotice,
    onError = OnCalmAmberNotice,
    errorContainer = CalmAmberNoticeContainer,
    onErrorContainer = OnCalmAmberNoticeContainer
)

private val DarkEmpatheticColorScheme = darkColorScheme(
    primary = DarkTerracottaPrimary,
    onPrimary = OnDarkTerracottaPrimary,
    primaryContainer = DarkTerracottaPrimaryContainer,
    onPrimaryContainer = OnDarkTerracottaPrimaryContainer,
    secondary = DarkSageSecondary,
    onSecondary = OnDarkSageSecondary,
    secondaryContainer = DarkSageSecondaryContainer,
    onSecondaryContainer = OnDarkSageSecondaryContainer,
    tertiary = DarkLavenderTertiary,
    onTertiary = OnDarkLavenderTertiary,
    tertiaryContainer = DarkLavenderTertiaryContainer,
    onTertiaryContainer = OnDarkLavenderTertiaryContainer,
    background = DarkWarmBackground,
    onBackground = OnDarkWarmBackground,
    surface = DarkWarmSurface,
    onSurface = OnDarkWarmSurface,
    surfaceVariant = DarkWarmSurfaceVariant,
    onSurfaceVariant = OnDarkWarmSurfaceVariant,
    error = DarkCalmAmberNotice,
    onError = OnDarkCalmAmberNotice,
    errorContainer = DarkCalmAmberNoticeContainer,
    onErrorContainer = OnDarkCalmAmberNoticeContainer
)

@Composable
fun DotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkEmpatheticColorScheme else LightEmpatheticColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DotTypography,
        content = content
    )
}
