package com.bharatsuraksha.saathi.ui.i18n

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.bharatsuraksha.saathi.R
import com.bharatsuraksha.saathi.ui.theme.Haldi
import com.bharatsuraksha.saathi.ui.theme.Neem
import com.bharatsuraksha.saathi.ui.theme.Saffron
import com.bharatsuraksha.saathi.ui.theme.Space2
import com.bharatsuraksha.saathi.ui.theme.Space3
import com.bharatsuraksha.saathi.ui.theme.Space4
import com.bharatsuraksha.saathi.ui.theme.tappable

/** The tag the app is currently showing. */
fun currentLanguageTag(): String =
    AppCompatDelegate.getApplicationLocales().toLanguageTags()
        .substringBefore(',')
        .substringBefore('-')
        .ifBlank { "hi" }

/**
 * Language picker. Each language is shown in its own script, grouped into what is
 * fully translated and what is community/beta, and every beta row says so.
 */
@Composable
fun LanguageScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var current by remember { mutableStateOf(currentLanguageTag()) }

    val results = remember(query) { LanguageRegistry.search(query) }
    val translated = results.filter { it.hasStrings }
    val pending = results.filterNot { it.hasStrings }

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
                    text = stringResource(R.string.language_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = stringResource(R.string.language_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.language_detection_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text(stringResource(R.string.language_search)) },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            )

            if (translated.isNotEmpty()) {
                SectionTitle(stringResource(R.string.language_group_translated))
                translated.forEach { language ->
                    LanguageRow(
                        language = language,
                        selected = language.tag == current,
                        onPick = {
                            current = language.tag
                            AppCompatDelegate.setApplicationLocales(
                                LocaleListCompat.forLanguageTags(language.tag)
                            )
                        },
                        onReport = { reportTranslation(context, language) }
                    )
                }
            }

            if (pending.isNotEmpty()) {
                SectionTitle(stringResource(R.string.language_group_beta))
                Text(
                    text = stringResource(R.string.language_pending_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                pending.forEach { language ->
                    LanguageRow(
                        language = language,
                        selected = false,
                        onPick = null,
                        onReport = { reportTranslation(context, language) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold,
        color = Saffron,
        modifier = Modifier.padding(top = Space2)
    )
}

@Composable
private fun LanguageRow(
    language: AppLanguage,
    selected: Boolean,
    onPick: (() -> Unit)?,
    onReport: () -> Unit
) {
    val selectable = onPick != null
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Saffron.copy(alpha = 0.16f)
            else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .then(
                if (selectable) Modifier.tappable(motion = true, onClick = onPick!!)
                else Modifier
            )
    ) {
        Column(Modifier.padding(Space4), verticalArrangement = Arrangement.spacedBy(Space2)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = language.nativeName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (language.status == LanguageStatus.BETA) {
                    Text(
                        text = stringResource(R.string.language_beta_badge),
                        style = MaterialTheme.typography.labelSmall,
                        color = Haldi,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Text(
                text = language.englishName + "  ·  " + language.tag + "  ·  " + language.script,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(
                    when (language.detection) {
                        Detection.STRONG -> R.string.language_detect_strong
                        Detection.BASIC -> R.string.language_detect_basic
                        Detection.UI_ONLY -> R.string.language_detect_ui_only
                    }
                ),
                style = MaterialTheme.typography.labelSmall,
                color = when (language.detection) {
                    Detection.STRONG -> Neem
                    Detection.BASIC -> Saffron
                    Detection.UI_ONLY -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            TextButton(onClick = onReport) {
                Text(stringResource(R.string.language_report))
            }
        }
    }
}

/** Opens a pre-filled report so a volunteer can send a correction. */
private fun reportTranslation(context: Context, language: AppLanguage) {
    val body = context.getString(R.string.language_report_body, language.englishName, language.tag)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.language_report_subject, language.englishName))
        putExtra(Intent.EXTRA_TEXT, body)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching {
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.language_report)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
