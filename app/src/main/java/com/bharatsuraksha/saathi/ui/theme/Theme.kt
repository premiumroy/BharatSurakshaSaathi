package com.bharatsuraksha.saathi.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Softly rounded, generous corners for a premium, modern feel. */
private val SaathiShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

private val LightColors = lightColorScheme(
    primary = BrandGreen,
    onPrimary = Color.White,
    primaryContainer = BrandGreenDark,
    onPrimaryContainer = Color.White,
    secondary = BrandSaffron,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE3CC),
    onSecondaryContainer = Color(0xFF4A2100),
    tertiary = BrandGold,
    onTertiary = Color(0xFF2A1F00),
    background = SurfaceCream,
    onBackground = TextPrimary,
    surface = SurfaceIvory,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceSand,
    onSurfaceVariant = TextSecondary,
    outline = OutlineGold,
    error = DangerRed,
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF6FD3AC),
    onPrimary = Color(0xFF00382A),
    primaryContainer = Color(0xFF0A4F39),
    onPrimaryContainer = Color(0xFFB9F0D6),
    secondary = BrandSaffron,
    onSecondary = Color(0xFF3A1B00),
    tertiary = Color(0xFFE8C766),
    onTertiary = Color(0xFF3A2E00),
    background = Color(0xFF101322),
    onBackground = Color(0xFFECE6DA),
    surface = Color(0xFF191D31),
    onSurface = Color(0xFFECE6DA),
    outline = Color(0xFF8A7B4E),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
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
        shapes = SaathiShapes,
        content = content
    )
}
