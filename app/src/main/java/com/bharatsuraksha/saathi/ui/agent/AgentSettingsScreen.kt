package com.bharatsuraksha.saathi.ui.agent

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.bharatsuraksha.saathi.R
import com.bharatsuraksha.saathi.data.prefs.SettingsRepository
import com.bharatsuraksha.saathi.service.MessageNotificationListener
import com.bharatsuraksha.saathi.work.PhoneHealthScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import com.bharatsuraksha.saathi.ui.theme.Saffron
import com.bharatsuraksha.saathi.ui.theme.Sindoor
import com.bharatsuraksha.saathi.ui.theme.Space2
import com.bharatsuraksha.saathi.ui.theme.Space3
import com.bharatsuraksha.saathi.ui.theme.Space4
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class AgentSettingsViewModel @Inject constructor(
    private val settings: SettingsRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    val messageWatch: StateFlow<Boolean> =
        settings.agentMessageWatch.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val callScreening: StateFlow<Boolean> =
        settings.agentCallScreening.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val pausedUntil: StateFlow<Long> =
        settings.agentPausedUntil.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val mutedApps: StateFlow<Set<String>> =
        settings.mutedApps.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val trustedSenders: StateFlow<Set<String>> =
        settings.trustedSenders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val silenceUnknown: StateFlow<Boolean> =
        settings.silenceUnknownCallers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val phoneHealth: StateFlow<Boolean> =
        settings.agentPhoneHealth.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val remoteAlert: StateFlow<Boolean> =
        settings.agentRemoteAlert.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun setMessageWatch(on: Boolean) = viewModelScope.launch { settings.setAgentMessageWatch(on) }
    fun setCallScreening(on: Boolean) = viewModelScope.launch { settings.setAgentCallScreening(on) }
    fun setSilenceUnknown(on: Boolean) = viewModelScope.launch { settings.setSilenceUnknownCallers(on) }

    fun setPhoneHealth(on: Boolean) = viewModelScope.launch {
        settings.setAgentPhoneHealth(on)
        if (on) PhoneHealthScheduler.schedule(appContext) else PhoneHealthScheduler.cancel(appContext)
    }

    fun setRemoteAlert(on: Boolean) = viewModelScope.launch { settings.setAgentRemoteAlert(on) }

    fun pauseFor(millis: Long) = viewModelScope.launch {
        settings.setAgentPausedUntil(System.currentTimeMillis() + millis)
    }

    fun resume() = viewModelScope.launch { settings.setAgentPausedUntil(0L) }

    fun toggleMute(packageName: String) = viewModelScope.launch {
        val current = mutedApps.value
        settings.setMutedApps(if (packageName in current) current - packageName else current + packageName)
    }

    fun addTrusted(sender: String) = viewModelScope.launch {
        val clean = sender.trim()
        if (clean.isEmpty()) return@launch
        settings.setTrustedSenders(trustedSenders.value + clean)
    }

    fun removeTrusted(sender: String) = viewModelScope.launch {
        settings.setTrustedSenders(trustedSenders.value - sender)
    }
}

/** One labelled app the agent can watch. */
private data class WatchedApp(val packageName: String, val label: String)

private val WATCHED_LABELS = listOf(
    WatchedApp("com.whatsapp", "WhatsApp"),
    WatchedApp("com.google.android.apps.messaging", "Messages (SMS)"),
    WatchedApp("com.android.mms", "SMS"),
    WatchedApp("org.telegram.messenger", "Telegram"),
    WatchedApp("com.instagram.android", "Instagram"),
    WatchedApp("com.google.android.gm", "Gmail"),
    WatchedApp("com.microsoft.outlook", "Outlook")
)

/**
 * Saathi Agent settings. Everything is opt-in, off by default, and explained in
 * plain words before it is switched on.
 */
