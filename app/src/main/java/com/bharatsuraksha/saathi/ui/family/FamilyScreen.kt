package com.bharatsuraksha.saathi.ui.family

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bharatsuraksha.saathi.R
import com.bharatsuraksha.saathi.ui.session.CodeWordStore
import com.bharatsuraksha.saathi.ui.theme.Haldi
import com.bharatsuraksha.saathi.ui.theme.JyotiFamily
import com.bharatsuraksha.saathi.ui.theme.JyotiHero
import com.bharatsuraksha.saathi.ui.theme.JyotiLamp
import com.bharatsuraksha.saathi.ui.theme.JyotiState
import com.bharatsuraksha.saathi.ui.theme.Neem
import com.bharatsuraksha.saathi.ui.theme.Space2
import com.bharatsuraksha.saathi.ui.theme.Space3
import com.bharatsuraksha.saathi.ui.theme.Space4
import com.bharatsuraksha.saathi.ui.theme.entrance
import com.bharatsuraksha.saathi.ui.theme.motionAllowed
import com.bharatsuraksha.saathi.ui.theme.rememberLowRam
import com.bharatsuraksha.saathi.ui.theme.tappable

@Composable
fun FamilyScreen(
    onOpenCodeWord: () -> Unit,
    onOpenHelper: () -> Unit
) {
    val motion = motionAllowed()
    val lowRam = rememberLowRam()
    val codeWord by CodeWordStore.word.collectAsState()

    Scaffold(containerColor = Color.Transparent) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Space4),
            verticalArrangement = Arrangement.spacedBy(Space4)
        ) {
            Row(
                Modifier.fillMaxWidth().entrance(0, motion),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space2)
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.tab_family),
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.size(Space2))
                    Text(
                        text = stringResource(R.string.family_intro_phase2),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(Modifier.size(96.dp)) {
                    JyotiHero(state = JyotiState.Idle, motion = motion, lowRam = lowRam)
                }
            }

            FamilyCard(
                icon = JyotiLamp,
                tint = Neem,
                titleRes = R.string.codeword_title,
                bodyRes = if (codeWord.isNullOrBlank()) R.string.codeword_intro_none else R.string.codeword_intro_set,
                index = 1,
                motion = motion,
                onClick = onOpenCodeWord
            )
            FamilyCard(
                icon = JyotiFamily,
                tint = Haldi,
                titleRes = R.string.helper_title,
                bodyRes = R.string.helper_intro,
                index = 2,
                motion = motion,
                onClick = onOpenHelper
            )

            Text(
                text = stringResource(R.string.codeword_privacy_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.entrance(3, motion)
            )
            Text(
                text = stringResource(R.string.verdict_guarantee_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.entrance(4, motion)
            )
        }
    }
}

@Composable
private fun FamilyCard(
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
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(32.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(titleRes),
                    style = MaterialTheme.typography.titleLarge,
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
