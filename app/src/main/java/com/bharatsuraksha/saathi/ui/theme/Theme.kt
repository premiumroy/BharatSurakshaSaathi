package com.bharatsuraksha.saathi.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = BrandGreen,
    onPrimary = Color.White,
    primaryContainer = BrandGreenDark,
    secondary = BrandSaffron,
    onSecondary = Color.White,
    background = SurfaceCream,
    onBackground = TextPrimary,
    surface = Color.White,
    onSurface = TextPrimary,
    error = DangerRed,
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF63D3A6),
    secondary = BrandSaffron,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E)
)

/** High-contrast light: pure black on white, for low vision. */
private val HighContrastLight = lightColorScheme(
    primary = Color(0xFF000000),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF000000),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFF00337A),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF000000),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF000000),
    error = Color(0xFFB00020),
    onError = Color(0xFFFFFFFF),
    outline = Color(0xFF000000)
)

/** High-contrast dark: pure white on black. */
private val HighContrastDark = darkColorScheme(
    primary = Color(0xFFFFFFFF),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF000000),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFFFFD400),
    onSecondary = Color(0xFF000000),
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF000000),
    onSurface = Color(0xFFFFFFFF),
    error = Color(0xFFFF6E6E),
    onError = Color(0xFF000000),
    outline = Color(0xFFFFFFFF)
)

@Composable
fun SaathiTheme(
    easyMode: Boolean = false,
    highContrast: Boolean = false,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = when {
        highContrast && darkTheme -> HighContrastDark
        highContrast -> HighContrastLight
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(
        colorScheme = colors,
        typography = if (easyMode) EasyTypography else SaathiTypography,
        content = content
    )
}
