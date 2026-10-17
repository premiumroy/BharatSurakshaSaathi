package com.bharatsuraksha.saathi.ui.safety

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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bharatsuraksha.saathi.R
import com.bharatsuraksha.saathi.ui.theme.Saffron
import com.bharatsuraksha.saathi.ui.theme.Space2
import com.bharatsuraksha.saathi.ui.theme.Space3
import com.bharatsuraksha.saathi.ui.theme.Space4

/** "मेरे नाम पर कितने सिम?" — how to check the connections registered in your name. */
@Composable
fun SimGuideScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val steps = listOf(
        R.string.sim_step_1,
        R.string.sim_step_2,
        R.string.sim_step_3,
        R.string.sim_step_4,
        R.string.sim_step_5
    )

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
                    text = stringResource(R.string.sim_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = stringResource(R.string.sim_intro),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            steps.forEachIndexed { index, res ->
                Card(shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(Space4), horizontalArrangement = Arrangement.spacedBy(Space3)) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Saffron,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = stringResource(res),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Button(
                onClick = {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://sancharsaathi.gov.in"))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                },
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp)
            ) { Text(stringResource(R.string.sim_open_portal), style = MaterialTheme.typography.titleMedium) }

            Text(
                text = stringResource(R.string.sim_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
