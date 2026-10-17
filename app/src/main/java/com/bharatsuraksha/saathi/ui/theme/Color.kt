package com.bharatsuraksha.saathi.ui.theme

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------- identity
// Dark: "ink" — the primary identity.
val Ink = Color(0xFF12101F)
val InkSurface = Color(0xFF1C1932)
val InkSurfaceHigh = Color(0xFF26224A)
val InkText = Color(0xFFF4EFE6)
val InkMuted = Color(0xFFA59FB8)

// Light: "paper".
val Paper = Color(0xFFFBF4E8)
val PaperSurface = Color(0xFFFFFFFF)
val PaperText = Color(0xFF1B1730)
val PaperMuted = Color(0xFF6B6580)

// Accent and verdicts.
val Saffron = Color(0xFFFF8A1F)
val Neem = Color(0xFF1FA37A)
val Haldi = Color(0xFFF2B632)
val Sindoor = Color(0xFFE5383B)

// Small-text variants that clear WCAG AA on their backgrounds.
val NeemInk = Color(0xFF14785A)
val HaldiInk = Color(0xFF8A6100)
val SindoorInk = Color(0xFFC4232A)

// Soft containers for verdict surfaces.
val NeemTint = Color(0xFFE4F4EE)
val HaldiTint = Color(0xFFFDF2D8)
val SindoorTint = Color(0xFFFCE7E8)
val NeemTintDark = Color(0xFF14312A)
val HaldiTintDark = Color(0xFF33290F)
val SindoorTintDark = Color(0xFF3A1A1D)

// Hairline lattice borders.
val LatticeOnDark = Color(0x1FF4EFE6)
val LatticeOnLight = Color(0x141B1730)

// ------------------------------------------------- legacy aliases (kept so
// every existing screen keeps compiling and stays on-identity).
val BrandGreen = InkSurfaceHigh          // app-bar / header band
val BrandGreenDark = Ink
val BrandSaffron = Saffron
val BrandGold = Saffron

val SurfaceCream = Paper
val SurfaceIvory = PaperSurface
val SurfaceSand = Color(0xFFF2E8D8)
val OutlineGold = Saffron
val BrandIndigo = Ink
val BrandIndigoSoft = InkSurfaceHigh

// Verdicts — the big colour is the pure token; the small-text variants are
// darkened so body copy passes contrast (see DESIGN.md).
val SafeGreen = Neem
val SafeGreenBg = NeemTint
val CarefulAmber = HaldiInk
val CarefulAmberBg = HaldiTint
val DangerRed = Sindoor
val DangerRedBg = SindoorTint

val TextPrimary = PaperText
val TextSecondary = PaperMuted
