package com.bharatsuraksha.saathi.ui.learn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bharatsuraksha.saathi.R
import com.bharatsuraksha.saathi.domain.learn.LearnData
import com.bharatsuraksha.saathi.domain.learn.WeeklyScam
import com.bharatsuraksha.saathi.ui.common.isHindiLocale
import com.bharatsuraksha.saathi.ui.theme.Saffron
import com.bharatsuraksha.saathi.ui.theme.Space2
import com.bharatsuraksha.saathi.ui.theme.Space3
import com.bharatsuraksha.saathi.ui.theme.Space4
import com.bharatsuraksha.saathi.ui.theme.Space6

/** "इस हफ़्ते की ठगी" — twelve bundled scam cards you can swipe through. */
@Composable
fun WeeklyScamsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val hindi = isHindiLocale()
    val cards = remember { LearnData.loadWeekly(context) }
    val pagerState = rememberPagerState(pageCount = { cards.size })

    Scaffold(containerColor = Color.Transparent) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Space3)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = Space4),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) { Text(stringResource(R.string.action_back)) }
                Text(
                    text = stringResource(R.string.weekly_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${pagerState.currentPage + 1}/${cards.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = Saffron
                )
            }
            Text(
                text = stringResource(R.string.weekly_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Space4)
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                pageSpacing = Space3
            ) { page ->
                val card = cards[page]
                WeeklyCard(card = card, hindi = hindi, modifier = Modifier.padding(horizontal = Space4))
            }

            Text(
                text = stringResource(R.string.weekly_swipe_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Space4)
            )
            Text(
                text = stringResource(R.string.verdict_guarantee_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Space4)
            )
        }
    }
}

@Composable
private fun WeeklyCard(card: WeeklyScam, hindi: Boolean, modifier: Modifier = Modifier) {
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth().heightIn(min = 420.dp)
    ) {
        Column(Modifier.padding(Space4), verticalArrangement = Arrangement.spacedBy(Space3)) {
            Text(
                text = if (hindi) card.titleHi else card.titleEn,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Section(
                label = stringResource(R.string.weekly_how),
                body = if (hindi) card.howHi else card.howEn
            )
            Section(
                label = stringResource(R.string.weekly_signs),
                body = if (hindi) card.signsHi else card.signsEn
            )
            Section(
                label = stringResource(R.string.weekly_do),
                body = if (hindi) card.doHi else card.doEn,
                emphasis = true
            )
        }
    }
}

@Composable
private fun Section(label: String, body: String, emphasis: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(Space2)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = Saffron,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
