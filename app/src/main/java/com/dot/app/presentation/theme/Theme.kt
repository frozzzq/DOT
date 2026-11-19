package com.dot.app.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DotPinkColorScheme = lightColorScheme(
    primary = DotRoseStrong,
    onPrimary = DotWhite,
    primaryContainer = DotBlushPink,
    onPrimaryContainer = DotInkBlack,

    secondary = DotSageGreen,
    onSecondary = DotInkBlack,
    secondaryContainer = DotSageSoft,
    onSecondaryContainer = DotInkBlack,

    tertiary = DotDustyBlue,
    onTertiary = DotInkBlack,
    tertiaryContainer = DotBlueSoft,
    onTertiaryContainer = DotDeepSlate,

    background = DotBlushCanvas,
    onBackground = DotInkBlack,

    surface = DotWhite,
    onSurface = DotInkBlack,
    surfaceVariant = DotBlushSurface,
    onSurfaceVariant = DotMutedText,

    outline = DotOutlineSoft,
    outlineVariant = DotDividerSoft,

    // Avisos calmados sin rojos alarmistas (RNF-03)
    error = DotRoseDark,
    onError = DotWhite,
    errorContainer = DotBlushPink,
    onErrorContainer = DotInkBlack
)

@Composable
fun DotTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DotPinkColorScheme,
        typography = DotTypography,
        content = content
    )
}
