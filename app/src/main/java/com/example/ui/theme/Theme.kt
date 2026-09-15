package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val WriteBotColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = DarkBackground,
    primaryContainer = DarkSurfaceElevated,
    onPrimaryContainer = NeonCyan,
    secondary = StepperGreen,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = StepperGreen,
    tertiary = ServoAmber,
    onTertiary = DarkBackground,
    error = EmergencyRed,
    onError = TextPrimary,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // WriteBot uses a specialized dark technical interface designed for plotter controls
    MaterialTheme(
        colorScheme = WriteBotColorScheme,
        typography = Typography,
        content = content
    )
}

