package com.bharatsuraksha.saathi.ui.agent

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bharatsuraksha.saathi.R
import com.bharatsuraksha.saathi.domain.agent.BatteryGuidance
import com.bharatsuraksha.saathi.ui.theme.Saffron
import com.bharatsuraksha.saathi.ui.theme.Space3
import com.bharatsuraksha.saathi.ui.theme.Space4

/**
 * Battery guidance for the phone makers whose savers stop background apps. The
 * steps are guidance only: the app changes no setting by itself.
 */
@Composable
fun BatteryGuidanceScreen(onBack: () -> Unit) {
    val vendor = remember { BatteryGuidance.vendorFor(Build.MANUFACTURER, Build.BRAND) }

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
                    text = stringResource(R.string.battery_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = stringResource(R.string.battery_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.battery_detected, vendor.label),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Saffron
            )

            vendor.stepKeys.forEachIndexed { index, key ->
                Card(shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(Space4), horizontalArrangement = Arrangement.spacedBy(Space3)) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Saffron,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = stringResource(stepRes(key)),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Text(
                text = stringResource(R.string.battery_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun stepRes(key: String): Int = when (key) {
    "battery_xiaomi_1" -> R.string.battery_xiaomi_1
    "battery_xiaomi_2" -> R.string.battery_xiaomi_2
    "battery_xiaomi_3" -> R.string.battery_xiaomi_3
    "battery_xiaomi_4" -> R.string.battery_xiaomi_4
    "battery_oppo_1" -> R.string.battery_oppo_1
    "battery_oppo_2" -> R.string.battery_oppo_2
    "battery_oppo_3" -> R.string.battery_oppo_3
    "battery_vivo_1" -> R.string.battery_vivo_1
    "battery_vivo_2" -> R.string.battery_vivo_2
    "battery_vivo_3" -> R.string.battery_vivo_3
    "battery_samsung_1" -> R.string.battery_samsung_1
    "battery_samsung_2" -> R.string.battery_samsung_2
    "battery_samsung_3" -> R.string.battery_samsung_3
    "battery_other_1" -> R.string.battery_other_1
    "battery_other_2" -> R.string.battery_other_2
    else -> R.string.battery_other_3
}
