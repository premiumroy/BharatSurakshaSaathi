package com.bharatsuraksha.saathi.ui.agent

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bharatsuraksha.saathi.R
import com.bharatsuraksha.saathi.domain.agent.PhoneHealth
import com.bharatsuraksha.saathi.ui.session.PhoneHealthStore
import com.bharatsuraksha.saathi.ui.theme.Neem
import com.bharatsuraksha.saathi.ui.theme.Saffron
import com.bharatsuraksha.saathi.ui.theme.Sindoor
import com.bharatsuraksha.saathi.ui.theme.Space2
import com.bharatsuraksha.saathi.ui.theme.Space3
import com.bharatsuraksha.saathi.ui.theme.Space4
import com.bharatsuraksha.saathi.work.PhoneHealthScheduler
import java.util.Calendar

/** Phone health: a score ring and three plain-language action cards. */
@Composable
fun PhoneHealthScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val snapshot by PhoneHealthStore.snapshot.collectAsState()
    val level = runCatching { PhoneHealth.Level.valueOf(snapshot.level) }
        .getOrDefault(PhoneHealth.Level.GOOD)
    val tint = when (level) {
        PhoneHealth.Level.GOOD -> Neem
        PhoneHealth.Level.WATCH -> Saffron
        PhoneHealth.Level.RISKY -> Sindoor
    }

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
                    text = stringResource(R.string.health_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = stringResource(R.string.health_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(Space4), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(
                                when (level) {
                                    PhoneHealth.Level.GOOD -> R.string.health_level_good
                                    PhoneHealth.Level.WATCH -> R.string.health_level_watch
                                    PhoneHealth.Level.RISKY -> R.string.health_level_risky
                                }
                            ),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = tint
                        )
                        Text(
                            text = if (snapshot.checkedAt == 0L) {
                                stringResource(R.string.health_never_checked)
                            } else {
                                stringResource(R.string.health_last_checked, formatWhen(snapshot.checkedAt))
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.health_usage_access, if (snapshot.usageAccessGranted) stringResource(R.string.health_yes) else stringResource(R.string.health_no)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    ScoreRing(score = snapshot.score, tint = tint, modifier = Modifier.size(96.dp))
                }
            }

            // ---- the three action cards ----
            snapshot.actionKeys.take(3).forEach { key ->
                Card(shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(Space4), verticalArrangement = Arrangement.spacedBy(Space2)) {
                        Text(
                            text = stringResource(actionTitleRes(key)),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(actionBodyRes(key)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val names = when (key) {
                            PhoneHealth.ACTION_ACCESSIBILITY -> snapshot.accessibilityNames
                            PhoneHealth.ACTION_PERMISSIONS -> snapshot.riskyPackageNames
                            PhoneHealth.ACTION_APK -> snapshot.apkNames
                            else -> emptyList()
                        }
                        if (names.isNotEmpty()) {
                            Text(
                                text = names.take(5).joinToString("\n") { "•  $it" },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // ---- remote access ----
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = if (snapshot.remoteUrgent) Sindoor.copy(alpha = 0.16f)
                    else MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(Space4), verticalArrangement = Arrangement.spacedBy(Space2)) {
                    Text(
                        text = stringResource(R.string.health_remote_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (snapshot.remoteAccessInstalled.isEmpty()) {
                        Text(
                            text = stringResource(R.string.health_remote_none),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = snapshot.remoteAccessInstalled.joinToString("\n") {
                                "•  " + PhoneHealthStore.labelFor(it)
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (snapshot.remoteUrgent) {
                            Text(
                                text = stringResource(R.string.health_remote_urgent),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Sindoor
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            context.startActivity(
                                Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                    ) { Text(stringResource(R.string.health_open_usage_access)) }
                }
            }

            Button(
                onClick = { PhoneHealthScheduler.runNow(context) },
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) { Text(stringResource(R.string.health_check_now)) }

            OutlinedButton(
                onClick = { openBattery(context) },
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
            ) { Text(stringResource(R.string.battery_open)) }

            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(Space4), verticalArrangement = Arrangement.spacedBy(Space2)) {
                    Text(
                        text = stringResource(R.string.health_limits_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    listOf(
                        R.string.health_limit_1,
                        R.string.health_limit_2,
                        R.string.health_limit_3
                    ).forEach { res ->
                        Text("•  " + stringResource(res), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Text(
                text = stringResource(R.string.verdict_guarantee_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ScoreRing(score: Int, tint: Color, modifier: Modifier = Modifier) {
    val track = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 9.dp.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(
                color = track, startAngle = -90f, sweepAngle = 360f, useCenter = false,
                topLeft = Offset(inset, inset), size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = tint, startAngle = -90f,
                sweepAngle = 360f * (score.coerceIn(0, 100) / 100f), useCenter = false,
                topLeft = Offset(inset, inset), size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Text(
            text = score.toString(),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun actionTitleRes(key: String): Int = when (key) {
    PhoneHealth.ACTION_ACCESSIBILITY -> R.string.health_action_accessibility_title
    PhoneHealth.ACTION_PERMISSIONS -> R.string.health_action_permissions_title
    PhoneHealth.ACTION_APK -> R.string.health_action_apk_title
    else -> R.string.health_action_clear_title
}

private fun actionBodyRes(key: String): Int = when (key) {
    PhoneHealth.ACTION_ACCESSIBILITY -> R.string.health_action_accessibility_body
    PhoneHealth.ACTION_PERMISSIONS -> R.string.health_action_permissions_body
    PhoneHealth.ACTION_APK -> R.string.health_action_apk_body
    else -> R.string.health_action_clear_body
}

private fun formatWhen(millis: Long): String {
    val c = Calendar.getInstance().apply { timeInMillis = millis }
    return "%02d/%02d %02d:%02d".format(
        c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH) + 1,
        c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)
    )
}

private fun openBattery(context: Context) {
    runCatching {
        context.startActivity(
            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
