package com.financeplanner.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF10B981),
    onPrimary = Color(0xFF002116),
    secondary = Color(0xFF0EA5E9),
    onSecondary = Color(0xFF001E2C),
    background = Color(0xFFF6F8FA),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE6F4EF),
    onSurfaceVariant = Color(0xFF0D261D),
    error = Color(0xFFEF4444)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF34D399),
    onPrimary = Color(0xFF002116),
    secondary = Color(0xFF38BDF8),
    onSecondary = Color(0xFF001E2C),
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFE2E8F0),
    surface = Color(0xFF111827),
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF0F2C23),
    onSurfaceVariant = Color(0xFFC7F4E2),
    error = Color(0xFFF87171)
)

@Composable
fun FinancePlannerTheme(content: @Composable () -> Unit) {
    val colorScheme = LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
