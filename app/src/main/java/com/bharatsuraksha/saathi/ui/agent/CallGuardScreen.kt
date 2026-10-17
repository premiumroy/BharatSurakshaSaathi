package com.bharatsuraksha.saathi.ui.agent

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.bharatsuraksha.saathi.R
import com.bharatsuraksha.saathi.domain.model.Verdict
import com.bharatsuraksha.saathi.service.CallGuardService
import com.bharatsuraksha.saathi.ui.theme.Neem
import com.bharatsuraksha.saathi.ui.theme.Saffron
import com.bharatsuraksha.saathi.ui.theme.Sindoor
import com.bharatsuraksha.saathi.ui.theme.Space2
import com.bharatsuraksha.saathi.ui.theme.Space3
import com.bharatsuraksha.saathi.ui.theme.Space4
import com.bharatsuraksha.saathi.ui.theme.Space6
import com.bharatsuraksha.saathi.ui.theme.motionAllowed

/**
 * Call Guard. The user starts it by hand; it is never started for them. The
 * screen is deliberately blunt about what Android will and will not allow.
 */
@Composable
fun CallGuardScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val motion = motionAllowed()
    val state by CallGuardService.state.collectAsState()
    var micDenied by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) CallGuardService.start(context) else micDenied = true
    }

    fun startWithPermission() {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) CallGuardService.start(context) else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
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
                    text = stringResource(R.string.guard_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // The notice the phase requires, before anything starts.
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.guard_start_notice),
                    modifier = Modifier.padding(Space4),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // ---- the indicator ----
            if (state.running) {
                ListeningIndicator(motion = motion, text = state.text, verdict = state.verdict)
                state.findings.forEach { key ->
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = Sindoor.copy(alpha = 0.14f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(guardHeardRes(key)),
                            modifier = Modifier.padding(Space3),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                Button(
                    onClick = { CallGuardService.stop(context) },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Sindoor, contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth().height(64.dp)
                ) { Text(stringResource(R.string.agent_guard_stop), style = MaterialTheme.typography.titleLarge) }
            } else {
                Button(
                    onClick = { startWithPermission() },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth().height(64.dp)
                ) { Text(stringResource(R.string.agent_guard_start), style = MaterialTheme.typography.titleLarge) }
                Text(
                    text = stringResource(R.string.guard_stopped_note),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (micDenied) {
                Text(
                    text = stringResource(R.string.guard_mic_needed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // ---- offline speech model ----
            Card(shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(Space4), verticalArrangement = Arrangement.spacedBy(Space2)) {
                    Text(
                        text = stringResource(R.string.guard_offline_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = stringResource(R.string.guard_offline_body),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedButton(
                        onClick = {
                            runCatching {
                                context.startActivity(
                                    Intent(Settings.ACTION_VOICE_INPUT_SETTINGS)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }
                        },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                    ) { Text(stringResource(R.string.guard_offline_open)) }
                }
            }

            // ---- the honest limits ----
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(Space4), verticalArrangement = Arrangement.spacedBy(Space2)) {
                    Text(
                        text = stringResource(R.string.guard_limits_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    listOf(
                        R.string.guard_limit_1,
                        R.string.guard_limit_2,
                        R.string.guard_limit_3,
                        R.string.guard_limit_4,
                        R.string.guard_limit_5,
                        R.string.guard_limit_6
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

/** The on-screen proof that the microphone is live. */
@Composable
private fun ListeningIndicator(motion: Boolean, text: String, verdict: Verdict) {
    val tint = when (verdict) {
        Verdict.SAFE -> Neem
        Verdict.CAREFUL -> Saffron
        else -> Sindoor
    }
    val transition = rememberInfiniteTransition(label = "listening")
    val pulse by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulse"
    )
    val scale = if (motion) pulse else 1f

    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = tint.copy(alpha = 0.14f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(Space4),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space3)
        ) {
            Box(
                Modifier
                    .size(20.dp)
                    .graphicsLayer { scaleX = scale; scaleY = scale }
                    .background(tint, CircleShape)
            )
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.agent_guard_listening),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = tint
                )
                Text(
                    text = if (text.isBlank()) {
                        stringResource(R.string.guard_waiting_for_speech)
                    } else {
                        stringResource(R.string.guard_heard_so_far, text.takeLast(120))
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun guardHeardRes(key: String): Int = when (key) {
    "cbi" -> R.string.guard_heard_cbi
    "police" -> R.string.guard_heard_police
    "customs" -> R.string.guard_heard_customs
    "ed" -> R.string.guard_heard_ed
    "narcotics" -> R.string.guard_heard_narcotics
    "digital_arrest" -> R.string.guard_heard_digital_arrest
    "arrest" -> R.string.guard_heard_arrest
    "court_case" -> R.string.guard_heard_case
    "do_not_disconnect", "stay_on_line" -> R.string.guard_heard_no_disconnect
    "secrecy" -> R.string.guard_heard_secrecy
    "otp", "otp_request" -> R.string.guard_heard_otp
    "pin", "cvv", "password" -> R.string.guard_heard_pin
    "safe_account", "transfer_money" -> R.string.guard_heard_safe_account
    "upi_request", "send_money" -> R.string.guard_heard_upi
    "urgency" -> R.string.guard_heard_urgency
    "parcel" -> R.string.guard_heard_parcel
    else -> R.string.guard_heard_other
}
