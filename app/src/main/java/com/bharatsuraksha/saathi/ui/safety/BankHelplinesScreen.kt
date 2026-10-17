package com.bharatsuraksha.saathi.ui.safety

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
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bharatsuraksha.saathi.R
import com.bharatsuraksha.saathi.domain.safety.BankHelpline
import com.bharatsuraksha.saathi.ui.session.BankHelplineStore
import com.bharatsuraksha.saathi.ui.theme.Saffron
import com.bharatsuraksha.saathi.ui.theme.Space2
import com.bharatsuraksha.saathi.ui.theme.Space3
import com.bharatsuraksha.saathi.ui.theme.Space4

/**
 * Trusted bank helplines, typed in by the user from their own card or passbook.
 * No bundled list ships with the app, because a wrong number is dangerous.
 */
@Composable
fun BankHelplinesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val entries by BankHelplineStore.entries.collectAsState()

    var bank by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<Int?>(null) }

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
                    text = stringResource(R.string.helpline_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = stringResource(R.string.helpline_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(Space4), verticalArrangement = Arrangement.spacedBy(Space3)) {
                    OutlinedTextField(
                        value = bank,
                        onValueChange = { bank = it; error = null },
                        label = { Text(stringResource(R.string.helpline_bank)) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = number,
                        onValueChange = { number = it; error = null },
                        label = { Text(stringResource(R.string.helpline_number)) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    error?.let {
                        Text(
                            text = stringResource(it),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Button(
                        onClick = {
                            val entry = BankHelpline.Entry(bank, number)
                            when (BankHelpline.validate(entry)) {
                                BankHelpline.REASON_BANK -> error = R.string.helpline_reason_bank
                                BankHelpline.REASON_NUMBER -> error = R.string.helpline_reason_number
                                null -> {
                                    BankHelplineStore.add(context, entry)
                                    bank = ""
                                    number = ""
                                }
                                else -> Unit
                            }
                        },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                    ) { Text(stringResource(R.string.helpline_add)) }
                }
            }

            if (entries.isEmpty()) {
                Text(
                    text = stringResource(R.string.helpline_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                entries.forEach { entry ->
                    Card(shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.padding(Space4),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = entry.bank,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = entry.number,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Saffron
                                )
                            }
                            TextButton(onClick = { BankHelplineStore.remove(context, entry) }) {
                                Text(stringResource(R.string.helpline_remove))
                            }
                        }
                    }
                }
            }

            Text(
                text = stringResource(R.string.helpline_source_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.helpline_privacy),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
