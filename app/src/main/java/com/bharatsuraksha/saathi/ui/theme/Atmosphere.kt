package com.bharatsuraksha.saathi.ui.theme

import android.provider.Settings
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bharatsuraksha.saathi.domain.model.Verdict
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * Honours the system "remove animations" setting (Animator duration scale = 0).
 * When true, screens render their static versions.
 */
@Composable
fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
        }.getOrDefault(false)
    }
}

/** One staggered reveal: fade + a small rise, offset per index (70ms steps). */
@Composable
fun Modifier.reveal(index: Int, reduceMotion: Boolean, stepMillis: Int = 70): Modifier {
    var shown by remember(reduceMotion) { mutableStateOf(reduceMotion) }
    LaunchedEffect(reduceMotion) {
        if (!reduceMotion) {
            delay((index * stepMillis).toLong())
            shown = true
        }
    }
    val p by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(durationMillis = 460, easing = SaathiEase),
        label = "reveal$index"
    )
    return this.graphicsLayer {
        alpha = p
        translationY = (1f - p) * 20.dp.toPx()
    }
}

/** Press feedback: a quick spring to 0.97. */
@Composable
fun Modifier.pressScale(reduceMotion: Boolean): Modifier {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed && !reduceMotion) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = 900f),
        label = "press"
    )
    return this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .pointerInput(reduceMotion) {
            awaitPointerEventScope {
                while (true) {
                    awaitFirstDown(requireUnconsumed = false)
                    pressed = true
                    waitForUpOrCancellation()
                    pressed = false
                }
            }
        }
}

/**
 * The layered background: ink or paper base, a jaali lattice at ~7% opacity,
 * and a soft radial saffron glow behind where the shield sits.
 */
@Composable
fun JaaliAtmosphere(modifier: Modifier = Modifier, glow: Boolean = true) {
    val dark = isSystemInDarkTheme()
    val base = if (dark) Ink else Paper
    val line = if (dark) LatticeOnDark else LatticeOnLight
    Canvas(modifier.fillMaxSize()) {
        drawRect(base)
        drawJaali(line)
        if (glow) {
            val c = Offset(size.width * 0.5f, size.height * 0.28f)
            val r = size.minDimension * 0.85f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Saffron.copy(alpha = if (dark) 0.16f else 0.12f), Color.Transparent),
                    center = c,
                    radius = r
                ),
                radius = r,
                center = c
            )
        }
    }
}

/** Diamond jaali grid, drawn as two crossing families of diagonals. */
private fun DrawScope.drawJaali(line: Color) {
    val step = 52.dp.toPx()
    val w = 1.dp.toPx()
    var x = -size.height
    while (x < size.width + size.height) {
        drawLine(line, Offset(x, 0f), Offset(x + size.height, size.height), w)
        drawLine(line, Offset(x, 0f), Offset(x - size.height, size.height), w)
        x += step
    }
}

/**
 * The guardian shield. [tint] carries the verdict colour; the whole emblem
 * breathes slowly and a chakra ring turns behind it.
 */
@Composable
fun ShieldEmblem(
    modifier: Modifier = Modifier,
    tint: Color = Saffron,
    breath: Boolean = true,
    reduceMotion: Boolean = false,
    glyph: Verdict? = null
) {
    val transition = rememberInfiniteTransition(label = "shield")
    val breathe by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(tween(4000, easing = SaathiEase), RepeatMode.Reverse),
        label = "breathe"
    )
    val spin by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(52000, easing = LinearEasing)),
        label = "spin"
    )
    val scale = if (breath && !reduceMotion) breathe else 1f
    val rotation = if (breath && !reduceMotion) spin else 0f

    Box(modifier) {
        Canvas(Modifier.fillMaxSize().graphicsLayer { rotationZ = rotation }) {
            drawChakra(tint.copy(alpha = 0.35f))
        }
        Canvas(Modifier.fillMaxSize().graphicsLayer { scaleX = scale; scaleY = scale }) {
            drawShield(tint)
            glyph?.let { drawVerdictGlyph(it, tint = Color.White) }
        }
    }
}

private fun DrawScope.drawChakra(color: Color) {
    val r = size.minDimension * 0.47f
    val c = center
    drawCircle(color.copy(alpha = color.alpha * 0.5f), radius = r, center = c, style = Stroke(1.dp.toPx()))
    val spokes = 24
    for (i in 0 until spokes) {
        val a = i * 2.0 * Math.PI / spokes
        val ca = cos(a).toFloat()
        val sa = sin(a).toFloat()
        drawLine(
            color.copy(alpha = color.alpha * 0.45f),
            Offset(c.x + r * 0.74f * ca, c.y + r * 0.74f * sa),
            Offset(c.x + r * ca, c.y + r * sa),
            1.dp.toPx()
        )
    }
}

