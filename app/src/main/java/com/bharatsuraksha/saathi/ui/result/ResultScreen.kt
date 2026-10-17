package com.bharatsuraksha.saathi.ui.result

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bharatsuraksha.saathi.R
import com.bharatsuraksha.saathi.domain.model.AnalysisResult
import com.bharatsuraksha.saathi.domain.model.Confidence
import com.bharatsuraksha.saathi.domain.model.Verdict
import com.bharatsuraksha.saathi.ui.common.isHindiLocale
import com.bharatsuraksha.saathi.ui.theme.BrandGreen
import com.bharatsuraksha.saathi.ui.theme.CarefulAmber
import com.bharatsuraksha.saathi.ui.theme.CarefulAmberBg
import com.bharatsuraksha.saathi.ui.theme.DangerRed
import com.bharatsuraksha.saathi.ui.theme.DangerRedBg
import com.bharatsuraksha.saathi.ui.theme.SafeGreen
import com.bharatsuraksha.saathi.ui.theme.SafeGreenBg

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    onBack: () -> Unit,
    onCheckAnother: () -> Unit,
    onOpenRecover: () -> Unit,
    viewModel: ResultViewModel = hiltViewModel()
) {
    val result = viewModel.result
    val hindi = isHindiLocale()
    val context = LocalContext.current
    val familyContacts by viewModel.familyContacts.collectAsStateWithLifecycle()
    val onlineMode by viewModel.onlineMode.collectAsStateWithLifecycle()
    val explanation by viewModel.explanation.collectAsStateWithLifecycle()
    val loadingExplanation by viewModel.loading.collectAsStateWithLifecycle()
    var showOnlineConsent by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.result_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandGreen,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        if (result == null) {
            Column(
                Modifier.fillMaxSize().padding(padding).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(stringResource(R.string.result_no_reasons), style = MaterialTheme.typography.bodyLarge)
                OutlinedButton(onClick = onCheckAnother) {
                    Text(stringResource(R.string.result_again_button))
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            VerdictBanner(result)

            Text(
                text = stringResource(R.string.verdict_guarantee_note),
                style = MaterialTheme.typography.bodyMedium
            )

            // Fixed family warnings (e.g. the digital-arrest banner).
            result.banners.forEach { banner ->
                Card(colors = CardDefaults.cardColors(containerColor = DangerRedBg)) {
                    Text(
                        text = banner.text(hindi),
                        modifier = Modifier.padding(16.dp),
                        color = DangerRed,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = stringResource(R.string.result_score_label, result.score),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(
                    R.string.result_confidence_label,
                    stringResource(confidenceRes(result.confidence))
                ),
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = stringResource(R.string.result_reasons_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            if (result.reasons.isEmpty()) {
                Text(stringResource(R.string.result_no_reasons), style = MaterialTheme.typography.bodyLarge)
            } else {
                result.reasons.forEach { reason ->
                    Card {
                        Text(
                            text = "• " + reason.text(hindi),
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            Text(
                text = stringResource(R.string.result_what_to_do_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            viewModel.adviceKeys.forEachIndexed { index, key ->
                Card {
                    Text(
                        text = "${index + 1}. " + adviceText(key),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // ---- Family alert (user-initiated, only for Dangerous) ----
            if (result.verdict == Verdict.DANGEROUS && familyContacts.isNotEmpty()) {
                Text(stringResource(R.string.result_family_alert), fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { shareFamilySms(context, familyContacts) },
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.family_share_sms)) }
                    OutlinedButton(
                        onClick = { shareFamilyWhatsapp(context, familyContacts) },
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.family_share_whatsapp)) }
                }
            }

            // ---- Optional online explanation ----
            if (onlineMode) {
                if (loadingExplanation) {
                    Text(stringResource(R.string.result_online_getting))
                } else if (explanation != null) {
                    Card {
                        Text(explanation!!, modifier = Modifier.padding(16.dp))
                    }
                } else {
                    OutlinedButton(
                        onClick = { showOnlineConsent = true },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.result_online_explain)) }
                }
            }

            // ---- Recovery ----
            OutlinedButton(onClick = onOpenRecover, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.result_recover_button))
            }

            Text(stringResource(R.string.result_checked_message_title), fontWeight = FontWeight.Bold)
            Card {
                Text(viewModel.message, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
            }

            Text(stringResource(R.string.result_disclaimer), style = MaterialTheme.typography.bodyMedium)

            Button(
                onClick = onCheckAnother,
                modifier = Modifier.fillMaxWidth().height(64.dp)
            ) {
                Text(stringResource(R.string.result_again_button), style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    if (showOnlineConsent) {
        AlertDialog(
            onDismissRequest = { showOnlineConsent = false },
            title = { Text(stringResource(R.string.result_online_consent_title)) },
            text = { Text(stringResource(R.string.result_online_consent_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showOnlineConsent = false
                    viewModel.fetchExplanation()
                }) { Text(stringResource(R.string.result_online_send)) }
            },
            dismissButton = {
                TextButton(onClick = { showOnlineConsent = false }) {
                    Text(stringResource(R.string.result_online_cancel))
                }
            }
        )
    }
}

@Composable
private fun VerdictBanner(result: AnalysisResult) {
    val (bg, fg) = when (result.verdict) {
        Verdict.SAFE -> SafeGreenBg to SafeGreen
        Verdict.CAREFUL -> CarefulAmberBg to CarefulAmber
        Verdict.DANGEROUS -> DangerRedBg to DangerRed
    }
    val label = when (result.verdict) {
        Verdict.SAFE -> R.string.verdict_safe
        Verdict.CAREFUL -> R.string.verdict_careful
        Verdict.DANGEROUS -> R.string.verdict_dangerous
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(20.dp))
            .padding(24.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(label),
            color = fg,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun confidenceRes(confidence: Confidence): Int = when (confidence) {
    Confidence.LOW -> R.string.confidence_low
    Confidence.MEDIUM -> R.string.confidence_medium
    Confidence.HIGH -> R.string.confidence_high
}

@Composable
private fun adviceText(key: String): String = stringResource(
    when (key) {
        "advice_dont_share_otp" -> R.string.advice_dont_share_otp
        "advice_dont_click" -> R.string.advice_dont_click
        "advice_dont_pay" -> R.string.advice_dont_pay
        "advice_block_number" -> R.string.advice_block_number
        "advice_no_remote_app" -> R.string.advice_no_remote_app
        "advice_verify_official" -> R.string.advice_verify_official
        "advice_police_no_arrest" -> R.string.advice_police_no_arrest
        "advice_report_1930" -> R.string.advice_report_1930
        "advice_report_cybercrime" -> R.string.advice_report_cybercrime
        "advice_keep_message" -> R.string.advice_keep_message
        "advice_no_guaranteed_returns" -> R.string.advice_no_guaranteed_returns
        "advice_job_never_pay" -> R.string.advice_job_never_pay
        "advice_call_back_known_number" -> R.string.advice_call_back_known_number
        "advice_loan_app_report" -> R.string.advice_loan_app_report
        "advice_lottery_never_pay" -> R.string.advice_lottery_never_pay
        "advice_sextortion_dont_pay" -> R.string.advice_sextortion_dont_pay
        "advice_courier_never_pay" -> R.string.advice_courier_never_pay
        else -> R.string.advice_verify_official
    }
)

private fun familyMessage(context: Context): String =
    context.getString(R.string.family_message)

private fun shareFamilySms(context: Context, contacts: Set<String>) {
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + contacts.joinToString(","))).apply {
        putExtra("sms_body", familyMessage(context))
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

private fun shareFamilyWhatsapp(context: Context, contacts: Set<String>) {
    val number = contacts.firstOrNull()?.filter(Char::isDigit).orEmpty()
    val url = "https://wa.me/$number?text=" + Uri.encode(familyMessage(context))
    context.startActivity(
        Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}
