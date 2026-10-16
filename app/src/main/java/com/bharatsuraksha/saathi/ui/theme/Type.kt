package com.bharatsuraksha.saathi.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.unit.sp

/** Normal, readable defaults. */
val SaathiTypography = Typography()

/**
 * Easy Mode: everything noticeably bigger for elderly users. Applied by
 * [SaathiTheme] when Easy Mode is on.
 */
val EasyTypography = Typography(
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
