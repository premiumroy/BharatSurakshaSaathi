package com.bharatsuraksha.saathi.ui.learn

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bharatsuraksha.saathi.R
import com.bharatsuraksha.saathi.domain.learn.GameCard
import com.bharatsuraksha.saathi.domain.learn.LearnData
import com.bharatsuraksha.saathi.domain.learn.StreakLogic
import com.bharatsuraksha.saathi.ui.common.isHindiLocale
import com.bharatsuraksha.saathi.ui.session.GameStatsStore
import com.bharatsuraksha.saathi.ui.theme.Haldi
import com.bharatsuraksha.saathi.ui.theme.JyotiFlame
import com.bharatsuraksha.saathi.ui.theme.MotionTokens
import com.bharatsuraksha.saathi.ui.theme.Neem
import com.bharatsuraksha.saathi.ui.theme.Saffron
import com.bharatsuraksha.saathi.ui.theme.Sindoor
import com.bharatsuraksha.saathi.ui.theme.Space2
import com.bharatsuraksha.saathi.ui.theme.Space3
import com.bharatsuraksha.saathi.ui.theme.Space4
import com.bharatsuraksha.saathi.ui.theme.Space6
import com.bharatsuraksha.saathi.ui.theme.entrance
import com.bharatsuraksha.saathi.ui.theme.motionAllowed
import com.bharatsuraksha.saathi.ui.theme.tappable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bharatsuraksha.saathi.data.prefs.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * "ठगी पहचानो / Spot the Scam". Swipe right for real, left for scam, or use the
 * two big buttons in Easy Mode. Ten cards make a round.
 */
@HiltViewModel
class LearnPrefsViewModel @Inject constructor(
    settings: SettingsRepository
) : ViewModel() {
    val easyMode: StateFlow<Boolean> =
        settings.easyMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
}

