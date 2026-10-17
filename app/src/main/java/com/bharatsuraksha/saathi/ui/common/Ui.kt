package com.bharatsuraksha.saathi.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bharatsuraksha.saathi.R
import com.bharatsuraksha.saathi.domain.model.Verdict
import com.bharatsuraksha.saathi.ui.theme.BrandGold
import com.bharatsuraksha.saathi.ui.theme.BrandGreen
import com.bharatsuraksha.saathi.ui.theme.BrandSaffron
import com.bharatsuraksha.saathi.ui.theme.CarefulAmber
import com.bharatsuraksha.saathi.ui.theme.CarefulAmberBg
import com.bharatsuraksha.saathi.ui.theme.DangerRed
import com.bharatsuraksha.saathi.ui.theme.DangerRedBg
import com.bharatsuraksha.saathi.ui.theme.SafeGreen
import com.bharatsuraksha.saathi.ui.theme.SafeGreenBg

/** True when the phone/app language is Hindi. */
@Composable
fun isHindiLocale(): Boolean {
    val configuration = LocalConfiguration.current
    val locales = configuration.locales
    return locales.size() > 0 && locales[0] != null && locales[0].language == "hi"
}

fun verdictColor(verdict: Verdict): Color = when (verdict) {
    Verdict.SAFE -> SafeGreen
    Verdict.CAREFUL -> CarefulAmber
    Verdict.DANGEROUS -> DangerRed
}

fun verdictBackground(verdict: Verdict): Color = when (verdict) {
    Verdict.SAFE -> SafeGreenBg
    Verdict.CAREFUL -> CarefulAmberBg
    Verdict.DANGEROUS -> DangerRedBg
}

fun verdictLabelRes(verdict: Verdict): Int = when (verdict) {
    Verdict.SAFE -> R.string.verdict_safe
    Verdict.CAREFUL -> R.string.verdict_careful
    Verdict.DANGEROUS -> R.string.verdict_dangerous
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
}

/** A wide, tall primary action button. */
@Composable
fun BigButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    container: Color = MaterialTheme.colorScheme.primary
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp),
        colors = ButtonDefaults.buttonColors(containerColor = container)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

/** Coloured verdict chip. */
@Composable
fun VerdictPill(verdict: Verdict, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(verdictBackground(verdict), RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = androidx.compose.ui.res.stringResource(verdictLabelRes(verdict)),
            color = verdictColor(verdict),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

/** Thin tricolour accent bar — a small Indian touch under the app bar. */
@Composable
fun TricolorBar(modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().height(4.dp)) {
        Box(Modifier.weight(1f).fillMaxHeight().background(BrandSaffron))
        Box(Modifier.weight(1f).fillMaxHeight().background(Color(0xFFF3E6CE)))
        Box(Modifier.weight(1f).fillMaxHeight().background(BrandGreen))
    }
}

/** Company / maker credit shown on the home and about surfaces. */
@Composable
fun BrandFooter(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = stringResource(R.string.brand_made_by),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = BrandGold
        )
        Text(
            text = stringResource(R.string.brand_owner),
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = stringResource(R.string.brand_alias),
            style = MaterialTheme.typography.bodySmall
        )
    }
}
