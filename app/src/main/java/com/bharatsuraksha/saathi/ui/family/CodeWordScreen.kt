package com.bharatsuraksha.saathi.ui.family

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
import androidx.compose.runtime.collectAsState
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
import com.bharatsuraksha.saathi.domain.learn.CodeWordFlow
import com.bharatsuraksha.saathi.ui.session.CodeWordStore
import com.bharatsuraksha.saathi.ui.theme.Neem
import com.bharatsuraksha.saathi.ui.theme.Saffron
import com.bharatsuraksha.saathi.ui.theme.Sindoor
import com.bharatsuraksha.saathi.ui.theme.Space2
import com.bharatsuraksha.saathi.ui.theme.Space3
import com.bharatsuraksha.saathi.ui.theme.Space4

/**
 * The family code word (stored encrypted on this phone) and the "someone is
 * asking for money in an emergency" check.
 */
@Composable
fun CodeWordScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val saved by CodeWordStore.word.collectAsState()

    var draft by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    var fromNewNumber by remember { mutableStateOf(true) }
    var asksMoney by remember { mutableStateOf(true) }
    var urgent by remember { mutableStateOf(false) }
    var onVideo by remember { mutableStateOf(false) }
    var matched by remember { mutableStateOf(false) }

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
                    text = stringResource(R.string.codeword_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Text(
                text = stringResource(R.string.codeword_explain),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // ---- set the code word ----
            Card(shape = MaterialTheme.shapes.medium) {
                Column(Modifier.padding(Space4), verticalArrangement = Arrangement.spacedBy(Space3)) {
                    Text(
                        text = if (saved.isNullOrBlank()) {
                            stringResource(R.string.codeword_set_title)
                        } else {
                            stringResource(R.string.codeword_change_title)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it; error = false },
                        label = { Text(stringResource(R.string.codeword_field)) },
                        isError = error,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (error) {
                        Text(
                            text = stringResource(R.string.codeword_invalid),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Button(
                        onClick = {
                            if (CodeWordFlow.isCodeWordAcceptable(draft)) {
                                CodeWordStore.set(context, draft)
                                draft = ""
                            } else {
                                error = true
                            }
                        },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                    ) { Text(stringResource(R.string.codeword_save)) }
                    if (!saved.isNullOrBlank()) {
                        Text(
                            text = stringResource(R.string.codeword_saved),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Neem
                        )
                        TextButton(onClick = { CodeWordStore.clear(context) }) {
                            Text(stringResource(R.string.codeword_remove))
                        }
                    }
                    Text(
                        text = stringResource(R.string.codeword_storage_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ---- the emergency check ----
            Text(
                text = stringResource(R.string.emergency_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.emergency_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(shape = MaterialTheme.shapes.medium) {
                Column(Modifier.padding(Space4), verticalArrangement = Arrangement.spacedBy(Space2)) {
                    Toggle(stringResource(R.string.emergency_new_number), fromNewNumber) { fromNewNumber = it }
                    Toggle(stringResource(R.string.emergency_asks_money), asksMoney) { asksMoney = it }
                    Toggle(stringResource(R.string.emergency_urgent), urgent) { urgent = it }
                    Toggle(stringResource(R.string.emergency_video), onVideo) { onVideo = it }
                    Toggle(
                        stringResource(R.string.emergency_code_matched),
                        matched,
                        enabled = !saved.isNullOrBlank()
                    ) { matched = it }
                }
            }

            val advice = CodeWordFlow.assess(
                CodeWordFlow.Situation(
                    hasCodeWord = !saved.isNullOrBlank(),
                    codeWordMatches = matched,
                    asksMoney = asksMoney,
                    urgent = urgent,
                    fromNewNumber = fromNewNumber,
                    onVideoCall = onVideo
                )
            )

            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = when (advice.level) {
                        CodeWordFlow.Level.LIKELY_FRAUD -> Sindoor.copy(alpha = 0.16f)
                        CodeWordFlow.Level.VERIFY_FIRST -> Saffron.copy(alpha = 0.16f)
                        CodeWordFlow.Level.LIKELY_REAL -> Neem.copy(alpha = 0.16f)
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(Space4), verticalArrangement = Arrangement.spacedBy(Space2)) {
                    Text(
                        text = stringResource(
                            when (advice.level) {
                                CodeWordFlow.Level.LIKELY_FRAUD -> R.string.emergency_verdict_fraud
                                CodeWordFlow.Level.VERIFY_FIRST -> R.string.emergency_verdict_verify
                                CodeWordFlow.Level.LIKELY_REAL -> R.string.emergency_verdict_real
                            }
                        ),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    advice.reasonKeys.forEach { key ->
                        Text(text = stringResource(reasonRes(key)), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // ---- what to do, and the clone warnings ----
            Card(shape = MaterialTheme.shapes.medium) {
                Column(Modifier.padding(Space4), verticalArrangement = Arrangement.spacedBy(Space2)) {
                    Text(
                        text = stringResource(R.string.emergency_checklist_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    CodeWordFlow.CHECKLIST_KEYS.forEach { key ->
                        Text("• " + stringResource(checkRes(key)), style = MaterialTheme.typography.bodyLarge)
                    }
                    Text(
                        text = stringResource(R.string.emergency_clone_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(top = Space2)
                    )
                    CodeWordFlow.VOICE_CLONE_KEYS.forEach { key ->
                        Text("• " + stringResource(cloneRes(key)), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
            ) { Text(stringResource(R.string.action_back)) }
            Text(
                text = stringResource(R.string.verdict_guarantee_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun Toggle(label: String, checked: Boolean, enabled: Boolean = true, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = checked, onCheckedChange = onChange, enabled = enabled)
    }
}

private fun reasonRes(key: String): Int = when (key) {
    CodeWordFlow.REASON_NEW_NUMBER -> R.string.codeword_reason_new_number
    CodeWordFlow.REASON_MONEY -> R.string.codeword_reason_money
    CodeWordFlow.REASON_URGENT -> R.string.codeword_reason_urgent
    CodeWordFlow.REASON_VIDEO -> R.string.codeword_reason_video
    CodeWordFlow.REASON_MATCHED -> R.string.codeword_reason_matched
    else -> R.string.emergency_intro
}

private fun checkRes(key: String): Int = when (key) {
    "codeword_check_1" -> R.string.codeword_check_1
    "codeword_check_2" -> R.string.codeword_check_2
    else -> R.string.codeword_check_3
}

private fun cloneRes(key: String): Int = when (key) {
    "codeword_clone_1" -> R.string.codeword_clone_1
    "codeword_clone_2" -> R.string.codeword_clone_2
    else -> R.string.codeword_clone_3
}
