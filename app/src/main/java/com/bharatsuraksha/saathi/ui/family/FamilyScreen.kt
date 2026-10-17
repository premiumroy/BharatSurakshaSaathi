package com.bharatsuraksha.saathi.ui.family

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bharatsuraksha.saathi.R
import com.bharatsuraksha.saathi.ui.common.ComingSoonPanel
import com.bharatsuraksha.saathi.ui.theme.JyotiHero
import com.bharatsuraksha.saathi.ui.theme.JyotiState
import com.bharatsuraksha.saathi.ui.theme.Space6
import com.bharatsuraksha.saathi.ui.theme.entrance
import com.bharatsuraksha.saathi.ui.theme.motionAllowed
import com.bharatsuraksha.saathi.ui.theme.rememberLowRam

/**
 * Family tab. Phase 2 fills this with the code word and the helper wizard; for
 * now it is an honest placeholder with the guardian lamp.
 */
@Composable
fun FamilyScreen() {
    val motion = motionAllowed()
    val lowRam = rememberLowRam()

    Scaffold(containerColor = Color.Transparent) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(Space6),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.size(150.dp).entrance(0, motion)) {
                JyotiHero(state = JyotiState.Idle, motion = motion, lowRam = lowRam)
            }
            Text(
                text = stringResource(R.string.tab_family),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth().entrance(1, motion)
            )
            ComingSoonPanel(
                titleRes = R.string.family_coming_soon,
                bodyRes = R.string.family_coming_body,
                modifier = Modifier.entrance(2, motion)
            )
        }
    }
}
