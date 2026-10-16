package com.bharatsuraksha.saathi.ui.notifications

import android.Manifest
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.bharatsuraksha.saathi.R
import com.bharatsuraksha.saathi.data.prefs.SettingsRepository
import com.bharatsuraksha.saathi.ui.theme.BrandGreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationAccessViewModel @Inject constructor(
    private val settings: SettingsRepository
) : ViewModel() {
    val autoCheck: StateFlow<Boolean> =
        settings.autoCheckEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
    val callTip: StateFlow<Boolean> =
        settings.callTipEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun setAutoCheck(on: Boolean) = viewModelScope.launch { settings.setAutoCheckEnabled(on) }
    fun setCallTip(on: Boolean) = viewModelScope.launch { settings.setCallTipEnabled(on) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationAccessScreen(
    onBack: () -> Unit,
    viewModel: NotificationAccessViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val autoCheck by viewModel.autoCheck.collectAsStateWithLifecycle()
    val callTip by viewModel.callTip.collectAsStateWithLifecycle()

    // Recomputed on every recomposition (e.g. after returning from system settings).
    val listenerEnabled = isListenerEnabled(context)
    var notifGranted by remember { mutableStateOf(hasPermission(context, Manifest.permission.POST_NOTIFICATIONS)) }
    var contactsGranted by remember { mutableStateOf(hasPermission(context, Manifest.permission.READ_CONTACTS)) }

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> notifGranted = granted }

    val contactsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> contactsGranted = granted }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notif_access_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandGreen,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(stringResource(R.string.notif_access_why), style = MaterialTheme.typography.bodyLarge)
            Text(stringResource(R.string.notif_access_how), style = MaterialTheme.typography.bodyMedium)

            Text(
                text = stringResource(
                    if (listenerEnabled) R.string.notif_access_granted else R.string.notif_access_not_granted
                ),
                fontWeight = FontWeight.Bold
            )
            OutlinedButton(
                onClick = {
                    context.startActivity(
                        Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            ) { Text(stringResource(R.string.notif_access_grant)) }

            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.settings_auto_check),
                            Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Switch(checked = autoCheck, onCheckedChange = viewModel::setAutoCheck)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.notif_access_notif_perm),
                            Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Switch(
                            checked = notifGranted,
                            onCheckedChange = { want ->
                                if (want && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.notif_access_contacts),
                            Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Switch(
                            checked = contactsGranted,
                            onCheckedChange = { want ->
                                if (want) contactsLauncher.launch(Manifest.permission.READ_CONTACTS)
                            }
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.settings_call_tip),
                            Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Switch(checked = callTip, onCheckedChange = viewModel::setCallTip)
                    }
                    OutlinedButton(
                        onClick = { requestCallScreeningRole(context) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.notif_access_callscreen)) }
                }
            }

            Text(stringResource(R.string.notif_access_privacy), style = MaterialTheme.typography.bodyMedium)
        }
    }

}

private fun isListenerEnabled(context: Context): Boolean =
    NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)

private fun hasPermission(context: Context, permission: String): Boolean {
    if (permission == Manifest.permission.POST_NOTIFICATIONS && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return true
    }
    return ContextCompat.checkSelfPermission(context, permission) ==
        android.content.pm.PackageManager.PERMISSION_GRANTED
}

private fun requestCallScreeningRole(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
    val roleManager = context.getSystemService(RoleManager::class.java) ?: return
    if (!roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) return
    if (roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) return
    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}
