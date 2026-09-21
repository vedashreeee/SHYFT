package com.shyft.privacy.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF70F7F2),
    onPrimary = Color(0xFF003736),
    primaryContainer = Color(0xFF00504E),
    onPrimaryContainer = Color(0xFF70F7F2),
    secondary = Color(0xFFB0CCC9),
    onSecondary = Color(0xFF1B3533),
    background = Color(0xFF0F1414),
    surface = Color(0xFF191C1C),
    onBackground = Color(0xFFE0E3E2),
    onSurface = Color(0xFFE0E3E2)
)

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = TealOnPrimary,
    primaryContainer = TealContainer,
    onPrimaryContainer = TealOnContainer,
    secondary = SlateSecondary,
    secondaryContainer = SlateContainer,
    background = Color(0xFFF4FBF9),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF191C1C),
    onSurface = Color(0xFF191C1C)
)

@Composable
fun SHYFTTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
