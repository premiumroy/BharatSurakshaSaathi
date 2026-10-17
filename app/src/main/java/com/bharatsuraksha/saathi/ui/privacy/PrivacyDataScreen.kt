package com.bharatsuraksha.saathi.ui.privacy

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bharatsuraksha.saathi.R
import com.bharatsuraksha.saathi.ui.common.DisclosureCard
import com.bharatsuraksha.saathi.ui.settings.SettingsViewModel
import com.bharatsuraksha.saathi.ui.theme.Sindoor

/**
 * "Privacy and data". Every sensitive permission is listed with what it does,
 * the promise that the data stays on the phone, and how to turn it off. The
 * delete button erases everything this app has stored.
 */
@Composable
fun PrivacyDataScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    var confirming by remember { mutableStateOf(false) }
    var deleted by remember { mutableStateOf(false) }

    Scaffold(containerColor = Color.Transparent) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onBack) { Text(stringResource(R.string.action_back)) }
                Text(
                    text = stringResource(R.string.privacy_data_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = stringResource(R.string.privacy_data_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = stringResource(R.string.privacy_data_perms_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            DisclosureCard(
                titleRes = R.string.disclosure_notif_title,
                bodyRes = R.string.disclosure_notif_body,
                offRes = R.string.disclosure_notif_off
            )
            DisclosureCard(
                titleRes = R.string.disclosure_mic_title,
                bodyRes = R.string.disclosure_mic_body,
                offRes = R.string.disclosure_mic_off
            )
            DisclosureCard(
                titleRes = R.string.disclosure_camera_title,
                bodyRes = R.string.disclosure_camera_body,
                offRes = R.string.disclosure_camera_off
            )
            DisclosureCard(
                titleRes = R.string.disclosure_call_title,
                bodyRes = R.string.disclosure_call_body,
                offRes = R.string.disclosure_call_off
            )
            DisclosureCard(
                titleRes = R.string.disclosure_usage_title,
                bodyRes = R.string.disclosure_usage_body,
                offRes = R.string.disclosure_usage_off
            )

            if (deleted) {
                Text(
                    text = stringResource(R.string.privacy_data_deleted),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Button(
                onClick = { confirming = true },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Sindoor,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
            ) { Text(stringResource(R.string.privacy_data_delete_all)) }

            Text(
                text = stringResource(R.string.privacy_data_delete_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (confirming) {
        AlertDialog(
            onDismissRequest = { confirming = false },
            title = { Text(stringResource(R.string.privacy_data_delete_confirm_title)) },
            text = { Text(stringResource(R.string.privacy_data_delete_confirm_body)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAllData()
                    confirming = false
                    deleted = true
                }) { Text(stringResource(R.string.privacy_data_delete_yes)) }
            },
            dismissButton = {
                TextButton(onClick = { confirming = false }) {
                    Text(stringResource(R.string.history_cancel))
                }
            }
        )
    }
}
