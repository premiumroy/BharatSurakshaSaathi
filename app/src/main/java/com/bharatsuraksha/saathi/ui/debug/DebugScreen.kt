package com.bharatsuraksha.saathi.ui.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bharatsuraksha.saathi.R
import com.bharatsuraksha.saathi.ui.session.LastResultStore
import com.bharatsuraksha.saathi.ui.theme.BrandGreen

/**
 * Hidden diagnostic screen: shows exactly which rules and families fired for the
 * last check. Reachable by tapping the "About" heading in Settings seven times.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(onBack: () -> Unit) {
    val result = LastResultStore.result
    val message = LastResultStore.message

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.debug_title), fontWeight = FontWeight.Bold) },
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (result == null) {
                Text(stringResource(R.string.debug_no_result))
                return@Column
            }
            Text(stringResource(R.string.debug_last_message), fontWeight = FontWeight.Bold)
            Text(message, style = MaterialTheme.typography.bodyMedium)
            Text(
                "verdict=${result.verdict}  score=${result.score}  confidence=${result.confidence}",
                fontWeight = FontWeight.Bold
            )
            Text("families = [${result.familyIds.joinToString(", ")}]")
            Text("links = [${result.links.joinToString(", ")}]")

            Text(stringResource(R.string.debug_matched_rules), fontWeight = FontWeight.Bold)
            if (result.matchedRuleIds.isEmpty()) {
                Text("—")
            } else {
                result.matchedRuleIds.forEach { id ->
                    Card { Text(id, Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium) }
                }
            }

            Text(stringResource(R.string.debug_reasons), fontWeight = FontWeight.Bold)
            result.reasons.forEach { r ->
                Card {
                    Text(
                        "• ${r.id} (weight=${r.weight}, family=${r.family})\n${r.reasonEn}",
                        Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