@Composable
fun SpotTheScamScreen(onBack: () -> Unit, prefs: LearnPrefsViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val easyMode by prefs.easyMode.collectAsState()
    val motion = motionAllowed()
    val hindi = isHindiLocale()
    val scope = rememberCoroutineScope()

    val all = remember { LearnData.loadCards(context) }
    val dayKey = remember { todayKey() }
    val round = remember { LearnData.dailyRound(all, dayKey) }

    var index by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    var feedback by remember { mutableStateOf<Pair<GameCard, Boolean>?>(null) }
    var newBadge by remember { mutableStateOf<String?>(null) }
    var done by remember { mutableStateOf(false) }

    val offsetX = remember { Animatable(0f) }
    val card = round.getOrNull(index)

    fun answer(card: GameCard, guessedReal: Boolean) {
        val correct = guessedReal != card.isScam
        val wasScam = card.isScam
        val before = GameStatsStore.stats.value
        val after = StreakLogic.answer(before, correct, wasScam, dayKey)
        GameStatsStore.save(context, after)
        streak = after.streak
        val gained = after.badges - before.badges
        newBadge = gained.firstOrNull()
        feedback = card to correct
        if (correct) newBadge = gained.firstOrNull()
    }

    fun advance() {
        feedback = null
        offsetX.snapTo(0f)
        if (index + 1 >= round.size) {
            GameStatsStore.save(context, StreakLogic.finishRound(GameStatsStore.stats.value))
            done = true
        } else {
            index += 1
        }
    }

    Scaffold(containerColor = Color.Transparent) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Space4),
            verticalArrangement = Arrangement.spacedBy(Space3)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onBack) { Text(stringResource(R.string.action_back)) }
                Text(
                    text = stringResource(R.string.spot_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stringResource(R.string.spot_streak) + " " + streak,
                    style = MaterialTheme.typography.titleMedium,
                    color = Saffron
                )
            }

            if (done) {
                RoundDone(
                    stats = GameStatsStore.stats.value,
                    onAgain = { index = 0; done = false; offsetX.snapTo(0f) },
                    onBack = onBack
                )
                return@Column
            }

            Text(
                text = stringResource(R.string.spot_progress, index + 1, round.size),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            card?.let { current ->
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Card(
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 220.dp)
                            .graphicsLayer {
                                translationX = offsetX.value
                                rotationZ = (offsetX.value / 60f).coerceIn(-12f, 12f)
                            }
                            .pointerInput(current.id) {
                                detectHorizontalDragGestures(
                                    onDragEnd = {
                                        val dx = offsetX.value
                                        if (dx > 140f) {
                                            scope.launch { offsetX.animateTo(900f, tween(MotionTokens.Quick)); answer(current, true) }
                                        } else if (dx < -140f) {
                                            scope.launch { offsetX.animateTo(-900f, tween(MotionTokens.Quick)); answer(current, false) }
                                        } else {
                                            scope.launch { offsetX.animateTo(0f, MotionTokens.Press) }
                                        }
                                    },
                                    onHorizontalDrag = { _, drag ->
                                        scope.launch { offsetX.snapTo(offsetX.value + drag) }
                                    }
                                )
                            }
                            .padding(Space4)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(Space3)) {
                            Text(
                                text = current.text,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (easyMode) {
                                Text(
                                    text = stringResource(R.string.spot_easy_hint),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (easyMode) {
                    Row(horizontalArrangement = Arrangement.spacedBy(Space3), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                scope.launch { offsetX.animateTo(-900f, tween(MotionTokens.Quick)) }
                                answer(current, false)
                            },
                            modifier = Modifier.weight(1f).height(96.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Sindoor, contentColor = Color.White)
                        ) { Text(stringResource(R.string.spot_scam), style = MaterialTheme.typography.headlineSmall) }
                        Button(
                            onClick = {
                                scope.launch { offsetX.animateTo(900f, tween(MotionTokens.Quick)) }
                                answer(current, true)
                            },
                            modifier = Modifier.weight(1f).height(96.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Neem, contentColor = Color.White)
                        ) { Text(stringResource(R.string.spot_real), style = MaterialTheme.typography.headlineSmall) }
                    }
                } else {
                    Text(
                        text = stringResource(R.string.spot_swipe_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            AnimatedVisibility(visible = feedback != null, enter = fadeIn(), exit = fadeOut()) {
                feedback?.let { (answered, correct) ->
                    FeedbackPanel(
                        card = answered,
                        correct = correct,
                        hindi = hindi,
                        newBadge = newBadge,
                        onNext = { advance() }
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedbackPanel(
    card: GameCard,
    correct: Boolean,
    hindi: Boolean,
    newBadge: String?,
    onNext: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (correct) Neem.copy(alpha = 0.14f) else Sindoor.copy(alpha = 0.14f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(Space4), verticalArrangement = Arrangement.spacedBy(Space2)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space2)) {
                Icon(
                    imageVector = JyotiFlame,
                    contentDescription = null,
                    tint = if (correct) Neem else Sindoor,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = stringResource(if (correct) R.string.spot_correct else R.string.spot_wrong),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (correct) Neem else Sindoor
                )
            }
            Text(
                text = if (hindi) card.whyHi else card.whyEn,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = stringResource(if (card.isScam) R.string.spot_was_scam else R.string.spot_was_real),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            newBadge?.let {
                Text(
                    text = stringResource(R.string.spot_new_badge, badgeLabel(it)),
                    style = MaterialTheme.typography.titleMedium,
                    color = Saffron
                )
            }
            Button(
                onClick = onNext,
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text(stringResource(R.string.spot_next)) }
        }
    }
}

@Composable
private fun RoundDone(stats: StreakLogic.Stats, onAgain: () -> Unit, onBack: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(top = Space6),
        verticalArrangement = Arrangement.spacedBy(Space3),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.spot_round_done),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(R.string.spot_round_summary, stats.accuracy, stats.badges.size, StreakLogic.ALL_BADGES.size),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.spot_best) + " " + stats.bestStreak,
            style = MaterialTheme.typography.titleMedium,
            color = Saffron
        )
        Button(onClick = onAgain, shape = RoundedCornerShape(50), modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text(stringResource(R.string.spot_play_again))
        }
        TextButton(onClick = onBack) { Text(stringResource(R.string.action_back)) }
    }
}

@Composable
private fun badgeLabel(id: String): String = stringResource(
    when (id) {
        StreakLogic.BADGE_FIRST -> R.string.badge_first
        StreakLogic.BADGE_STREAK_5 -> R.string.badge_streak_5
        StreakLogic.BADGE_STREAK_10 -> R.string.badge_streak_10
        StreakLogic.BADGE_PERFECT -> R.string.badge_perfect
        StreakLogic.BADGE_SCAM_HUNTER -> R.string.badge_hunter
        StreakLogic.BADGE_REGULAR -> R.string.badge_regular
        else -> R.string.badge_first
    }
)

private fun todayKey(): String {
    val c = Calendar.getInstance()
    return "%04d-%02d-%02d".format(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
}
