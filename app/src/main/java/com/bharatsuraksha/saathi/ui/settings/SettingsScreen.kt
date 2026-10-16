package com.bharatsuraksha.saathi.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bharatsuraksha.saathi.R
import com.bharatsuraksha.saathi.data.prefs.PinManager
import com.bharatsuraksha.saathi.domain.model.Sensitivity
import com.bharatsuraksha.saathi.ui.theme.BrandGreen

private val WATCHABLE_APPS = listOf(
    "com.whatsapp" to "WhatsApp",
    "com.google.android.apps.messaging" to "Google Messages",
    "com.android.messaging" to "Messages",
    "com.samsung.android.messaging" to "Samsung Messages",
    "org.telegram.messenger" to "Telegram"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenNotificationAccess: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenTips: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val easyMode by viewModel.easyMode.collectAsStateWithLifecycle()
    val highContrast by viewModel.highContrast.collectAsStateWithLifecycle()
    val kidsMode by viewModel.kidsMode.collectAsStateWithLifecycle()
    val sensitivity by viewModel.sensitivity.collectAsStateWithLifecycle()
    val watchedApps by viewModel.watchedApps.collectAsStateWithLifecycle()
    val autoCheck by viewModel.autoCheck.collectAsStateWithLifecycle()
    val callTip by viewModel.callTip.collectAsStateWithLifecycle()
    val onlineMode by viewModel.onlineMode.collectAsStateWithLifecycle()
    val endpoint by viewModel.onlineEndpoint.collectAsStateWithLifecycle()
    val familyContacts by viewModel.familyContacts.collectAsStateWithLifecycle()
    val pinHash by viewModel.pinHash.collectAsStateWithLifecycle()
    val hasPin by viewModel.hasPin.collectAsStateWithLifecycle()

    var unlocked by remember { mutableStateOf(false) }
    var showUnlock by remember { mutableStateOf(false) }
    var showSetPin by remember { mutableStateOf(false) }
    var showClear by remember { mutableStateOf(false) }
    var endpointText by remember(endpoint) { mutableStateOf(endpoint) }
    var newContact by remember { mutableStateOf("") }

    val locked = kidsMode && !unlocked

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title), fontWeight = FontWeight.Bold) },
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
            if (locked) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.settings_kids_locked), style = MaterialTheme.typography.bodyLarge)
                        TextButton(onClick = { showUnlock = true }) {
                            Text(stringResource(R.string.pin_enter_title))
                        }
                    }
                }
            }

            // ---------------- Detection ----------------
            SectionHeader(stringResource(R.string.settings_detection))

            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.settings_sensitivity), fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SensitivityChip(Sensitivity.LOW, R.string.sensitivity_low, sensitivity, locked, viewModel)
                        SensitivityChip(Sensitivity.MEDIUM, R.string.sensitivity_medium, sensitivity, locked, viewModel)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SensitivityChip(Sensitivity.HIGH, R.string.sensitivity_high, sensitivity, locked, viewModel)
                    }
                    ToggleRow(
                        label = stringResource(R.string.settings_auto_check),
                        checked = autoCheck,
                        enabled = !locked,
                        onCheckedChange = viewModel::setAutoCheck
                    )
                    ToggleRow(
                        label = stringResource(R.string.settings_call_tip),
                        checked = callTip,
                        enabled = !locked,
                        onCheckedChange = viewModel::setCallTip
                    )
                    TextButton(onClick = onOpenNotificationAccess, enabled = !locked) {
                        Text(stringResource(R.string.notif_access_title))
                    }
                }
            }

            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.settings_watch_apps), fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.settings_watch_apps_help), style = MaterialTheme.typography.bodyMedium)
                    WATCHABLE_APPS.forEach { (pkg, label) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = watchedApps.contains(pkg),
                                enabled = !locked,
                                onCheckedChange = { checked ->
                                    val next = watchedApps.toMutableSet()
                                    if (checked) next.add(pkg) else next.remove(pkg)
                                    viewModel.setWatchedApps(next)
                                }
                            )
                            Text(label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }

            // ---------------- Appearance ----------------
            SectionHeader(stringResource(R.string.settings_appearance))

            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ToggleRow(
                        label = stringResource(R.string.settings_easy_mode),
                        checked = easyMode,
                        enabled = !locked,
                        onCheckedChange = viewModel::setEasyMode
                    )
                    ToggleRow(
                        label = stringResource(R.string.settings_high_contrast),
                        checked = highContrast,
                        enabled = !locked,
                        onCheckedChange = viewModel::setHighContrast
                    )
                    ToggleRow(
                        label = stringResource(R.string.settings_kids_mode),
                        checked = kidsMode,
                        enabled = !locked || kidsMode,
                        onCheckedChange = { on ->
                            if (on && !hasPin) {
                                showSetPin = true
                            } else {
                                viewModel.setKidsMode(on)
                                if (!on) unlocked = false
                            }
                        }
                    )
                    if (kidsMode) {
                        Text(
                            stringResource(R.string.settings_kids_sensitivity_note),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    TextButton(onClick = { showSetPin = true }, enabled = !locked) {
                        Text(stringResource(R.string.settings_manage_pin))
                    }
                }
            }

            // ---------------- Language ----------------
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.settings_language), fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = false,
                            onClick = { AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en")) },
                            label = { Text("English") }
                        )
                        FilterChip(
                            selected = false,
                            onClick = { AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("hi")) },
                            label = { Text("हिन्दी") }
                        )
                    }
                }
            }

            // ---------------- Online ----------------
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.settings_online), fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.settings_online_help), style = MaterialTheme.typography.bodyMedium)
                    ToggleRow(
                        label = stringResource(R.string.settings_online),
                        checked = onlineMode,
                        enabled = !locked,
                        onCheckedChange = viewModel::setOnlineMode
                    )
                    OutlinedTextField(
                        value = endpointText,
                        onValueChange = { endpointText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.settings_online_endpoint)) },
                        singleLine = true,
                        enabled = !locked && onlineMode
                    )
                    TextButton(
                        onClick = { viewModel.setEndpoint(endpointText) },
                        enabled = !locked && onlineMode
                    ) { Text(stringResource(R.string.action_save)) }
                }
            }

            // ---------------- Family contacts ----------------
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.family_title), fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.family_intro), style = MaterialTheme.typography.bodyMedium)
                    familyContacts.forEach { contact ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(contact, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                            TextButton(onClick = {
                                viewModel.setFamilyContacts(familyContacts - contact)
                            }) { Text(stringResource(R.string.history_delete)) }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = newContact,
                            onValueChange = { newContact = it },
                            modifier = Modifier.weight(1f),
                            label = { Text(stringResource(R.string.family_add)) },
                            singleLine = true
                        )
                        TextButton(
                            onClick = {
                                if (newContact.isNotBlank()) {
                                    viewModel.setFamilyContacts(familyContacts + newContact.trim())
                                    newContact = ""
                                }
                            }
                        ) { Text(stringResource(R.string.action_add)) }
                    }
                }
            }

            // ---------------- Data / about ----------------
            SectionHeader(stringResource(R.string.settings_data))

            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onOpenPrivacy, enabled = !locked) {
                        Text(stringResource(R.string.settings_privacy))
                    }
                    TextButton(onClick = onOpenTips) { Text(stringResource(R.string.settings_tips)) }
                    TextButton(onClick = { showClear = true }, enabled = !locked) {
                        Text(stringResource(R.string.settings_clear_data))
                    }
                    Text(stringResource(R.string.settings_about), fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.settings_about_body), style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showUnlock) {
        PinDialog(
            title = stringResource(R.string.pin_enter_title),
            onDismiss = { showUnlock = false },
            onConfirm = { pin ->
                if (viewModel.checkPin(pin, pinHash)) {
                    unlocked = true
                    showUnlock = false
                    true
                } else {
                    false
                }
            }
        )
    }

    if (showSetPin) {
        PinDialog(
            title = stringResource(R.string.pin_set_title),
            confirmStep = true,
            onDismiss = { showSetPin = false },
            onConfirm = { pin ->
                if (PinManager.isValidPin(pin)) {
                    viewModel.setPin(pin)
                    viewModel.setKidsMode(true)
                    showSetPin = false
                    true
                } else {
                    false
                }
            }
        )
    }

    if (showClear) {
        AlertDialog(
            onDismissRequest = { showClear = false },
            title = { Text(stringResource(R.string.settings_clear_data_title)) },
            text = { Text(stringResource(R.string.settings_clear_data_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAllData()
                    showClear = false
                }) { Text(stringResource(R.string.history_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showClear = false }) { Text(stringResource(R.string.history_cancel)) }
            }
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(checked = checked, enabled = enabled, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SensitivityChip(
    value: Sensitivity,
    labelRes: Int,
    current: Sensitivity,
    locked: Boolean,
    viewModel: SettingsViewModel
) {
    FilterChip(
        selected = current == value,
        enabled = !locked,
        onClick = { viewModel.setSensitivity(value) },
        label = { Text(stringResource(labelRes)) }
    )
}

/** A reusable PIN entry dialog. When [confirmStep] is true, the PIN is entered twice. */
@Composable
private fun PinDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Boolean,
    confirmStep: Boolean = false
) {
    var pin by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it.filter(Char::isDigit).take(6); error = null },
                    label = { Text(stringResource(R.string.pin_hint)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword
                    )
                )
                if (confirmStep) {
                    OutlinedTextField(
                        value = confirm,
                        onValueChange = { confirm = it.filter(Char::isDigit).take(6); error = null },
                        label = { Text(stringResource(R.string.pin_confirm)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword
                        )
                    )
                }
                if (confirmStep && !PinManager.isValidPin(pin) && pin.isNotEmpty()) {
                    Text(stringResource(R.string.pin_too_short), color = MaterialTheme.colorScheme.error)
                }
                if (confirmStep && confirm.isNotEmpty() && pin != confirm) {
                    Text(stringResource(R.string.pin_mismatch), color = MaterialTheme.colorScheme.error)
                }
                error?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error) }
                Text(stringResource(R.string.pin_forgot), style = MaterialTheme.typography.bodyMedium)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (confirmStep && pin != confirm) {
                    error = R.string.pin_mismatch
                } else {
                    val ok = onConfirm(pin)
                    if (!ok) error = R.string.pin_wrong
                }
            }) { Text(stringResource(R.string.pin_ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.pin_cancel)) }
        }
    )
}