private fun DrawScope.drawShield(tint: Color) {
    val w = size.width
    val h = size.height
    val path = Path().apply {
        moveTo(w * 0.50f, h * 0.06f)
        lineTo(w * 0.90f, h * 0.24f)
        lineTo(w * 0.90f, h * 0.54f)
        quadraticBezierTo(w * 0.90f, h * 0.84f, w * 0.50f, h * 0.96f)
        quadraticBezierTo(w * 0.10f, h * 0.84f, w * 0.10f, h * 0.54f)
        lineTo(w * 0.10f, h * 0.24f)
        close()
    }
    drawPath(
        path = path,
        brush = Brush.verticalGradient(listOf(tint.copy(alpha = 0.30f), tint.copy(alpha = 0.10f)))
    )
    drawPath(path = path, color = tint, style = Stroke(width = 2.dp.toPx()))

    // A quiet lattice motif inside the shield.
    val step = w * 0.11f
    var x = w * 0.24f
    while (x < w * 0.76f) {
        drawLine(tint.copy(alpha = 0.18f), Offset(x, h * 0.30f), Offset(x, h * 0.70f), 1.dp.toPx())
        x += step
    }
}

/** Never colour alone: each verdict has its own shape. */
@Composable
fun VerdictGlyph(verdict: Verdict, tint: Color, size: Dp = 28.dp, modifier: Modifier = Modifier) {
    Canvas(modifier.then(Modifier.fillMaxSize())) {
        drawVerdictGlyph(verdict, tint, size.toPx())
    }
}

private fun DrawScope.drawVerdictGlyph(verdict: Verdict, tint: Color, sizeOverride: Float? = null) {
    val s = sizeOverride ?: (size.minDimension * 0.34f)
    val c = center
    val stroke = (s * 0.14f).coerceAtLeast(2f)
    when (verdict) {
        Verdict.SAFE -> {   // neem: a rounded shield tick
            val p = Path().apply {
                moveTo(c.x - s * 0.42f, c.y)
                lineTo(c.x - s * 0.10f, c.y + s * 0.34f)
                lineTo(c.x + s * 0.46f, c.y - s * 0.36f)
            }
            drawPath(p, tint, style = Stroke(width = stroke))
        }
        Verdict.CAREFUL -> { // haldi: a triangle warning
            val p = Path().apply {
                moveTo(c.x, c.y - s * 0.46f)
                lineTo(c.x + s * 0.48f, c.y + s * 0.40f)
                lineTo(c.x - s * 0.48f, c.y + s * 0.40f)
                close()
            }
            drawPath(p, tint, style = Stroke(width = stroke))
            drawCircle(tint, radius = stroke * 0.6f, center = Offset(c.x, c.y + s * 0.14f))
        }
        Verdict.DANGEROUS -> { // sindoor: an octagon cross
            val r = s * 0.48f
            val p = Path()
            for (i in 0 until 8) {
                val a = i * Math.PI / 4.0 + Math.PI / 8.0
                val x = c.x + r * cos(a).toFloat()
                val y = c.y + r * sin(a).toFloat()
                if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
            }
            p.close()
            drawPath(p, tint, style = Stroke(width = stroke))
            drawLine(tint, Offset(c.x - s * 0.22f, c.y), Offset(c.x + s * 0.22f, c.y), stroke)
            drawLine(tint, Offset(c.x, c.y - s * 0.22f), Offset(c.x, c.y + s * 0.22f), stroke)
        }
    }
}

/** Verdict palette helpers shared by Result and History. */
@Composable
fun verdictAccent(verdict: Verdict): Color = when (verdict) {
    Verdict.SAFE -> Neem
    Verdict.CAREFUL -> Haldi
    Verdict.DANGEROUS -> Sindoor
}

@Composable
fun verdictContainer(verdict: Verdict, dark: Boolean = isSystemInDarkTheme()): Color = when (verdict) {
    Verdict.SAFE -> if (dark) NeemTintDark else NeemTint
    Verdict.CAREFUL -> if (dark) HaldiTintDark else HaldiTint
    Verdict.DANGEROUS -> if (dark) SindoorTintDark else SindoorTint
}

@Composable
fun verdictOnContainer(verdict: Verdict): Color = when (verdict) {
    Verdict.SAFE -> NeemInk
    Verdict.CAREFUL -> HaldiInk
    Verdict.DANGEROUS -> SindoorInk
}
