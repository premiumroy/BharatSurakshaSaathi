package com.bharatsuraksha.saathi.ui.safety

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bharatsuraksha.saathi.R
import com.bharatsuraksha.saathi.domain.safety.GoldenHour
import com.bharatsuraksha.saathi.ui.theme.Saffron
import com.bharatsuraksha.saathi.ui.theme.Sindoor
import com.bharatsuraksha.saathi.ui.theme.Space2
import com.bharatsuraksha.saathi.ui.theme.Space3
import com.bharatsuraksha.saathi.ui.theme.Space4
import kotlinx.coroutines.delay

/**
 * "अभी मदद / Help now". The golden-hour countdown, one tap to 1930, the freeze
 * steps and the evidence checklist. Always reachable from Home and Result.
 */
@Composable
fun HelpNowScreen(onBack: () -> Unit, onOpenRecovery: () -> Unit) {
    val context = LocalContext.current
    var startedAt by remember { mutableLongStateOf(0L) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(startedAt) {
        while (startedAt > 0L) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }

    val status = if (startedAt > 0L) GoldenHour.status(startedAt, now) else null

    Scaffold(containerColor = Color.Transparent) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Space4),
            verticalArrangement = Arrangement.spacedBy(Space4)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onBack) { Text(stringResource(R.string.action_back)) }
                Text(
                    text = stringResource(R.string.helpnow_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // ---- golden hour ----
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(Space4), verticalArrangement = Arrangement.spacedBy(Space3)) {
                    Text(
                        text = stringResource(R.string.helpnow_golden_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.helpnow_golden_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (status == null) {
                        Text(
                            text = stringResource(R.string.helpnow_when),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(Space2), modifier = Modifier.fillMaxWidth()) {
                            WhenChip(stringResource(R.string.helpnow_just_now), Modifier.weight(1f)) {
                                startedAt = System.currentTimeMillis()
                                now = startedAt
                            }
                            WhenChip(stringResource(R.string.helpnow_15), Modifier.weight(1f)) {
                                startedAt = System.currentTimeMillis() - 15L * 60L * 1000L
                                now = System.currentTimeMillis()
                            }
                            WhenChip(stringResource(R.string.helpnow_30), Modifier.weight(1f)) {
                                startedAt = System.currentTimeMillis() - 30L * 60L * 1000L
                                now = System.currentTimeMillis()
                            }
                        }
                    } else {
                        val tint = when (GoldenHour.level(status.remainingMillis)) {
                            GoldenHour.Level.URGENT -> Sindoor
                            GoldenHour.Level.SOON -> Saffron
                            GoldenHour.Level.EXPIRED -> MaterialTheme.colorScheme.onSurfaceVariant
                            else -> Saffron
                        }
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(96.dp)
                                .background(tint.copy(alpha = 0.14f), RoundedCornerShape(28.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (status.expired) {
                                    stringResource(R.string.helpnow_expired)
                                } else {
                                    GoldenHour.format(status.remainingMillis)
                                },
                                style = MaterialTheme.typography.displayMedium,
                                color = tint,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Text(
                            text = if (status.expired) {
                                stringResource(R.string.helpnow_expired_note)
                            } else {
                                stringResource(R.string.helpnow_still_time)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = { startedAt = 0L }) {
                            Text(stringResource(R.string.helpnow_reset))
                        }
                    }
                }
            }

            Button(
                onClick = { dial(context, "1930") },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Sindoor, contentColor = Color.White),
                modifier = Modifier.fillMaxWidth().height(72.dp)
            ) { Text(stringResource(R.string.helpnow_call_1930), style = MaterialTheme.typography.titleLarge) }

            StepCard(
                title = stringResource(R.string.helpnow_freeze_title),
                body = stringResource(R.string.helpnow_freeze_body)
            )
            StepCard(
                title = stringResource(R.string.helpnow_evidence_title),
                body = stringResource(R.string.helpnow_evidence_body)
            )
            OutlinedButton(
                onClick = onOpenRecovery,
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
            ) { Text(stringResource(R.string.helpnow_open_evidence)) }

            OutlinedButton(
                onClick = { open(context, "https://cybercrime.gov.in") },
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
            ) { Text(stringResource(R.string.helpnow_cybercrime)) }

            Text(
                text = stringResource(R.string.helpnow_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.verdict_guarantee_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WhenChip(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        modifier = modifier.heightIn(min = 52.dp)
    ) { Text(label, style = MaterialTheme.typography.labelLarge) }
}

@Composable
private fun StepCard(title: String, body: String) {
    Card(shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Space4), verticalArrangement = Arrangement.spacedBy(Space2)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun dial(context: Context, number: String) {
    context.startActivity(
        Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

private fun open(context: Context, url: String) {
    context.startActivity(
        Intent(Intent.ACTION_VIEW, Uri.parse(url))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}
