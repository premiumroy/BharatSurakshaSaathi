package com.bharatsuraksha.saathi.ui.safety

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bharatsuraksha.saathi.R
import com.bharatsuraksha.saathi.ui.theme.Haldi
import com.bharatsuraksha.saathi.ui.theme.JyotiBook
import com.bharatsuraksha.saathi.ui.theme.JyotiCall
import com.bharatsuraksha.saathi.ui.theme.JyotiCamera
import com.bharatsuraksha.saathi.ui.theme.JyotiLamp
import com.bharatsuraksha.saathi.ui.theme.JyotiShield
import com.bharatsuraksha.saathi.ui.theme.Neem
import com.bharatsuraksha.saathi.ui.theme.Saffron
import com.bharatsuraksha.saathi.ui.theme.Space3
import com.bharatsuraksha.saathi.ui.theme.Space4
import com.bharatsuraksha.saathi.ui.theme.entrance
import com.bharatsuraksha.saathi.ui.theme.motionAllowed
import com.bharatsuraksha.saathi.ui.theme.tappable

/** The safety-tools hub: everything from Phase 3 in one list. */
@Composable
fun SafetyToolsScreen(
    onBack: () -> Unit,
    onOpenDashboard: () -> Unit,
    onOpenShareCard: () -> Unit,
    onOpenHelpNow: () -> Unit,
    onOpenChakshu: () -> Unit,
    onOpenSimGuide: () -> Unit,
    onOpenHelplines: () -> Unit,
    onOpenQr: () -> Unit,
    onOpenAgent: () -> Unit,
    onOpenAgentDashboard: () -> Unit
) {
    val motion = motionAllowed()
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
                    text = stringResource(R.string.safety_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = stringResource(R.string.safety_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Tool(JyotiShield, Neem, R.string.safety_agent, R.string.safety_agent_sub, 0, motion, onOpenAgent)
            Tool(JyotiLamp, Saffron, R.string.safety_agent_dash, R.string.safety_agent_dash_sub, 1, motion, onOpenAgentDashboard)
            Tool(JyotiCamera, Saffron, R.string.safety_qr, R.string.safety_qr_sub, 0, motion, onOpenQr)
            Tool(JyotiShield, Saffron, R.string.safety_dashboard, R.string.safety_dashboard_sub, 1, motion, onOpenDashboard)
            Tool(JyotiCamera, Saffron, R.string.safety_share_card, R.string.safety_share_card_sub, 1, motion, onOpenShareCard)
            Tool(JyotiCall, Neem, R.string.safety_help_now, R.string.safety_help_now_sub, 2, motion, onOpenHelpNow)
            Tool(JyotiBook, Haldi, R.string.safety_chakshu, R.string.safety_chakshu_sub, 3, motion, onOpenChakshu)
            Tool(JyotiLamp, Neem, R.string.safety_sim, R.string.safety_sim_sub, 4, motion, onOpenSimGuide)
            Tool(JyotiShield, Haldi, R.string.safety_helplines, R.string.safety_helplines_sub, 5, motion, onOpenHelplines)

            Text(
                text = stringResource(R.string.safety_privacy),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun Tool(
    icon: ImageVector,
    tint: Color,
    titleRes: Int,
    bodyRes: Int,
    index: Int,
    motion: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .entrance(index, motion)
            .tappable(motion = motion, onClick = onClick)
    ) {
        Row(
            Modifier.padding(Space4),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space3)
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(30.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(bodyRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
