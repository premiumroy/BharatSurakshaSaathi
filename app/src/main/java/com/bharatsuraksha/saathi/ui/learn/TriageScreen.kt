package com.bharatsuraksha.saathi.ui.learn

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.bharatsuraksha.saathi.R
import com.bharatsuraksha.saathi.domain.learn.TriageScoring
import com.bharatsuraksha.saathi.domain.model.Verdict
import com.bharatsuraksha.saathi.ui.theme.MotionTokens
import com.bharatsuraksha.saathi.ui.theme.Saffron
import com.bharatsuraksha.saathi.ui.theme.Space2
import com.bharatsuraksha.saathi.ui.theme.Space3
import com.bharatsuraksha.saathi.ui.theme.Space4
import com.bharatsuraksha.saathi.ui.theme.Space6
import com.bharatsuraksha.saathi.ui.theme.motionAllowed
import com.bharatsuraksha.saathi.ui.theme.verdictAccent
import com.bharatsuraksha.saathi.ui.theme.verdictContainer
import com.bharatsuraksha.saathi.ui.theme.verdictOnContainer

private val QUESTION_RES = listOf(
    R.string.triage_q_authority,
    R.string.triage_q_video,
    R.string.triage_q_money,
    R.string.triage_q_secrecy,
    R.string.triage_q_panic
)

/** "कॉल पर क्या हुआ?" — five yes/no questions, then a verdict and the actions. */
@Composable
fun TriageScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val motion = motionAllowed()

    var step by remember { mutableIntStateOf(0) }
    var answers by remember { mutableStateOf(TriageScoring.Answers()) }
    var outcome by remember { mutableStateOf<TriageScoring.Outcome?>(null) }

    val progress by animateFloatAsState(
        targetValue = if (outcome != null) 1f else step / QUESTION_RES.size.toFloat(),
        animationSpec = androidx.compose.animation.core.tween(
            if (motion) MotionTokens.Medium else 0,
            easing = MotionTokens.Entrance
        ),
        label = "triageProgress"
    )

    fun record(value: Boolean) {
        answers = when (step) {
            0 -> answers.copy(claimedAuthority = value)
            1 -> answers.copy(videoCall = value)
            2 -> answers.copy(askedMoneyOrOtp = value)
            3 -> answers.copy(askedSecrecy = value)
            else -> answers.copy(createdPanic = value)
        }
        if (step + 1 >= QUESTION_RES.size) {
            val finalAnswers = when (step) {
                0 -> answers.copy(claimedAuthority = value)
                1 -> answers.copy(videoCall = value)
                2 -> answers.copy(askedMoneyOrOtp = value)
                3 -> answers.copy(askedSecrecy = value)
                else -> answers.copy(createdPanic = value)
            }
            outcome = TriageScoring.score(finalAnswers)
        } else {
            step += 1
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
                    text = stringResource(R.string.triage_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // animated progress
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .height(6.dp)
                        .background(Saffron, RoundedCornerShape(50))
                )
            }

            val result = outcome
            if (result == null) {
                Text(
                    text = stringResource(R.string.triage_step, step + 1, QUESTION_RES.size),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(QUESTION_RES[step]),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(Space2))
                Row(horizontalArrangement = Arrangement.spacedBy(Space3), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { record(true) },
                        modifier = Modifier.weight(1f).height(64.dp),
                        shape = RoundedCornerShape(50)
                    ) { Text(stringResource(R.string.triage_yes), style = MaterialTheme.typography.titleLarge) }
                    OutlinedButton(
                        onClick = { record(false) },
                        modifier = Modifier.weight(1f).height(64.dp),
                        shape = RoundedCornerShape(50)
                    ) { Text(stringResource(R.string.triage_no), style = MaterialTheme.typography.titleLarge) }
                }
                Text(
                    text = stringResource(R.string.triage_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                TriageResult(outcome = result, context = context)
                OutlinedButton(
                    onClick = {
                        step = 0
                        answers = TriageScoring.Answers()
                        outcome = null
                    },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                ) { Text(stringResource(R.string.triage_again)) }
            }

            // The fixed banner always shows on this screen.
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.digital_arrest_banner),
                    modifier = Modifier.padding(Space4),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Text(
                text = stringResource(R.string.verdict_guarantee_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TriageResult(outcome: TriageScoring.Outcome, context: android.content.Context) {
    val accent = verdictAccent(outcome.verdict)
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = verdictContainer(outcome.verdict)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(Space4), verticalArrangement = Arrangement.spacedBy(Space2)) {
            Text(
                text = stringResource(
                    when (outcome.verdict) {
                        Verdict.SAFE -> R.string.verdict_safe
                        Verdict.CAREFUL -> R.string.verdict_careful
                        else -> R.string.verdict_dangerous
                    }
                ),
                style = MaterialTheme.typography.displaySmall,
                color = verdictOnContainer(outcome.verdict)
            )
            outcome.reasonKeys.forEach { key ->
                Text(text = stringResource(reasonRes(key)), style = MaterialTheme.typography.bodyLarge)
            }
        }
    }

    if (outcome.verdict != Verdict.SAFE) {
        Button(
            onClick = {
                context.startActivity(
                    Intent(Intent.ACTION_DIAL, Uri.parse("tel:1930"))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.White)
        ) { Text(stringResource(R.string.triage_call_1930), style = MaterialTheme.typography.titleLarge) }
        OutlinedButton(
            onClick = {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://cybercrime.gov.in"))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            },
            shape = RoundedCornerShape(50),
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
        ) { Text(stringResource(R.string.triage_report_online)) }
    }
}

private fun reasonRes(key: String): Int = when (key) {
    TriageScoring.REASON_MONEY -> R.string.triage_reason_money
    TriageScoring.REASON_AUTHORITY -> R.string.triage_reason_authority
    TriageScoring.REASON_SECRECY -> R.string.triage_reason_secrecy
    TriageScoring.REASON_PANIC -> R.string.triage_reason_panic
    TriageScoring.REASON_VIDEO -> R.string.triage_reason_video
    else -> R.string.triage_note
}