@Composable
fun AgentSettingsScreen(
    onBack: () -> Unit,
    onOpenGuard: () -> Unit = {},
    onOpenPhoneHealth: () -> Unit = {},
    onOpenBattery: () -> Unit = {},
    viewModel: AgentSettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val messageWatch by viewModel.messageWatch.collectAsStateWithLifecycle()
    val callScreening by viewModel.callScreening.collectAsStateWithLifecycle()
    val pausedUntil by viewModel.pausedUntil.collectAsStateWithLifecycle()
    val muted by viewModel.mutedApps.collectAsStateWithLifecycle()
    val trusted by viewModel.trustedSenders.collectAsStateWithLifecycle()
    val silenceUnknown by viewModel.silenceUnknown.collectAsStateWithLifecycle()
    val phoneHealth by viewModel.phoneHealth.collectAsStateWithLifecycle()
    val remoteAlert by viewModel.remoteAlert.collectAsStateWithLifecycle()

    var newSender by remember { mutableStateOf("") }
    val paused = pausedUntil > System.currentTimeMillis()

    val roleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }

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
                    text = stringResource(R.string.agent_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = stringResource(R.string.agent_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // ---- pause ----
            Card(shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(Space4), verticalArrangement = Arrangement.spacedBy(Space3)) {
                    Text(
                        text = if (paused) stringResource(R.string.agent_paused_until, pauseLabel(pausedUntil)) else stringResource(R.string.agent_running),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (paused) Sindoor else Saffron
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(Space2), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { viewModel.pauseFor(60L * 60L * 1000L) },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.weight(1f).heightIn(min = 52.dp)
                        ) { Text(stringResource(R.string.agent_pause_hour), style = MaterialTheme.typography.labelLarge) }
                        OutlinedButton(
                            onClick = { viewModel.pauseFor(millisUntilTomorrow()) },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.weight(1f).heightIn(min = 52.dp)
                        ) { Text(stringResource(R.string.agent_pause_today), style = MaterialTheme.typography.labelLarge) }
                    }
                    if (paused) {
                        Button(
                            onClick = { viewModel.resume() },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                        ) { Text(stringResource(R.string.agent_resume)) }
                    }
                }
            }

            // ---- message watch ----
            Card(shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(Space4), verticalArrangement = Arrangement.spacedBy(Space2)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.agent_watch_title),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(checked = messageWatch, onCheckedChange = viewModel::setMessageWatch)
                    }
                    Text(
                        text = stringResource(R.string.agent_watch_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = {
                            context.startActivity(
                                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                    ) { Text(stringResource(R.string.helper_open_notif)) }
                }
            }

            // ---- call screening ----
            Card(shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(Space4), verticalArrangement = Arrangement.spacedBy(Space2)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.agent_call_title),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(checked = callScreening, onCheckedChange = viewModel::setCallScreening)
                    }
                    Text(
                        text = stringResource(R.string.agent_call_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                val rm = context.getSystemService(RoleManager::class.java)
                                if (rm != null && rm.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
                                    roleLauncher.launch(rm.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
                                }
                            }
                        },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                    ) { Text(stringResource(R.string.agent_call_role)) }

                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.agent_silence_title),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = stringResource(R.string.agent_silence_body),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Switch(checked = silenceUnknown, onCheckedChange = viewModel::setSilenceUnknown)
                    }
                }
            }

            // ---- call guard ----
            Card(shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(Space4), verticalArrangement = Arrangement.spacedBy(Space2)) {
                    Text(
                        text = stringResource(R.string.agent_guard_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = stringResource(R.string.agent_guard_sub),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = onOpenGuard,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                    ) { Text(stringResource(R.string.guard_title)) }
                }
            }

            // ---- phone health ----
            Card(shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(Space4), verticalArrangement = Arrangement.spacedBy(Space2)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.agent_health_title),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(checked = phoneHealth, onCheckedChange = viewModel::setPhoneHealth)
                    }
                    Text(
                        text = stringResource(R.string.agent_health_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = onOpenPhoneHealth,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                    ) { Text(stringResource(R.string.health_title)) }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.agent_remote_title),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(checked = remoteAlert, onCheckedChange = viewModel::setRemoteAlert)
                    }
                    Text(
                        text = stringResource(R.string.agent_remote_body),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = onOpenBattery,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                    ) { Text(stringResource(R.string.battery_title)) }
                }
            }

            // ---- muted apps ----
            Card(shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(Space4), verticalArrangement = Arrangement.spacedBy(Space2)) {
                    Text(
                        text = stringResource(R.string.agent_mute_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    WATCHED_LABELS.forEach { app ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = app.label,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = app.packageName in muted,
                                onCheckedChange = { viewModel.toggleMute(app.packageName) }
                            )
                        }
                    }
                    Text(
                        text = stringResource(R.string.agent_mute_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ---- trusted senders ----
            Card(shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(Space4), verticalArrangement = Arrangement.spacedBy(Space2)) {
                    Text(
                        text = stringResource(R.string.agent_trust_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = stringResource(R.string.agent_trust_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = newSender,
                        onValueChange = { newSender = it },
                        label = { Text(stringResource(R.string.agent_trust_field)) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { viewModel.addTrusted(newSender); newSender = "" },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                    ) { Text(stringResource(R.string.agent_trust_add)) }
                    trusted.forEach { sender ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = sender,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { viewModel.removeTrusted(sender) }) {
                                Text(stringResource(R.string.helpline_remove))
                            }
                        }
                    }
                }
            }

            // ---- honest limits ----
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(Space4), verticalArrangement = Arrangement.spacedBy(Space2)) {
                    Text(
                        text = stringResource(R.string.agent_limits_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = stringResource(R.string.agent_limits_body),
                        style = MaterialTheme.typography.bodyMedium
                    )
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

private fun pauseLabel(untilMillis: Long): String {
    val minutes = ((untilMillis - System.currentTimeMillis()) / 60000L).coerceAtLeast(0L)
    return if (minutes >= 60) "${minutes / 60}h ${minutes % 60}m" else "${minutes}m"
}

private fun millisUntilTomorrow(): Long {
    val now = Calendar.getInstance()
    val end = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return (end.timeInMillis - now.timeInMillis).coerceAtLeast(60_000L)
}
