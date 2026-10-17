package com.bharatsuraksha.saathi.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Mixed radii: the hero card keeps two sharp-ish corners so the layout reads as
 * drawn by hand, not stamped from a grid. Buttons are fully pill.
 */
private val SaathiShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(topStart = 28.dp, bottomEnd = 28.dp, topEnd = 8.dp, bottomStart = 8.dp),
    large = RoundedCornerShape(topStart = 28.dp, bottomEnd = 28.dp, topEnd = 8.dp, bottomStart = 8.dp),
    extraLarge = RoundedCornerShape(topStart = 36.dp, bottomEnd = 36.dp, topEnd = 8.dp, bottomStart = 8.dp)
)

private val LightColors = lightColorScheme(
    primary = Saffron,
    onPrimary = Color(0xFF2A1600),
    primaryContainer = Color(0xFFFFE6CC),
    onPrimaryContainer = Color(0xFF4A2400),
    secondary = Neem,
    onSecondary = Color.White,
    secondaryContainer = NeemTint,
    onSecondaryContainer = NeemInk,
    tertiary = Haldi,
    onTertiary = Color(0xFF2A1F00),
    // Transparent so the Canvas atmosphere (jaali + glow) shows through every screen.
    background = Color.Transparent,
    onBackground = PaperText,
    surface = PaperSurface,
    onSurface = PaperText,
    surfaceVariant = Color(0xFFF2E8D8),
    onSurfaceVariant = PaperMuted,
    outline = Color(0xFFD9CDB8),
    error = SindoorInk,
    onError = Color.White,
    errorContainer = SindoorTint,
    onErrorContainer = Color(0xFF3A0A0C)
)

private val DarkColors = darkColorScheme(
    primary = Saffron,
    onPrimary = Color(0xFF2A1600),
    primaryContainer = Color(0xFF3A2410),
    onPrimaryContainer = Color(0xFFFFDCB8),
    secondary = Neem,
    onSecondary = Color(0xFF04231A),
    secondaryContainer = NeemTintDark,
    onSecondaryContainer = Color(0xFFB6EAD6),
    tertiary = Haldi,
    onTertiary = Color(0xFF2A1F00),
    background = Color.Transparent,
    onBackground = InkText,
    surface = InkSurface,
    onSurface = InkText,
    surfaceVariant = InkSurfaceHigh,
    onSurfaceVariant = InkMuted,
    outline = Color(0xFF4A4568),
    error = Color(0xFFFF8A8C),
    onError = Color(0xFF3A0A0C),
    errorContainer = SindoorTintDark,
    onErrorContainer = Color(0xFFFFDAD9)
)

/** High-contrast light: pure black on white, for low vision. No decoration. */
private val HighContrastLight = lightColorScheme(
    primary = Color(0xFF000000), onPrimary = Color.White,
    primaryContainer = Color(0xFF000000), onPrimaryContainer = Color.White,
    secondary = Color(0xFF00337A), onSecondary = Color.White,
    background = Color.White, onBackground = Color.Black,
    surface = Color.White, onSurface = Color.Black,
    error = Color(0xFFB00020), onError = Color.White,
    outline = Color.Black
)

/** High-contrast dark: pure white on black. */
private val HighContrastDark = darkColorScheme(
    primary = Color.White, onPrimary = Color.Black,
    primaryContainer = Color.Black, onPrimaryContainer = Color.White,
    secondary = Color(0xFFFFD400), onSecondary = Color.Black,
    background = Color.Black, onBackground = Color.White,
    surface = Color.Black, onSurface = Color.White,
    error = Color(0xFFFF6E6E), onError = Color.Black,
    outline = Color.White
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
        shapes = SaathiShapes
    ) {
        if (highContrast) {
            // Accessibility mode: flat, no atmosphere, no motion.
            Box(Modifier.fillMaxSize().background(colors.background)) { content() }
        } else {
            Box(Modifier.fillMaxSize()) {
                JaaliAtmosphere()
                content()
            }
        }
    }
}
