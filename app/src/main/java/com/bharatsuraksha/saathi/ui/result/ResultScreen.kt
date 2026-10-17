package com.bharatsuraksha.saathi.ui.result

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
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
import com.bharatsuraksha.saathi.ui.theme.JyotiHero
import com.bharatsuraksha.saathi.ui.theme.JyotiState
import com.bharatsuraksha.saathi.ui.theme.OdometerNumber
import com.bharatsuraksha.saathi.ui.theme.dangerEdgePulse
import com.bharatsuraksha.saathi.ui.theme.inkSpread
import com.bharatsuraksha.saathi.ui.theme.motionAllowed
import com.bharatsuraksha.saathi.ui.theme.rememberLowRam
import com.bharatsuraksha.saathi.ui.theme.rememberReduceMotion
import com.bharatsuraksha.saathi.ui.theme.reveal
import com.bharatsuraksha.saathi.ui.theme.verdictAccent
import com.bharatsuraksha.saathi.ui.theme.verdictContainer
import com.bharatsuraksha.saathi.ui.theme.verdictOnContainer

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
    val reduce = rememberReduceMotion()
    val motion = motionAllowed()
    val lowRam = rememberLowRam()
    val haptics = LocalHapticFeedback.current
    val familyContacts by viewModel.familyContacts.collectAsStateWithLifecycle()
    val onlineMode by viewModel.onlineMode.collectAsStateWithLifecycle()
    val explanation by viewModel.explanation.collectAsStateWithLifecycle()
    val loadingExplanation by viewModel.loading.collectAsStateWithLifecycle()
    var showOnlineConsent by remember { mutableStateOf(false) }

    // One haptic tick when the verdict lands; stronger for Dangerous.
    LaunchedEffect(result?.verdict) {
        if (reduce) return@LaunchedEffect
        when (result?.verdict) {
            Verdict.DANGEROUS -> haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            Verdict.CAREFUL -> haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            else -> Unit
        }
    }

    Scaffold(containerColor = Color.Transparent) { padding ->
        if (result == null) {
            Column(
                Modifier.fillMaxSize().padding(padding).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(stringResource(R.string.result_no_reasons), style = MaterialTheme.typography.bodyLarge)
                OutlinedButton(onClick = onCheckAnother, shape = RoundedCornerShape(50)) {
                    Text(stringResource(R.string.result_again_button))
                }
            }
            return@Scaffold
        }

        val accent by animateColorAsState(
            targetValue = verdictAccent(result.verdict),
            animationSpec = tween(700, easing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)),
            label = "verdictMorph"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .inkSpread(motion)
                .then(if (result.verdict == Verdict.DANGEROUS) Modifier.dangerEdgePulse(motion) else Modifier)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    stringResource(R.string.result_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // ---- verdict block: morphed shield + big word + sweeping gauge ----
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = verdictContainer(result.verdict)),
                modifier = Modifier.fillMaxWidth().reveal(0, reduce)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(Modifier.size(104.dp)) {
                        JyotiHero(
                            state = jyotiStateFor(result.verdict),
                            motion = motion,
                            lowRam = lowRam
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(verdictLabelRes(result.verdict)),
                            style = MaterialTheme.typography.displayMedium,
                            color = verdictOnContainer(result.verdict)
                        )
                        Text(
                            stringResource(R.string.verdict_guarantee_note),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    ScoreGauge(score = result.score, tint = accent, motion = motion)
                }
            }

            Text(
                stringResource(
                    R.string.result_confidence_label,
                    stringResource(confidenceRes(result.confidence))
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.reveal(1, reduce)
            )

            result.banners.forEachIndexed { i, banner ->
                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth().reveal(2 + i, reduce)
                ) {
                    Text(
                        banner.text(hindi),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Text(
                stringResource(R.string.result_reasons_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.reveal(3, reduce)
            )
            if (result.reasons.isEmpty()) {
                Text(
                    stringResource(R.string.result_no_reasons),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.reveal(4, reduce)
                )
            } else {
                result.reasons.forEachIndexed { i, reason ->
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth().reveal(4 + i, reduce)
                    ) {
                        Row {
                            Box(Modifier.size(width = 5.dp, height = 64.dp).background(accent))
                            Text(
                                reason.text(hindi),
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            Text(
                stringResource(R.string.result_what_to_do_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.reveal(8, reduce)
            )
            viewModel.adviceKeys.forEachIndexed { index, key ->
                Card(
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth().reveal(9 + index, reduce)
                ) {
                    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "${index + 1}",
                            style = MaterialTheme.typography.titleLarge,
                            color = accent
                        )
                        Text(adviceText(key), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            if (result.verdict == Verdict.DANGEROUS && familyContacts.isNotEmpty()) {
                Text(
                    stringResource(R.string.result_family_alert),
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { shareFamilySms(context, familyContacts) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(50)
                    ) { Text(stringResource(R.string.family_share_sms)) }
                    OutlinedButton(
                        onClick = { shareFamilyWhatsapp(context, familyContacts) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(50)
                    ) { Text(stringResource(R.string.family_share_whatsapp)) }
                }
            }

            if (onlineMode) {
                if (loadingExplanation) {
                    Text(stringResource(R.string.result_online_getting))
                } else if (explanation != null) {
                    Card(shape = MaterialTheme.shapes.medium) {
                        Text(explanation!!, modifier = Modifier.padding(16.dp))
                    }
                } else {
                    OutlinedButton(
                        onClick = { showOnlineConsent = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50)
                    ) { Text(stringResource(R.string.result_online_explain)) }
                }
            }

            OutlinedButton(
                onClick = onOpenRecover,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50)
            ) { Text(stringResource(R.string.result_recover_button)) }

            Text(
                stringResource(R.string.result_checked_message_title),
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Card(shape = MaterialTheme.shapes.medium) {
                Text(viewModel.message, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
            }

            Text(
                stringResource(R.string.result_disclaimer),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onCheckAnother,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
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

/** Circular gauge that sweeps to the score with a slight overshoot. */
@Composable
private fun ScoreGauge(score: Int, tint: Color, motion: Boolean) {
    val progress by animateFloatAsState(
        targetValue = score / 100f,
        animationSpec = if (motion) spring(dampingRatio = 0.45f, stiffness = 130f) else tween(0),
        label = "gauge"
    )
    val track = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    Box(Modifier.size(74.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 7.dp.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(
                color = track,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = tint,
                startAngle = 135f,
                sweepAngle = 270f * progress.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        OdometerNumber(
            value = score,
            motion = motion,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun jyotiStateFor(verdict: Verdict): JyotiState = when (verdict) {
    Verdict.SAFE -> JyotiState.Clear
    Verdict.CAREFUL -> JyotiState.Careful
    Verdict.DANGEROUS -> JyotiState.Dangerous
}

private fun verdictLabelRes(verdict: Verdict): Int = when (verdict) {
    Verdict.SAFE -> R.string.verdict_safe
    Verdict.CAREFUL -> R.string.verdict_careful
    Verdict.DANGEROUS -> R.string.verdict_dangerous
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

private fun familyMessage(context: Context): String = context.getString(R.string.family_message)

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
