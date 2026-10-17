package com.bharatsuraksha.saathi.ui.safety

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import com.bharatsuraksha.saathi.R
import com.bharatsuraksha.saathi.domain.safety.ChakshuSummary
import com.bharatsuraksha.saathi.ui.common.isHindiLocale
import com.bharatsuraksha.saathi.ui.theme.Saffron
import com.bharatsuraksha.saathi.ui.theme.Space2
import com.bharatsuraksha.saathi.ui.theme.Space3
import com.bharatsuraksha.saathi.ui.theme.Space4

/**
 * Chakshu report assistant. It only prepares a copy-ready summary and opens the
 * portal. The user submits the report themselves; the app never sends it.
 */
@Composable
fun ChakshuScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val hindi = isHindiLocale()

    var number by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var medium by remember { mutableStateOf(ChakshuSummary.MEDIUM_CALL) }
    var category by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var screenshot by remember { mutableStateOf(false) }
    var copied by remember { mutableStateOf(false) }

    val report = ChakshuSummary.Report(number, date, time, medium, category, screenshot, notes)
    val summary = remember(report, hindi) { ChakshuSummary.build(report, hindi) }
    val complete = ChakshuSummary.isComplete(report)

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
                    text = stringResource(R.string.chakshu_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = stringResource(R.string.chakshu_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = number,
                onValueChange = { number = it },
                label = { Text(stringResource(R.string.chakshu_number)) },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(Space3), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text(stringResource(R.string.chakshu_date)) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text(stringResource(R.string.chakshu_time)) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = stringResource(R.string.chakshu_medium),
                style = MaterialTheme.typography.titleMedium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(Space2), modifier = Modifier.fillMaxWidth()) {
                MediumChip(stringResource(R.string.chakshu_medium_call), medium == ChakshuSummary.MEDIUM_CALL, Modifier.weight(1f)) {
                    medium = ChakshuSummary.MEDIUM_CALL
                }
                MediumChip(stringResource(R.string.chakshu_medium_sms), medium == ChakshuSummary.MEDIUM_SMS, Modifier.weight(1f)) {
                    medium = ChakshuSummary.MEDIUM_SMS
                }
                MediumChip("WhatsApp", medium == ChakshuSummary.MEDIUM_WHATSAPP, Modifier.weight(1f)) {
                    medium = ChakshuSummary.MEDIUM_WHATSAPP
                }
            }

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text(stringResource(R.string.chakshu_category)) },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(stringResource(R.string.chakshu_notes)) },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.chakshu_screenshot),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(checked = screenshot, onCheckedChange = { screenshot = it })
            }

            Card(shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(Space4), verticalArrangement = Arrangement.spacedBy(Space2)) {
                    Text(
                        text = stringResource(R.string.chakshu_summary),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(text = summary, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Button(
                onClick = {
                    copy(context, summary)
                    copied = true
                },
                shape = RoundedCornerShape(50),
                enabled = complete,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
            ) { Text(stringResource(R.string.chakshu_copy)) }
            if (!complete) {
                Text(
                    text = stringResource(R.string.chakshu_incomplete),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            if (copied) {
                Text(
                    text = stringResource(R.string.chakshu_copied),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Saffron
                )
            }

            OutlinedButton(
                onClick = {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://sancharsaathi.gov.in"))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                },
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
            ) { Text(stringResource(R.string.chakshu_open_portal)) }

            Text(
                text = stringResource(R.string.chakshu_never_auto),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MediumChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    if (selected) {
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(50),
            modifier = modifier.heightIn(min = 52.dp)
        ) { Text(label, style = MaterialTheme.typography.labelLarge) }
    } else {
        OutlinedButton(
            onClick = onClick,
            shape = RoundedCornerShape(50),
            modifier = modifier.heightIn(min = 52.dp)
        ) { Text(label, style = MaterialTheme.typography.labelLarge) }
    }
}

private fun copy(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
    clipboard.setPrimaryClip(ClipData.newPlainText("Chakshu report", text))
}
