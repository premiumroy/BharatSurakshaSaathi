package com.bharatsuraksha.saathi.ui.learn

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
import com.bharatsuraksha.saathi.domain.learn.StreakLogic
import com.bharatsuraksha.saathi.ui.session.GameStatsStore
import com.bharatsuraksha.saathi.ui.theme.Haldi
import com.bharatsuraksha.saathi.ui.theme.JyotiBook
import com.bharatsuraksha.saathi.ui.theme.JyotiCall
import com.bharatsuraksha.saathi.ui.theme.JyotiHero
import com.bharatsuraksha.saathi.ui.theme.JyotiShield
import com.bharatsuraksha.saathi.ui.theme.JyotiState
import com.bharatsuraksha.saathi.ui.theme.Neem
import com.bharatsuraksha.saathi.ui.theme.Saffron
import com.bharatsuraksha.saathi.ui.theme.Space2
import com.bharatsuraksha.saathi.ui.theme.Space3
import com.bharatsuraksha.saathi.ui.theme.Space4
import com.bharatsuraksha.saathi.ui.theme.Space6
import com.bharatsuraksha.saathi.ui.theme.entrance
import com.bharatsuraksha.saathi.ui.theme.motionAllowed
import com.bharatsuraksha.saathi.ui.theme.rememberLowRam
import com.bharatsuraksha.saathi.ui.theme.tappable

@Composable
fun LearnScreen(
    onOpenGame: () -> Unit,
    onOpenTriage: () -> Unit,
    onOpenWeekly: () -> Unit
) {
    val motion = motionAllowed()
    val lowRam = rememberLowRam()
    val stats by GameStatsStore.stats.collectAsState()

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
                        text = stringResource(R.string.tab_learn),
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.size(Space2))
                    Text(
                        text = stringResource(R.string.learn_intro),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(Modifier.size(96.dp)) {
                    JyotiHero(state = JyotiState.Idle, motion = motion, lowRam = lowRam)
                }
            }

            StreakCard(stats = stats, modifier = Modifier.entrance(1, motion))

            LearnCard(
                icon = JyotiShield,
                tint = Saffron,
                titleRes = R.string.spot_title,
                bodyRes = R.string.spot_intro,
                index = 2,
                motion = motion,
                onClick = onOpenGame
            )
            LearnCard(
                icon = JyotiCall,
                tint = Neem,
                titleRes = R.string.triage_title,
                bodyRes = R.string.triage_intro,
                index = 3,
                motion = motion,
                onClick = onOpenTriage
            )
            LearnCard(
                icon = JyotiBook,
                tint = Haldi,
                titleRes = R.string.weekly_title,
                bodyRes = R.string.weekly_intro,
                index = 4,
                motion = motion,
                onClick = onOpenWeekly
            )
            Text(
                text = stringResource(R.string.learn_disclaimer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.entrance(5, motion)
            )
        }
    }
}

@Composable
private fun StreakCard(stats: StreakLogic.Stats, modifier: Modifier = Modifier) {
    Card(shape = MaterialTheme.shapes.medium, modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(Space4), verticalArrangement = Arrangement.spacedBy(Space2)) {
            Text(
                text = stringResource(R.string.spot_your_progress),
                style = MaterialTheme.typography.titleMedium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(Space4)) {
                Stat(stringResource(R.string.spot_streak), stats.streak.toString())
                Stat(stringResource(R.string.spot_best), stats.bestStreak.toString())
                Stat(stringResource(R.string.spot_accuracy), "${stats.accuracy}%")
            }
            Text(
                text = stringResource(R.string.spot_badges_count, stats.badges.size, StreakLogic.ALL_BADGES.size),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Column {
        Text(text = value, style = MaterialTheme.typography.headlineMedium, color = Saffron)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LearnCard(
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
