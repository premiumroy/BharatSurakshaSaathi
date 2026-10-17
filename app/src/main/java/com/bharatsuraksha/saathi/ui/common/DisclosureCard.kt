package com.bharatsuraksha.saathi.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bharatsuraksha.saathi.R

/**
 * The disclosure shown BEFORE any sensitive permission is requested. It says
 * what the permission does, that the data stays on the phone, and how to turn
 * it off. Nothing is requested without this being on screen first.
 */
@Composable
fun DisclosureCard(
    titleRes: Int,
    bodyRes: Int,
    offRes: Int,
    modifier: Modifier = Modifier
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Text(text = stringResource(bodyRes), style = MaterialTheme.typography.bodyMedium)
            Text(
                text = stringResource(R.string.disclosure_stays),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(text = stringResource(offRes), style = MaterialTheme.typography.bodyMedium)
            Text(
                text = stringResource(R.string.disclosure_change_mind),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
