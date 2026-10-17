package com.bharatsuraksha.saathi.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Serif for display/headlines (an editorial, "big brand" feel), clean sans for body. */
private val Display = FontFamily.Serif
private val Sans = FontFamily.SansSerif

private val Base = Typography()

/** Normal, readable defaults. */
val SaathiTypography = Base.copy(
    displayLarge = Base.displayLarge.copy(fontFamily = Display, fontWeight = FontWeight.Bold),
    displayMedium = Base.displayMedium.copy(fontFamily = Display, fontWeight = FontWeight.Bold),
    displaySmall = Base.displaySmall.copy(fontFamily = Display, fontWeight = FontWeight.Bold),
    headlineLarge = Base.headlineLarge.copy(fontFamily = Display, fontWeight = FontWeight.Bold),
    headlineMedium = Base.headlineMedium.copy(fontFamily = Display, fontWeight = FontWeight.Bold),
    headlineSmall = Base.headlineSmall.copy(fontFamily = Display, fontWeight = FontWeight.SemiBold),
    titleLarge = Base.titleLarge.copy(fontFamily = Display, fontWeight = FontWeight.Bold),
    titleMedium = Base.titleMedium.copy(fontFamily = Sans, fontWeight = FontWeight.SemiBold)
)

/**
 * Easy Mode: everything noticeably bigger for elderly users. Applied by
 * [SaathiTheme] when Easy Mode is on.
 */
val EasyTypography = SaathiTypography.copy(
    displayLarge = SaathiTypography.displayLarge.copy(fontSize = 62.sp),
    displayMedium = SaathiTypography.displayMedium.copy(fontSize = 50.sp),
    headlineLarge = SaathiTypography.headlineLarge.copy(fontSize = 40.sp),
    headlineMedium = SaathiTypography.headlineMedium.copy(fontSize = 34.sp),
    titleLarge = SaathiTypography.titleLarge.copy(fontSize = 28.sp),
    titleMedium = SaathiTypography.titleMedium.copy(fontSize = 24.sp),
    bodyLarge = SaathiTypography.bodyLarge.copy(fontSize = 24.sp, lineHeight = 34.sp),
    bodyMedium = SaathiTypography.bodyMedium.copy(fontSize = 21.sp, lineHeight = 30.sp),
    labelLarge = SaathiTypography.labelLarge.copy(fontSize = 22.sp)
)
