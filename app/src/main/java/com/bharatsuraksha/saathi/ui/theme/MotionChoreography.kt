package com.bharatsuraksha.saathi.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import com.bharatsuraksha.saathi.ui.session.CalmMode
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.animation.core.animateFloatAsState as animateFloatAsStateInk
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.hypot
import kotlin.math.max

/**
 * One orchestrated entrance: fade plus a small rise, offset per [index] by the
 * stagger token. Static when motion is off.
 */
@Composable
fun Modifier.entrance(index: Int, motion: Boolean, stepMillis: Int = MotionTokens.Stagger): Modifier {
    var shown by remember(motion) { mutableStateOf(!motion) }
    LaunchedEffect(motion) {
        if (motion) {
            delay((index * stepMillis).toLong())
            shown = true
        }
    }
    val p by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(MotionTokens.Medium + 140, easing = MotionTokens.Entrance),
        label = "entrance$index"
    )
    return this.graphicsLayer {
        alpha = p
        translationY = (1f - p) * 20.dp.toPx()
    }
}

/**
 * Every tappable element: a 0.97 press spring, a light haptic, a 48dp minimum
 * touch target, and a button role for TalkBack.
 */
fun Modifier.tappable(
    onClickLabel: String? = null,
    enabled: Boolean = true,
    motion: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val haptics = LocalHapticFeedback.current
    val scale by animateFloatAsState(
        targetValue = if (pressed && motion) 0.97f else 1f,
        animationSpec = MotionTokens.Press,
        label = "pressScale"
    )
    this
        .sizeIn(minWidth = MinTouch, minHeight = MinTouch)
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .clickable(
            interactionSource = interaction,
            indication = null,
            enabled = enabled,
            onClickLabel = onClickLabel,
            role = Role.Button,
            onClick = {
                if (enabled) {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            }
        )
}

/** A lattice ripple: one expanding hairline ring per tap. */
@Composable
fun Modifier.latticeRipple(
    rippleKey: Int,
    color: Color,
    motion: Boolean,
    shape: Shape = RoundedCornerShape(50)
): Modifier {
    var progress by remember { mutableStateOf(1f) }
    LaunchedEffect(rippleKey) {
        if (rippleKey > 0 && motion) {
            progress = 0f
            delay(MotionTokens.Slow.toLong())
            progress = 1f
        }
    }
    return this
        .clip(shape)
        .drawBehind {
            if (progress < 1f) {
                val r = max(size.width, size.height) * (0.2f + 0.9f * progress)
                drawCircle(
                    color = color.copy(alpha = (1f - progress) * 0.5f),
                    radius = r,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
}

/** Odometer-style number: rolls from 0 to [value]. */
@Composable
fun OdometerNumber(
    value: Int,
    motion: Boolean,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.displayMedium,
    color: Color = Color.Unspecified
) {
    val animated by animateFloatAsState(
        targetValue = value.toFloat(),
        animationSpec = if (motion) MotionTokens.Settle else tween(0),
        label = "odometer"
    )
    Text(
        text = if (motion) animated.toInt().toString() else value.toString(),
        style = style,
        color = color,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

/**
 * Ink-spread reveal: a circular wipe from [origin] that uncovers the content.
 * Used when moving from a tap into Result.
 */
@Composable
fun InkSpreadReveal(
    origin: Offset,
    motion: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var started by remember(motion) { mutableStateOf(!motion) }
    LaunchedEffect(motion) {
        if (motion) {
            delay(16)
            started = true
        }
    }
    val p by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(MotionTokens.Slow, easing = MotionTokens.Entrance),
        label = "inkSpread"
    )
    Box(
        modifier
            .fillMaxSize()
            .drawWithContent {
                if (p >= 1f) {
                    drawContent()
                } else {
                    val r = hypot(size.width, size.height) * p
                    val circle = Path().apply {
                        addOval(Rect(offset = Offset(origin.x - r, origin.y - r), size = Size(r * 2f, r * 2f)))
                    }
                    clipPath(circle) { this@drawWithContent.drawContent() }
                }
            }
    ) { content() }
}

/**
 * Ink spread: a circular wipe from [originFraction] that uncovers the content.
 * The signature move when a tap turns into the Result screen.
 */
@Composable
fun Modifier.inkSpread(
    motion: Boolean,
    originFraction: Offset = Offset(0.5f, 0.22f)
): Modifier {
    var started by remember(motion) { mutableStateOf(!motion) }
    LaunchedEffect(motion) {
        if (motion) {
            delay(16)
            started = true
        }
    }
    val p by animateFloatAsStateInk(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(MotionTokens.Slow, easing = MotionTokens.Entrance),
        label = "inkSpread"
    )
    return this.drawWithContent {
        if (p >= 1f) {
            drawContent()
        } else {
            val o = Offset(size.width * originFraction.x, size.height * originFraction.y)
            val r = hypot(size.width, size.height) * p
            clipPath(Path().apply { addOval(Rect(Offset(o.x - r, o.y - r), Size(r * 2f, r * 2f))) }) {
                this@drawWithContent.drawContent()
            }
        }
    }
}

/** True when the app should animate at all (system setting plus Calm mode). */
@Composable
fun motionAllowed(): Boolean {
    val calm by CalmMode.enabled.collectAsState()
    return !calm && !rememberReduceMotion()
}
