package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val HangisColorScheme = darkColorScheme(
    primary = HangisCyan,
    onPrimary = Color(0xFF00222A),
    primaryContainer = Color(0xFF004D5A),
    onPrimaryContainer = Color(0xFF70F4FF),
    secondary = HangisPurple,
    onSecondary = Color(0xFF26004D),
    secondaryContainer = HangisPurpleContainer,
    onSecondaryContainer = Color(0xFFE9D5FF),
    tertiary = HangisTeal,
    background = HangisBackground,
    onBackground = HangisTextPrimary,
    surface = HangisSurface,
    onSurface = HangisTextPrimary,
    surfaceVariant = HangisSurfaceVariant,
    onSurfaceVariant = HangisTextSecondary,
    outline = HangisBorderSubtle,
    error = HangisRed,
    onError = Color.White
)

@Composable
fun HangisWatchTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = HangisColorScheme,
        typography = Typography,
        content = content
    )
}

// Alias for compatibility
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    HangisWatchTheme(content = content)
}
