package com.bharatsuraksha.saathi.ui.upi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bharatsuraksha.saathi.R
import com.bharatsuraksha.saathi.domain.model.Verdict
import com.bharatsuraksha.saathi.domain.upi.UpiAnalyzer
import com.bharatsuraksha.saathi.domain.upi.UpiKind
import com.bharatsuraksha.saathi.domain.upi.UpiParser
import com.bharatsuraksha.saathi.domain.upi.UpiPayload
import com.bharatsuraksha.saathi.ui.common.isHindiLocale
import com.bharatsuraksha.saathi.ui.session.UpiScanStore
import com.bharatsuraksha.saathi.ui.theme.JyotiHero
import com.bharatsuraksha.saathi.ui.theme.JyotiState
import com.bharatsuraksha.saathi.ui.theme.Sindoor
import com.bharatsuraksha.saathi.ui.theme.Space2
import com.bharatsuraksha.saathi.ui.theme.Space3
import com.bharatsuraksha.saathi.ui.theme.Space4
import com.bharatsuraksha.saathi.ui.theme.motionAllowed
import com.bharatsuraksha.saathi.ui.theme.rememberLowRam
import com.bharatsuraksha.saathi.ui.theme.verdictAccent
import com.bharatsuraksha.saathi.ui.theme.verdictContainer
import com.bharatsuraksha.saathi.ui.theme.verdictOnContainer
import kotlinx.coroutines.delay

