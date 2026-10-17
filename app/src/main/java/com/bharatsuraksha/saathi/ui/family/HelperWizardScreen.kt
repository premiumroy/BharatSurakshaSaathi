package com.bharatsuraksha.saathi.ui.family

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.bharatsuraksha.saathi.ui.theme.Saffron
import com.bharatsuraksha.saathi.ui.theme.Space2
import com.bharatsuraksha.saathi.ui.theme.Space3
import com.bharatsuraksha.saathi.ui.theme.Space4
import com.bharatsuraksha.saathi.ui.theme.Space6
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HelperWizardViewModel @Inject constructor(
    private val settings: SettingsRepository
) : ViewModel() {

    val easyMode: StateFlow<Boolean> =
        settings.easyMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val familyContacts: StateFlow<Set<String>> =
        settings.familyContacts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun setEasyMode(on: Boolean) = viewModelScope.launch { settings.setEasyMode(on) }

    fun addContact(number: String) {
        val clean = number.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch { settings.setFamilyContacts(familyContacts.value + clean) }
    }
}

/**
 * Helper mode: a young person sets the app up on an elder's phone. Four big,
 * plain-language steps.
 */
@Composable
fun HelperWizardScreen(onBack: () -> Unit, viewModel: HelperWizardViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val easyMode by viewModel.easyMode.collectAsStateWithLifecycle()
    val contacts by viewModel.familyContacts.collectAsStateWithLifecycle()

    var step by remember { mutableIntStateOf(0) }
    var contact by remember { mutableStateOf("") }

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
                    text = stringResource(R.string.helper_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = stringResource(R.string.helper_step_of, step + 1, 4),
                style = MaterialTheme.typography.labelLarge,
                color = Saffron
            )

            when (step) {
                0 -> Step(
                    title = stringResource(R.string.helper_lang_title),
                    body = stringResource(R.string.helper_lang_body)
                ) {
                    OutlinedButton(
                        onClick = { open(context, Intent(Settings.ACTION_LOCALE_SETTINGS)) },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                    ) { Text(stringResource(R.string.helper_open_language), style = MaterialTheme.typography.titleMedium) }
                }

                1 -> Step(
                    title = stringResource(R.string.helper_easy_title),
                    body = stringResource(R.string.helper_easy_body)
                ) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.easy_mode_label),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(checked = easyMode, onCheckedChange = viewModel::setEasyMode)
                    }
                }

                2 -> Step(
                    title = stringResource(R.string.helper_notif_title),
                    body = stringResource(R.string.helper_notif_body)
                ) {
                    OutlinedButton(
                        onClick = { open(context, Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                    ) { Text(stringResource(R.string.helper_open_notif), style = MaterialTheme.typography.titleMedium) }
                }

                else -> Step(
                    title = stringResource(R.string.helper_contact_title),
                    body = stringResource(R.string.helper_contact_body)
                ) {
                    OutlinedTextField(
                        value = contact,
                        onValueChange = { contact = it },
                        label = { Text(stringResource(R.string.helper_contact_field)) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { viewModel.addContact(contact); contact = "" },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                    ) { Text(stringResource(R.string.helper_contact_add), style = MaterialTheme.typography.titleMedium) }
                    if (contacts.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.helper_contact_saved, contacts.size),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            Spacer(Modifier.height(Space2))
            Row(horizontalArrangement = Arrangement.spacedBy(Space3), modifier = Modifier.fillMaxWidth()) {
                if (step > 0) {
                    OutlinedButton(
                        onClick = { step -= 1 },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.weight(1f).heightIn(min = 60.dp)
                    ) { Text(stringResource(R.string.helper_prev), style = MaterialTheme.typography.titleMedium) }
                }
                Button(
                    onClick = { if (step < 3) step += 1 else onBack() },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f).heightIn(min = 60.dp)
                ) {
                    Text(
                        text = stringResource(if (step < 3) R.string.helper_next else R.string.helper_done),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            Text(
                text = stringResource(R.string.helper_plain_words),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun Step(title: String, body: String, content: @Composable () -> Unit) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(Space4), verticalArrangement = Arrangement.spacedBy(Space3)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(Space2))
            content()
        }
    }
}

private fun open(context: Context, intent: Intent) {
    context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}
