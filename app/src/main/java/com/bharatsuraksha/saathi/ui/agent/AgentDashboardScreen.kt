package com.bharatsuraksha.saathi.ui.agent

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bharatsuraksha.saathi.R
import com.bharatsuraksha.saathi.ui.session.AgentActivityStore
import com.bharatsuraksha.saathi.ui.theme.Neem
import com.bharatsuraksha.saathi.ui.theme.Saffron
import com.bharatsuraksha.saathi.ui.theme.Sindoor
import com.bharatsuraksha.saathi.ui.theme.Space2
import com.bharatsuraksha.saathi.ui.theme.Space3
import com.bharatsuraksha.saathi.ui.theme.Space4

/** Today's agent activity, plus an honest list of what the agent cannot see. */
@Composable
fun AgentDashboardScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val activity by AgentActivityStore.activity.collectAsState()

    Scaffold(containerColor = Color.Transparent) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Space4),
            verticalArrangement = Arrangement.spacedBy(Space3)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onBack) { Text(stringResource(R.string.action_back)) }
                Text(
                    text = stringResource(R.string.agent_dash_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = stringResource(R.string.agent_dash_today),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(Space3), modifier = Modifier.fillMaxWidth()) {
                Stat(stringResource(R.string.agent_dash_checked), activity.messagesChecked.toString(), Saffron, Modifier.weight(1f))
                Stat(stringResource(R.string.agent_dash_warned), activity.warningsSent.toString(), Sindoor, Modifier.weight(1f))
                Stat(stringResource(R.string.agent_dash_calls), activity.callsScreened.toString(), Neem, Modifier.weight(1f))
            }

            Text(
                text = stringResource(R.string.agent_dash_timeline),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Card(shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
                HourTimeline(
                    hourly = activity.hourly,
                    modifier = Modifier.fillMaxWidth().height(130.dp).padding(Space4)
                )
            }

            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(Space4), verticalArrangement = Arrangement.spacedBy(Space2)) {
                    Text(
                        text = stringResource(R.string.agent_cannot_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    listOf(
                        R.string.agent_cannot_1,
                        R.string.agent_cannot_2,
                        R.string.agent_cannot_3,
                        R.string.agent_cannot_4,
                        R.string.agent_cannot_5
                    ).forEach { res ->
                        Text("•  " + stringResource(res), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            OutlinedButton(
                onClick = { AgentActivityStore.reset(context) },
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) { Text(stringResource(R.string.agent_dash_reset)) }
            Text(
                text = stringResource(R.string.agent_dash_privacy),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun Stat(label: String, value: String, tint: Color, modifier: Modifier = Modifier) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {
        Column(Modifier.padding(Space3)) {
            Text(text = value, style = MaterialTheme.typography.headlineMedium, color = tint)
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Twenty-four thin bars, one per hour, drawn on Canvas. */
@Composable
private fun HourTimeline(hourly: List<Int>, modifier: Modifier = Modifier) {
    val barColor = Saffron
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val maxValue = (hourly.maxOrNull() ?: 0).coerceAtLeast(1)
    Canvas(modifier) {
        if (hourly.isEmpty()) return@Canvas
        val slot = size.width / hourly.size
        val barWidth = (slot * 0.62f).coerceAtLeast(1f)
        hourly.forEachIndexed { hour, value ->
            val x = slot * hour + (slot - barWidth) / 2f
            drawRoundRect(
                color = trackColor,
                topLeft = Offset(x, 0f),
                size = Size(barWidth, size.height),
                cornerRadius = CornerRadius(barWidth / 2f)
            )
            if (value > 0) {
                val h = size.height * (value.toFloat() / maxValue)
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, size.height - h),
                    size = Size(barWidth, h),
                    cornerRadius = CornerRadius(barWidth / 2f)
                )
            }
        }
    }
}
