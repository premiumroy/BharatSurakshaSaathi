package com.bharatsuraksha.saathi.ui.theme

import android.app.ActivityManager
import android.content.Context
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

/** The five states the guardian lamp can be in. */
enum class JyotiState { Idle, Scanning, Clear, Careful, Dangerous }

/** Fewer embers on a low-RAM device. */
@Composable
fun rememberLowRam(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        am?.isLowRamDevice ?: false
    }
}

/**
 * SURAKSHA JYOTI — the guardian lamp.
 *
 * A jaali-lattice shield holding a diya flame. The flame flickers with layered
 * sine noise and is trailed by embers; a 24-segment lattice ring reports state.
 * Two infinite animations at most (flicker + embers), both off when motion is
 * disabled.
 */
@Composable
fun JyotiHero(
    state: JyotiState,
    modifier: Modifier = Modifier,
    motion: Boolean = true,
    lowRam: Boolean = false,
    scanProgress: Float = 0f,
    parallax: Offset = Offset.Zero
) {
    val tint = when (state) {
        JyotiState.Idle, JyotiState.Scanning -> Saffron
        JyotiState.Clear -> Neem
        JyotiState.Careful -> Haldi
        JyotiState.Dangerous -> Sindoor
    }

    val transition = rememberInfiniteTransition(label = "jyoti")
    val flickerRaw by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing)),
        label = "flicker"
    )
    val emberRaw by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing)),
        label = "embers"
    )
    val flicker = if (motion) flickerRaw else 0.35f
    val ember = if (motion) emberRaw else 0.15f

    val leanTarget = when (state) {
        JyotiState.Scanning -> 1f
        JyotiState.Dangerous -> 1f
        JyotiState.Careful -> 0.55f
        else -> 0f
    }
    val lean by animateFloatAsState(
        targetValue = leanTarget,
        animationSpec = tween(MotionTokens.Medium, easing = MotionTokens.Entrance),
        label = "lean"
    )

    Box(
        modifier.graphicsLayer {
            translationX = parallax.x * 10.dp.toPx()
            translationY = parallax.y * 6.dp.toPx()
        }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawJyoti(
                state = state,
                tint = tint,
                flicker = flicker,
                ember = ember,
                lean = lean,
                scanProgress = scanProgress,
                emberCount = if (lowRam) 6 else 14,
                animated = motion
            )
        }
    }
}

