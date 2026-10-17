package com.bharatsuraksha.saathi.ui.learn

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bharatsuraksha.saathi.R
import com.bharatsuraksha.saathi.ui.theme.JyotiFlame
import com.bharatsuraksha.saathi.ui.theme.MotionTokens
import com.bharatsuraksha.saathi.ui.theme.Saffron
import com.bharatsuraksha.saathi.ui.theme.Space2
import com.bharatsuraksha.saathi.ui.theme.Space3
import com.bharatsuraksha.saathi.ui.theme.Space4
import androidx.compose.material3.Icon
import kotlinx.coroutines.delay

/**
 * "रुको-सोचो-करो": a 30 second pause with a breathing flame and three
 * questions. Skippable, and it never blocks the emergency actions.
 */
@Composable
fun CalmTimerCard(motion: Boolean, modifier: Modifier = Modifier) {
    var secondsLeft by remember { mutableIntStateOf(30) }
    var running by remember { mutableStateOf(true) }
    var a by remember { mutableStateOf(false) }
    var b by remember { mutableStateOf(false) }
    var c by remember { mutableStateOf(false) }

    LaunchedEffect(running) {
        while (running && secondsLeft > 0) {
            delay(1000)
            secondsLeft -= 1
        }
        if (secondsLeft <= 0) running = false
    }

    val transition = rememberInfiniteTransition(label = "breath")
    val raw by transition.animateFloat(
        initialValue = 0.86f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(4000, easing = MotionTokens.Entrance), RepeatMode.Reverse),
        label = "breath"
    )
    val scale = if (motion && running) raw else 1f

    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(Space4), verticalArrangement = Arrangement.spacedBy(Space3)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space3)) {
                Box(Modifier.size(40.dp).graphicsLayer { scaleX = scale; scaleY = scale }) {
                    Icon(
                        imageVector = JyotiFlame,
                        contentDescription = null,
                        tint = Saffron,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.calm_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (running) {
                            stringResource(R.string.calm_counting, secondsLeft)
                        } else {
                            stringResource(R.string.calm_done)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (running) {
                    TextButton(onClick = { running = false }) { Text(stringResource(R.string.calm_skip)) }
                }
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(((30 - secondsLeft) / 30f).coerceIn(0f, 1f))
                        .height(6.dp)
                        .background(Saffron, RoundedCornerShape(50))
                )
            }

            ChecklistRow(stringResource(R.string.calm_check_1), a) { a = it }
            ChecklistRow(stringResource(R.string.calm_check_2), b) { b = it }
            ChecklistRow(stringResource(R.string.calm_check_3), c) { c = it }

            Text(
                text = stringResource(R.string.calm_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ChecklistRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
