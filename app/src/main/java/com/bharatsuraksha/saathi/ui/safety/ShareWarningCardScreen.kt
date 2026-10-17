package com.bharatsuraksha.saathi.ui.safety

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.toIntSize
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.bharatsuraksha.saathi.R
import com.bharatsuraksha.saathi.domain.safety.Masking
import com.bharatsuraksha.saathi.ui.common.isHindiLocale
import com.bharatsuraksha.saathi.ui.session.LastResultStore
import com.bharatsuraksha.saathi.ui.theme.Saffron
import com.bharatsuraksha.saathi.ui.theme.Space2
import com.bharatsuraksha.saathi.ui.theme.Space3
import com.bharatsuraksha.saathi.ui.theme.Space4
import com.bharatsuraksha.saathi.ui.theme.verdictAccent
import com.bharatsuraksha.saathi.ui.theme.verdictContainer
import com.bharatsuraksha.saathi.ui.theme.verdictOnContainer
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

/**
 * Builds a shareable warning card image and sends it through the Android share
 * sheet. Phone numbers, UPI IDs, account numbers and links in the quoted
 * message are masked first, and the user can switch that off.
 */
@Composable
fun ShareWarningCardScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val hindi = isHindiLocale()
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    val result = LastResultStore.result
    val original = LastResultStore.message

    var masking by remember { mutableStateOf(true) }
    var status by remember { mutableStateOf<Int?>(null) }

    val layer = rememberGraphicsLayer()
    val shownText = if (masking) Masking.mask(original) else original

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
                    text = stringResource(R.string.share_card_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (result == null) {
                Text(
                    text = stringResource(R.string.share_card_none),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // The preview is the exact composable that gets captured.
                Box(
                    Modifier
                        .fillMaxWidth()
                        .drawWithContent {
                            layer.record(
                                density = this,
                                layoutDirection = this.layoutDirection,
                                size = this.size.toIntSize(),
                                block = { this@drawWithContent.drawContent() }
                            )
                            drawLayer(layer)
                        }
                ) {
                    WarningCard(
                        verdictLabel = stringResource(verdictRes(result.verdict)),
                        verdictColor = verdictAccent(result.verdict),
                        container = verdictContainer(result.verdict),
                        onContainer = verdictOnContainer(result.verdict),
                        reasons = result.reasons.take(3).map { it.text(hindi) },
                        quoted = shownText,
                        tips = listOf(
                            stringResource(R.string.share_tip_1),
                            stringResource(R.string.share_tip_2),
                            stringResource(R.string.share_tip_3)
                        ),
                        appName = stringResource(R.string.app_name),
                        guarantee = stringResource(R.string.verdict_guarantee_note)
                    )
                }

                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.share_card_mask),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(R.string.share_card_mask_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = masking, onCheckedChange = { masking = it })
                }

                Button(
                    onClick = {
                        scope.launch {
                            val ok = shareCard(context, layer, density.density, shownText, hindi)
                            status = if (ok) R.string.share_card_ok else R.string.share_card_failed
                        }
                    },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp)
                ) { Text(stringResource(R.string.share_card_share), style = MaterialTheme.typography.titleLarge) }

                status?.let {
                    Text(
                        text = stringResource(it),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = stringResource(R.string.share_card_privacy),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WarningCard(
    verdictLabel: String,
    verdictColor: Color,
    container: Color,
    onContainer: Color,
    reasons: List<String>,
    quoted: String,
    tips: List<String>,
    appName: String,
    guarantee: String
) {
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = container),
        modifier = Modifier.fillMaxWidth().width(360.dp)
    ) {
        Column(Modifier.padding(Space4), verticalArrangement = Arrangement.spacedBy(Space3)) {
            Text(
                text = appName,
                style = MaterialTheme.typography.labelLarge,
                color = onContainer
            )
            Text(
                text = verdictLabel,
                style = MaterialTheme.typography.displaySmall,
                color = verdictColor,
                fontWeight = FontWeight.ExtraBold
            )
            if (quoted.isNotBlank()) {
                Card(
                    shape = MaterialTheme.shapes.small,
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = quoted,
                        modifier = Modifier.padding(Space3),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF1B1730)
                    )
                }
            }
            if (reasons.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(Space2)) {
                    Text(
                        text = "•  " + reasons.joinToString("\n•  "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF1B1730)
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(Space2)) {
                tips.forEach { tip ->
                    Text(
                        text = "→  " + tip,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF1B1730)
                    )
                }
            }
            Text(
                text = guarantee,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF1B1730)
            )
        }
    }
}

private suspend fun shareCard(
    context: Context,
    layer: androidx.compose.ui.graphics.layer.GraphicsLayer,
    density: Float,
    quoted: String,
    hindi: Boolean
): Boolean = try {
    val image = layer.toImageBitmap()
    val bitmap = image.asAndroidBitmap()
    val dir = File(context.cacheDir, "captures").apply { mkdirs() }
    val file = File(dir, "saathi-warning-card.png")
    FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
    val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, quoted)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(
        Intent.createChooser(intent, context.getString(R.string.share_card_share))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
    true
} catch (e: Exception) {
    false
}

private fun verdictRes(verdict: com.bharatsuraksha.saathi.domain.model.Verdict): Int = when (verdict) {
    com.bharatsuraksha.saathi.domain.model.Verdict.SAFE -> R.string.verdict_safe
    com.bharatsuraksha.saathi.domain.model.Verdict.CAREFUL -> R.string.verdict_careful
    else -> R.string.verdict_dangerous
}
