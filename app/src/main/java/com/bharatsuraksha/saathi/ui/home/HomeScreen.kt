package com.bharatsuraksha.saathi.ui.home

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bharatsuraksha.saathi.R
import com.bharatsuraksha.saathi.ui.theme.BrandGreen
import com.bharatsuraksha.saathi.ui.theme.BrandSaffron

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenResult: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenImage: () -> Unit,
    onOpenLink: () -> Unit,
    onOpenPhone: () -> Unit,
    onOpenTips: () -> Unit,
    onOpenRecover: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenEasy: () -> Unit,
    onOpenVoice: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val easyMode by viewModel.easyMode.collectAsStateWithLifecycle()
    val kidsMode by viewModel.kidsMode.collectAsStateWithLifecycle()
    val incoming by viewModel.incomingText.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    LaunchedEffect(incoming) {
        incoming?.let {
            viewModel.onTextChange(it)
            viewModel.onIncomingConsumed()
        }
    }
    LaunchedEffect(Unit) {
        viewModel.navigateToResult.collect { onOpenResult() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name), fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onOpenHistory) {
                        Icon(Icons.Filled.History, contentDescription = stringResource(R.string.home_history_button))
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.settings_title))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandGreen,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (kidsMode) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Text(
                        text = stringResource(R.string.home_kids_badge),
                        modifier = Modifier.padding(12.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = stringResource(R.string.home_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.home_subtitle),
                style = MaterialTheme.typography.bodyLarge
            )

            OutlinedTextField(
                value = state.text,
                onValueChange = viewModel::onTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
                label = { Text(stringResource(R.string.home_input_label)) },
                placeholder = { Text(stringResource(R.string.home_input_hint)) },
                isError = state.errorRes != null
            )

            state.errorRes?.let {
                Text(
                    text = stringResource(it),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = {
                    val clip = clipboard.getText()?.text
                    if (!clip.isNullOrBlank()) viewModel.onTextChange(clip)
                }) {
                    Icon(Icons.Filled.ContentPaste, contentDescription = null)
                    Spacer(Modifier.padding(4.dp))
                    Text(stringResource(R.string.home_paste_button))
                }
                OutlinedButton(onClick = { viewModel.onTextChange("") }) {
                    Text(stringResource(R.string.home_clear_button))
                }
            }

            // The one huge button.
            Button(
                onClick = viewModel::check,
                enabled = !state.checking,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
            ) {
                Icon(Icons.Filled.Search, contentDescription = null)
                Spacer(Modifier.padding(6.dp))
                Text(
                    text = stringResource(R.string.home_check_button),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = onOpenEasy,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandSaffron)
            ) {
                Text(stringResource(R.string.home_easy_button), fontWeight = FontWeight.Bold)
            }

            Text(
                text = stringResource(R.string.home_quick_tools),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ToolChip(stringResource(R.string.home_tool_image), onOpenImage, Modifier.weight(1f))
                ToolChip(stringResource(R.string.home_tool_link), onOpenLink, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ToolChip(stringResource(R.string.home_tool_phone), onOpenPhone, Modifier.weight(1f))
                ToolChip(stringResource(R.string.home_tool_tips), onOpenTips, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ToolChip(stringResource(R.string.home_tool_recover), onOpenRecover, Modifier.weight(1f))
                ToolChip(stringResource(R.string.home_tool_settings), onOpenSettings, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ToolChip(stringResource(R.string.easy_speak), onOpenVoice, Modifier.weight(1f))
            }

            Text(
                text = stringResource(R.string.home_examples_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            listOf(R.string.home_example_1, R.string.home_example_2, R.string.home_example_3).forEach { res ->
                val example = stringResource(res)
                AssistChip(
                    onClick = { viewModel.onTextChange(example) },
                    label = {
                        Text(
                            example.take(48) + if (example.length > 48) "…" else "",
                            maxLines = 1
                        )
                    }
                )
            }

            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.easy_mode_label),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(checked = easyMode, onCheckedChange = viewModel::onEasyModeChange)
                    }
                    TextButton(onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }) {
                        Text(stringResource(R.string.home_notif_button))
                    }
                }
            }

            Text(
                text = stringResource(R.string.history_privacy_note),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ToolChip(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(onClick = onClick, modifier = modifier) {
        Text(label, maxLines = 1, style = MaterialTheme.typography.labelLarge)
    }
}
