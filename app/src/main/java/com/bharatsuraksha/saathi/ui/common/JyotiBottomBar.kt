package com.bharatsuraksha.saathi.ui.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bharatsuraksha.saathi.R
import com.bharatsuraksha.saathi.ui.theme.JyotiBook
import com.bharatsuraksha.saathi.ui.theme.JyotiFamily
import com.bharatsuraksha.saathi.ui.theme.JyotiLamp
import com.bharatsuraksha.saathi.ui.theme.JyotiShield
import com.bharatsuraksha.saathi.ui.theme.MotionTokens
import com.bharatsuraksha.saathi.ui.theme.Saffron
import com.bharatsuraksha.saathi.ui.theme.tappable

data class JyotiTab(val route: String, val icon: ImageVector, val labelRes: Int)

/** The four tabs. Learn and Family are Phase 2 placeholders. */
fun jyotiTabs(learn: String, family: String, home: String, history: String) = listOf(
    JyotiTab(home, JyotiShield, R.string.tab_home),
    JyotiTab(learn, JyotiBook, R.string.tab_learn),
    JyotiTab(family, JyotiFamily, R.string.tab_family),
    JyotiTab(history, JyotiLamp, R.string.tab_history)
)

/**
 * Bottom navigation with a flame indicator that glides between the four tabs.
 */
@Composable
fun JyotiBottomBar(
    tabs: List<JyotiTab>,
    currentRoute: String?,
    motion: Boolean,
    onSelect: (String) -> Unit
) {
    val selected = tabs.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
    val position by animateFloatAsState(
        targetValue = selected.toFloat(),
        animationSpec = if (motion) MotionTokens.Settle else androidx.compose.animation.core.tween(0),
        label = "navIndicator"
    )
    val count = tabs.size.coerceAtLeast(1)

    Box(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(76.dp)
    ) {
        Canvas(Modifier.matchParentSize()) {
            val slot = size.width / count
            val cx = slot * (position + 0.5f)
            val cy = size.height * 0.34f
            val h = 16.dp.toPx()
            val w = 7.dp.toPx()
            val flame = Path().apply {
                moveTo(cx, cy - h)
                cubicTo(cx + w, cy - h * 0.5f, cx + w * 0.7f, cy - h * 0.1f, cx, cy)
                cubicTo(cx - w * 0.7f, cy - h * 0.1f, cx - w, cy - h * 0.5f, cx, cy - h)
                close()
            }
            drawPath(flame, Saffron.copy(alpha = 0.92f))
        }

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = index == selected
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .tappable(onClickLabel = stringResource(tab.labelRes), motion = motion) { onSelect(tab.route) }
                        .padding(top = 16.dp, bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = stringResource(tab.labelRes),
                        tint = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = stringResource(tab.labelRes),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/** A calm, centred "coming soon" panel used by the Phase 2 tabs. */
@Composable
fun ComingSoonPanel(titleRes: Int, bodyRes: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(bodyRes),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.coming_soon_tag),
            style = MaterialTheme.typography.labelLarge,
            color = Saffron
        )
    }
}