/** The UPI result, in the same verdict design as everything else. */
@Composable
fun UpiResultScreen(onBack: () -> Unit, onScanAnother: () -> Unit) {
    val hindi = isHindiLocale()
    val motion = motionAllowed()
    val lowRam = rememberLowRam()

    val payload: UpiPayload = remember { UpiParser.parse(UpiScanStore.raw) }
    val analysis = remember { UpiAnalyzer.analyze(payload) }
    val accent = verdictAccent(analysis.verdict)

    // The "do not pay yet" pause, only for a dangerous QR.
    var pause by remember { mutableIntStateOf(if (analysis.verdict == Verdict.DANGEROUS) 15 else 0) }
    LaunchedEffect(analysis.verdict) {
        while (pause > 0) {
            delay(1000)
            pause -= 1
        }
    }

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
                    text = stringResource(R.string.upi_result_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = verdictContainer(analysis.verdict)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(Space4),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Space3)
                ) {
                    Box(Modifier.size(96.dp)) {
                        JyotiHero(
                            state = when (analysis.verdict) {
                                Verdict.SAFE -> JyotiState.Clear
                                Verdict.CAREFUL -> JyotiState.Careful
                                else -> JyotiState.Dangerous
                            },
                            motion = motion,
                            lowRam = lowRam
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(
                                when (analysis.verdict) {
                                    Verdict.SAFE -> R.string.verdict_safe
                                    Verdict.CAREFUL -> R.string.verdict_careful
                                    else -> R.string.verdict_dangerous
                                }
                            ),
                            style = MaterialTheme.typography.displaySmall,
                            color = verdictOnContainer(analysis.verdict)
                        )
                        Text(
                            text = stringResource(R.string.verdict_guarantee_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ---- send or receive, in plain words ----
            Card(shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(Space4), verticalArrangement = Arrangement.spacedBy(Space2)) {
                    Text(
                        text = directionText(payload, hindi),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.upi_never_receive),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (payload.kind == UpiKind.PLAIN_ID) {
                        Text(
                            text = stringResource(R.string.upi_plain_id_note),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ---- the 15 second pause ----
            if (pause > 0) {
                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = Sindoor.copy(alpha = 0.16f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(Space4), verticalArrangement = Arrangement.spacedBy(Space2)) {
                        Text(
                            text = stringResource(R.string.upi_do_not_pay),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Sindoor
                        )
                        Text(
                            text = stringResource(R.string.upi_pause_seconds, pause),
                            style = MaterialTheme.typography.displaySmall,
                            color = Sindoor
                        )
                        Text(
                            text = stringResource(R.string.upi_pause_body),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // ---- what the QR actually contains ----
            Card(shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(Space4), verticalArrangement = Arrangement.spacedBy(Space2)) {
                    Text(
                        text = stringResource(R.string.upi_details),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Detail(stringResource(R.string.upi_detail_kind), kindText(payload.kind))
                    payload.payeeAddress?.let { Detail(stringResource(R.string.upi_detail_payee), it) }
                    payload.payeeName?.let { Detail(stringResource(R.string.upi_detail_name), it) }
                    payload.amount?.let { Detail(stringResource(R.string.upi_detail_amount), amountText(it, payload.currency)) }
                    payload.note?.let { Detail(stringResource(R.string.upi_detail_note), it) }
                    payload.txnRef?.let { Detail(stringResource(R.string.upi_detail_ref), it) }
                    payload.merchantCode?.let { Detail(stringResource(R.string.upi_detail_mcc), it) }
                }
            }

            Text(
                text = stringResource(R.string.upi_reasons_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (analysis.findings.isEmpty()) {
                Text(
                    text = stringResource(R.string.upi_no_reasons),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                analysis.findings.forEach { finding ->
                    Card(shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
                        Row {
                            Box(Modifier.size(width = 5.dp, height = 56.dp).background(accent))
                            Text(
                                text = stringResource(reasonRes(finding.reasonKey)),
                                modifier = Modifier.padding(Space3),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            Text(
                text = stringResource(R.string.upi_never_verified),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.upi_disclaimer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onScanAnother,
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth().height(60.dp)
            ) { Text(stringResource(R.string.upi_scan_another), style = MaterialTheme.typography.titleMedium) }
            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
            ) { Text(stringResource(R.string.action_back)) }
            Spacer(Modifier.height(Space2))
        }
    }
}

@Composable
private fun Detail(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.42f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.58f)
        )
    }
}

private fun kindText(kind: UpiKind): String = when (kind) {
    UpiKind.PAY -> "upi://pay"
    UpiKind.MANDATE -> "upi://mandate"
    UpiKind.PLAIN_ID -> "UPI ID"
    UpiKind.UNKNOWN -> "unknown"
}

private fun amountText(amount: String, currency: String?): String =
    (currency ?: "INR") + " " + amount

@Composable
private fun directionText(payload: UpiPayload, hindi: Boolean): String {
    val amount = payload.amount
    return when {
        payload.isCollectRequest ->
            if (hindi) "यह एक कलेक्ट रिक्वेस्ट है। मंज़ूरी देने पर आपके खाते से पैसा कटेगा।"
            else "This is a collect request. Approving it takes money out of your account."

        payload.kind == UpiKind.PAY && amount != null ->
            if (hindi) "यह QR आपके खाते से ₹$amount भेजेगा।"
            else "This QR will send ₹$amount from your account."

        payload.kind == UpiKind.PAY ->
            if (hindi) "यह QR आपके खाते से पैसे भेजेगा। रकम बाद में डाली जाएगी।"
            else "This QR will send money from your account. The amount is filled in later."

        payload.kind == UpiKind.PLAIN_ID ->
            if (hindi) "यह एक UPI आईडी है, कोई रकम तय नहीं।"
            else "This is a plain UPI ID with no amount attached."

        else ->
            if (hindi) "यह UPI पेलोड नहीं पहचाना गया।"
            else "This is not a recognised UPI payload."
    }
}

private fun reasonRes(key: String): Int = when (key) {
    UpiAnalyzer.REASON_COLLECT -> R.string.upi_reason_collect
    UpiAnalyzer.REASON_AMOUNT -> R.string.upi_reason_amount
    UpiAnalyzer.REASON_LARGE -> R.string.upi_reason_large
    UpiAnalyzer.REASON_AUTHORITY -> R.string.upi_reason_authority
    UpiAnalyzer.REASON_HANDLE -> R.string.upi_reason_handle
    UpiAnalyzer.REASON_MISSING_PAYEE -> R.string.upi_reason_missing
    UpiAnalyzer.REASON_PLAIN_ID -> R.string.upi_reason_plain
    else -> R.string.upi_no_reasons
}