private fun DrawScope.drawJyoti(
    state: JyotiState,
    tint: Color,
    flicker: Float,
    ember: Float,
    lean: Float,
    scanProgress: Float,
    emberCount: Int,
    animated: Boolean
) {
    val c = center
    val r = size.minDimension / 2f

    // 1. soft radial glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(tint.copy(alpha = 0.18f + 0.07f * lean), Color.Transparent),
            center = c,
            radius = r * 1.08f
        ),
        radius = r * 1.08f,
        center = c
    )

    // 2. the 24-segment lattice ring
    val segments = 24
    val lit = when (state) {
        JyotiState.Idle -> 0
        JyotiState.Scanning -> (scanProgress.coerceIn(0f, 1f) * segments).toInt()
        else -> segments
    }
    val ringR = r * 0.88f
    val step = 360f / segments
    for (i in 0 until segments) {
        val isLit = i < lit
        drawArc(
            color = if (isLit) tint else tint.copy(alpha = 0.13f),
            startAngle = i * step + step * 0.22f,
            sweepAngle = step * 0.56f,
            useCenter = false,
            topLeft = Offset(c.x - ringR, c.y - ringR),
            size = Size(ringR * 2f, ringR * 2f),
            style = Stroke(width = (if (isLit) 2.4f else 1.1f).dp.toPx(), cap = StrokeCap.Round)
        )
    }

    // 3. the lattice shield
    val halfW = r * 0.72f
    val top = c.y - r * 0.80f
    val bot = c.y + r * 0.90f
    val shield = Path().apply {
        moveTo(c.x, top)
        lineTo(c.x + halfW, top + r * 0.20f)
        lineTo(c.x + halfW, c.y + r * 0.06f)
        quadraticBezierTo(c.x + halfW, bot - r * 0.20f, c.x, bot)
        quadraticBezierTo(c.x - halfW, bot - r * 0.20f, c.x - halfW, c.y + r * 0.06f)
        lineTo(c.x - halfW, top + r * 0.20f)
        close()
    }
    drawPath(shield, tint.copy(alpha = 0.09f))
    drawPath(shield, tint.copy(alpha = 0.85f), style = Stroke(width = 2.dp.toPx()))
    for (k in -2..2) {
        val x = c.x + k * (halfW * 0.36f)
        drawLine(tint.copy(alpha = 0.15f), Offset(x, top + r * 0.34f), Offset(x, bot - r * 0.34f), 1.dp.toPx())
    }

    // 4. the diya flame, flickering with layered sine noise
    val n1 = sin(flicker * 2.0 * PI * 3.0).toFloat()
    val n2 = sin(flicker * 2.0 * PI * 7.0 + 1.3).toFloat()
    val n3 = sin(flicker * 2.0 * PI * 11.0 + 2.1).toFloat()
    val heightFactor = 0.88f + 0.05f * n1 + 0.03f * n2 + 0.02f * n3
    val sway = 0.09f * n1 + 0.05f * n2
    val tremble = if (state == JyotiState.Dangerous && animated) 0.05f * sin(flicker * 2.0 * PI * 16.0).toFloat() else 0f
    val flameCx = c.x + r * (sway + lean * 0.14f + tremble)
    val baseY = c.y + r * 0.36f
    val flameH = r * 0.92f * heightFactor
    val flameW = r * 0.30f

    val flame = Path().apply {
        moveTo(flameCx, baseY - flameH)
        cubicTo(
            flameCx + flameW, baseY - flameH * 0.54f,
            flameCx + flameW * 0.72f, baseY - flameH * 0.12f,
            flameCx, baseY
        )
        cubicTo(
            flameCx - flameW * 0.72f, baseY - flameH * 0.12f,
            flameCx - flameW, baseY - flameH * 0.54f,
            flameCx, baseY - flameH
        )
        close()
    }
    drawPath(
        path = flame,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFFFE6B8), tint),
            startY = baseY - flameH,
            endY = baseY
        )
    )

    // the bowl
    val bowl = Path().apply {
        moveTo(flameCx - r * 0.40f, baseY + r * 0.06f)
        quadraticBezierTo(flameCx, baseY + r * 0.40f, flameCx + r * 0.40f, baseY + r * 0.06f)
    }
    drawPath(bowl, tint.copy(alpha = 0.9f), style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))

    // 5. embers
    for (i in 0 until emberCount) {
        val phase = (ember + i.toFloat() / emberCount) % 1f
        val ey = baseY - r * 0.18f - phase * r * 1.15f
        val ex = flameCx + sin(phase * 6.0 + i * 1.7).toFloat() * r * 0.26f
        drawCircle(
            color = tint.copy(alpha = (1f - phase) * 0.8f),
            radius = (1.1f + 1.8f * (1f - phase)).dp.toPx(),
            center = Offset(ex, ey)
        )
    }
}

/** Dangerous: two calm pulses along the screen edges. */
@Composable
fun DangerEdgePulse(motion: Boolean, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "edge")
    val raw by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "edgePulse"
    )
    val p = if (motion) raw else 0.5f
    Canvas(modifier.fillMaxSize()) {
        val w = 5.dp.toPx()
        val alpha = 0.18f + 0.30f * p
        drawRect(
            brush = Brush.horizontalGradient(listOf(Sindoor.copy(alpha = alpha), Color.Transparent)),
            topLeft = Offset(0f, 0f),
            size = Size(w * 2.2f, size.height)
        )
        drawRect(
            brush = Brush.horizontalGradient(listOf(Color.Transparent, Sindoor.copy(alpha = alpha))),
            topLeft = Offset(size.width - w * 2.2f, 0f),
            size = Size(w * 2.2f, size.height)
        )
    }
}

/** Dangerous: two calm pulses drawn along the screen edges, on top of content. */
@Composable
fun Modifier.dangerEdgePulse(motion: Boolean): Modifier {
    val transition = rememberInfiniteTransition(label = "edge")
    val raw by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "edgePulse"
    )
    val p = if (motion) raw else 0.5f
    return this.drawWithContent {
        drawContent()
        val w = 6.dp.toPx()
        val alpha = 0.16f + 0.30f * p
        drawRect(
            brush = Brush.horizontalGradient(listOf(Sindoor.copy(alpha = alpha), Color.Transparent)),
            topLeft = Offset(0f, 0f),
            size = Size(w * 2.4f, size.height)
        )
        drawRect(
            brush = Brush.horizontalGradient(listOf(Color.Transparent, Sindoor.copy(alpha = alpha))),
            topLeft = Offset(size.width - w * 2.4f, 0f),
            size = Size(w * 2.4f, size.height)
        )
    }
}
