package com.bharatsuraksha.saathi.ui.safety

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.bharatsuraksha.saathi.R
import com.bharatsuraksha.saathi.data.db.EvidenceEntity
import com.bharatsuraksha.saathi.data.db.HistoryEntity
import com.bharatsuraksha.saathi.data.repository.EvidenceRepository
import com.bharatsuraksha.saathi.data.repository.HistoryRepository
import com.bharatsuraksha.saathi.domain.learn.StreakLogic
import com.bharatsuraksha.saathi.ui.theme.Neem
import com.bharatsuraksha.saathi.ui.theme.Saffron
import com.bharatsuraksha.saathi.ui.theme.Sindoor
import com.bharatsuraksha.saathi.ui.theme.Space2
import com.bharatsuraksha.saathi.ui.theme.Space3
import com.bharatsuraksha.saathi.ui.theme.Space4
import com.bharatsuraksha.saathi.ui.theme.Space6
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val history: HistoryRepository,
    private val evidence: EvidenceRepository
) : ViewModel() {

    val items: StateFlow<List<HistoryEntity>> =
        history.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val evidenceItems: StateFlow<List<EvidenceEntity>> =
        evidence.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun clearAll() = viewModelScope.launch {
        history.deleteAll()
        evidence.deleteAll()
    }
}

/** Safety dashboard, all from local Room data. */
@Composable
fun DashboardScreen(onBack: () -> Unit, viewModel: DashboardViewModel = hiltViewModel()) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val evidence by viewModel.evidenceItems.collectAsStateWithLifecycle()
    var confirmClear by remember { mutableStateOf(false) }

    val checks = items.size
    val dangers = items.count { it.verdict == "DANGEROUS" }
    val lastDanger = items.filter { it.verdict == "DANGEROUS" }.maxByOrNull { it.checkedAt }
    val daysSinceRisky = lastDanger?.let { daysSince(it.checkedAt) }
    val streak = checkStreak(items)

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
                    text = stringResource(R.string.dashboard_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(Space3), modifier = Modifier.fillMaxWidth()) {
                StatCard(stringResource(R.string.dashboard_checks), checks.toString(), Saffron, Modifier.weight(1f))
                StatCard(stringResource(R.string.dashboard_dangers), dangers.toString(), Sindoor, Modifier.weight(1f))
            }

            Card(shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(Space4), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.dashboard_streak),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(R.string.dashboard_streak_days, streak),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = daysSinceRisky?.let {
                                stringResource(R.string.dashboard_since_risky, it)
                            } ?: stringResource(R.string.dashboard_no_risky),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    StreakRing(days = streak, modifier = Modifier.size(84.dp))
                }
            }

            Text(
                text = stringResource(R.string.dashboard_week),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Card(shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
                WeekChart(
                    counts = weekCounts(items),
                    modifier = Modifier.fillMaxWidth().height(140.dp).padding(Space4)
                )
            }

            Text(
                text = stringResource(R.string.dashboard_evidence_count, evidence.size),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!confirmClear) {
                OutlinedButton(
                    onClick = { confirmClear = true },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) { Text(stringResource(R.string.dashboard_clear)) }
            } else {
                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(Modifier.padding(Space4), verticalArrangement = Arrangement.spacedBy(Space3)) {
                        Text(
                            text = stringResource(R.string.dashboard_clear_confirm),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(Space3)) {
                            OutlinedButton(
                                onClick = { confirmClear = false },
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.weight(1f)
                            ) { Text(stringResource(R.string.history_cancel)) }
                            OutlinedButton(
                                onClick = { viewModel.clearAll(); confirmClear = false },
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.weight(1f)
                            ) { Text(stringResource(R.string.dashboard_clear_yes)) }
                        }
                    }
                }
            }

            Text(
                text = stringResource(R.string.dashboard_privacy),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, tint: Color, modifier: Modifier = Modifier) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {
        Column(Modifier.padding(Space4)) {
            Text(text = value, style = MaterialTheme.typography.displaySmall, color = tint)
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** A ring that fills as the daily streak grows toward seven. */
@Composable
private fun StreakRing(days: Int, modifier: Modifier = Modifier) {
    val track = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 8.dp.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(
                color = track, startAngle = -90f, sweepAngle = 360f, useCenter = false,
                topLeft = Offset(inset, inset), size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = Neem, startAngle = -90f,
                sweepAngle = 360f * (days.coerceIn(0, 7) / 7f), useCenter = false,
                topLeft = Offset(inset, inset), size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Text(
            text = days.toString(),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/** Seven bars, oldest on the left, drawn on Canvas. */
@Composable
private fun WeekChart(counts: List<Int>, modifier: Modifier = Modifier) {
    val barColor = Saffron
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val maxCount = (counts.maxOrNull() ?: 0).coerceAtLeast(1)
    Canvas(modifier) {
        if (counts.isEmpty()) return@Canvas
        val slot = size.width / counts.size
        val barWidth = slot * 0.52f
        counts.forEachIndexed { index, value ->
            val x = slot * index + (slot - barWidth) / 2f
            drawRoundRect(
                color = trackColor,
                topLeft = Offset(x, 0f),
                size = Size(barWidth, size.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2f)
            )
            val h = size.height * (value.toFloat() / maxCount)
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, size.height - h),
                size = Size(barWidth, h),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2f)
            )
        }
    }
}

// ------------------------------------------------------------------ helpers

private fun dayKey(millis: Long): String {
    val c = Calendar.getInstance().apply { timeInMillis = millis }
    return "%04d-%02d-%02d".format(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
}

private fun daysSince(millis: Long): Int {
    val today = StreakLogic.dayNumber(dayKey(System.currentTimeMillis()))
    val then = StreakLogic.dayNumber(dayKey(millis))
    return (today - then).coerceAtLeast(0)
}

/** Consecutive days, ending today, that have at least one check. */
private fun checkStreak(items: List<HistoryEntity>): Int {
    if (items.isEmpty()) return 0
    val days = items.map { StreakLogic.dayNumber(dayKey(it.checkedAt)) }.toSet()
    val today = StreakLogic.dayNumber(dayKey(System.currentTimeMillis()))
    var streak = 0
    var cursor = today
    while (days.contains(cursor)) {
        streak += 1
        cursor -= 1
    }
    return streak
}

/** Counts for the last seven days, oldest first. */
private fun weekCounts(items: List<HistoryEntity>): List<Int> {
    val today = StreakLogic.dayNumber(dayKey(System.currentTimeMillis()))
    val buckets = items.map { StreakLogic.dayNumber(dayKey(it.checkedAt)) }
    return (6 downTo 0).map { offset -> buckets.count { it == today - offset } }
}
