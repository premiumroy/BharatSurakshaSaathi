package com.bharatsuraksha.saathi.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.bharatsuraksha.saathi.R

/**
 * Yatra One is the display face (festive, unmistakably Indian); Mukta carries
 * body and headings and is Devanagari-friendly. Both are OFL and bundled in
 * res/font. If a file is missing the family falls back to the platform default.
 */
val DisplayFamily: FontFamily = FontFamily(
    Font(R.font.yatra_one, FontWeight.Normal),
    Font(R.font.yatra_one, FontWeight.Bold)
)

val BodyFamily: FontFamily = FontFamily(
    Font(R.font.mukta_regular, FontWeight.Normal),
    Font(R.font.mukta_semibold, FontWeight.SemiBold),
    Font(R.font.mukta_bold, FontWeight.Bold),
    Font(R.font.mukta_extrabold, FontWeight.ExtraBold)
)

/** Type scale, ratio ~1.25, with strong 800 vs 400 weight contrast. */
val SaathiTypography = Typography(
    displayLarge = TextStyle(fontFamily = DisplayFamily, fontWeight = FontWeight.Normal, fontSize = 56.sp, lineHeight = 62.sp),
    displayMedium = TextStyle(fontFamily = DisplayFamily, fontWeight = FontWeight.Normal, fontSize = 44.sp, lineHeight = 50.sp),
    displaySmall = TextStyle(fontFamily = DisplayFamily, fontWeight = FontWeight.Normal, fontSize = 35.sp, lineHeight = 42.sp),
    headlineLarge = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, lineHeight = 42.sp),
    headlineMedium = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.ExtraBold, fontSize = 27.sp, lineHeight = 34.sp),
    headlineSmall = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 24.sp),
    titleSmall = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Normal, fontSize = 17.sp, lineHeight = 26.sp),
    bodyMedium = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp),
    bodySmall = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 14.sp)
)

/** Easy Mode: same identity, bigger and calmer. */
val EasyTypography = SaathiTypography.copy(
    displayLarge = SaathiTypography.displayLarge.copy(fontSize = 60.sp),
    displayMedium = SaathiTypography.displayMedium.copy(fontSize = 48.sp),
    headlineLarge = SaathiTypography.headlineLarge.copy(fontSize = 40.sp),
    headlineMedium = SaathiTypography.headlineMedium.copy(fontSize = 33.sp),
    titleLarge = SaathiTypography.titleLarge.copy(fontSize = 27.sp),
    titleMedium = SaathiTypography.titleMedium.copy(fontSize = 24.sp),
    bodyLarge = SaathiTypography.bodyLarge.copy(fontSize = 24.sp, lineHeight = 34.sp),
    bodyMedium = SaathiTypography.bodyMedium.copy(fontSize = 21.sp, lineHeight = 30.sp),
    labelLarge = SaathiTypography.labelLarge.copy(fontSize = 22.sp)
)
