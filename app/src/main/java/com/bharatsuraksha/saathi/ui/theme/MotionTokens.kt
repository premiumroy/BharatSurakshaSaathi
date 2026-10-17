package com.bharatsuraksha.saathi.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring

/** Suraksha Jyoti motion vocabulary. Every animation reads its numbers here. */
object MotionTokens {

    // durations (ms)
    const val Fast = 120
    const val Quick = 200
    const val Medium = 320
    const val Slow = 520

    /** Stagger between siblings in one entrance. */
    const val Stagger = 70

    /** The hero scan is held for at least this long. */
    const val HeroScanMin = 800

    /** The whole Result choreography. */
    const val ResultBeat = 1600

    /** Entrance / exit easing. */
    val Entrance: Easing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)
    val Exit: Easing = CubicBezierEasing(0.4f, 0f, 1f, 1f)

    /** Press feedback: a quick spring to 0.97. */
    val Press: SpringSpec<Float> = spring(dampingRatio = 0.7f, stiffness = 400f)

    /** A soft settle used when a value lands (score, gauge). */
    val Settle: SpringSpec<Float> = spring(dampingRatio = 0.55f, stiffness = 160f)
}

/** Kept as a top-level name so existing call sites read naturally. */
val SaathiEase: Easing = MotionTokens.Entrance
