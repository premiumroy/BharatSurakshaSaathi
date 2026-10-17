package com.bharatsuraksha.saathi.ui.home

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bharatsuraksha.saathi.R
import com.bharatsuraksha.saathi.ui.common.BrandFooter
import com.bharatsuraksha.saathi.ui.theme.JyotiFlame
import com.bharatsuraksha.saathi.ui.theme.JyotiGear
import com.bharatsuraksha.saathi.ui.theme.JyotiHero
import com.bharatsuraksha.saathi.ui.theme.JyotiLamp
import com.bharatsuraksha.saathi.ui.theme.JyotiState
import com.bharatsuraksha.saathi.ui.theme.MotionTokens
import com.bharatsuraksha.saathi.ui.theme.Saffron
import com.bharatsuraksha.saathi.ui.theme.Space2
import com.bharatsuraksha.saathi.ui.theme.Space3
import com.bharatsuraksha.saathi.ui.theme.Space4
import com.bharatsuraksha.saathi.ui.theme.Space6
import com.bharatsuraksha.saathi.ui.theme.Space8
import com.bharatsuraksha.saathi.ui.theme.entrance
import com.bharatsuraksha.saathi.ui.theme.latticeRipple
import com.bharatsuraksha.saathi.ui.theme.motionAllowed
import com.bharatsuraksha.saathi.ui.theme.rememberLowRam
import com.bharatsuraksha.saathi.ui.theme.tappable
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    onOpenResult: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenImage: () -> Unit,
    onOpenLink: () -> Unit,
    onOpenPhone: () -> Unit,
    onOpenTips: () -> Unit,
    onOpenRecover: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenEasy: () -> Unit,
    onOpenVoice: () -> Unit,
    onOpenHelpNow: () -> Unit,
    onOpenQr: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val easyMode by viewModel.easyMode.collectAsStateWithLifecycle()
    val kidsMode by viewModel.kidsMode.collectAsStateWithLifecycle()
    val incoming by viewModel.incomingText.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val motion = motionAllowed()
    val lowRam = rememberLowRam()

    var scanning by remember { mutableStateOf(false) }
    var rippleKey by remember { mutableIntStateOf(0) }
    var relight by remember { mutableIntStateOf(0) }
    var pull by remember { mutableStateOf(0f) }
    var parallax by remember { mutableStateOf(Offset.Zero) }
    var lit by remember { mutableStateOf(false) }
    var scanIndex by remember { mutableIntStateOf(0) }

    val scanProgress by animateFloatAsState(
        targetValue = if (scanning) 1f else 0f,
        animationSpec = tween(MotionTokens.HeroScanMin, easing = LinearEasing),
        label = "scan"
    )

    LaunchedEffect(incoming) {
        incoming?.let {
            viewModel.onTextChange(it)
            viewModel.onIncomingConsumed()
        }
    }
    // Hold the scan for at least the hero minimum so it reads as a gesture.
    LaunchedEffect(Unit) {
        viewModel.navigateToResult.collect {
            if (motion) delay(MotionTokens.HeroScanMin.toLong())
            scanning = false
            onOpenResult()
        }
    }

    // Pull-down relights the lamp for a moment.
    LaunchedEffect(relight) {
        if (relight > 0) {
            lit = true
            delay(1200)
            lit = false
        }
    }
    LaunchedEffect(scanning) {
        if (scanning) {
            while (true) {
                delay(900)
                scanIndex = (scanIndex + 1) % 3
            }
        }
    }

    // Pull down at the top to light the lamp and refresh.
    val pullConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (available.y > 0f) {
                    pull = (pull + available.y * 0.5f).coerceAtMost(190f)
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (pull > 110f) {
                    relight += 1
                }
                pull = 0f
                return Velocity.Zero
            }
        }
    }

    val heroState = when {
        scanning -> JyotiState.Scanning
        lit -> JyotiState.Clear
        else -> JyotiState.Idle
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = { JyotiHeader(onHistory = onOpenHistory, onSettings = onOpenSettings) }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(pullConnection)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Space4)
                    .padding(bottom = Space8),
                verticalArrangement = Arrangement.spacedBy(Space4)
            ) {
                // ---- hero and headline share a row, so they can never overlap ----
                Row(
                    modifier = Modifier.fillMaxWidth().entrance(0, motion),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Space2)
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.home_title),
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.size(Space2))
                        Text(
                            text = stringResource(R.string.home_subtitle),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        Modifier
                            .size(132.dp)
                            .offset(x = 10.dp, y = (-(pull / 4f)).dp)
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent(PointerEventPass.Initial)
                                        val pos = event.changes.firstOrNull()?.position
                                        if (pos != null) {
                                            parallax = Offset(
                                                ((pos.x / size.width) - 0.5f) * 2f,
                                                ((pos.y / size.height) - 0.5f) * 2f
                                            )
                                        }
                                    }
                                }
                            }
                    ) {
                        JyotiHero(
                            state = heroState,
                            motion = motion,
                            lowRam = lowRam,
                            scanProgress = scanProgress,
                            parallax = parallax
                        )
                    }
                }

                if (kidsMode) {
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier.entrance(1, motion)
                    ) {
                        Text(
                            stringResource(R.string.home_kids_badge),
                            modifier = Modifier.padding(Space3),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                OutlinedTextField(
                    value = state.text,
                    onValueChange = viewModel::onTextChange,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp).entrance(2, motion),
                    label = { Text(stringResource(R.string.home_input_label)) },
                    placeholder = { Text(stringResource(R.string.home_input_hint)) },
                    shape = RoundedCornerShape(8.dp),
                    isError = state.errorRes != null
                )

                state.errorRes?.let {
                    Text(
                        text = stringResource(it),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(Space3), modifier = Modifier.entrance(3, motion)) {
                    OutlinedButton(
                        onClick = {
                            val clip = clipboard.getText()?.text
                            if (!clip.isNullOrBlank()) viewModel.onTextChange(clip)
                        },
                        shape = RoundedCornerShape(50)
                    ) { Text(stringResource(R.string.home_paste_button)) }
                    OutlinedButton(
                        onClick = { viewModel.onTextChange("") },
                        shape = RoundedCornerShape(50)
                    ) { Text(stringResource(R.string.home_clear_button)) }
                }

                // ---- the one saffron action ----
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 64.dp)
                        .entrance(4, motion)
                        .latticeRipple(rippleKey, Saffron, motion)
                        .tappable(motion = motion) {
                            rippleKey += 1
                            scanning = true
                            viewModel.check()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 64.dp)
                            .background(Saffron, RoundedCornerShape(50)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space2)) {
                            Icon(JyotiFlame, contentDescription = null, tint = Color(0xFF2A1600), modifier = Modifier.size(24.dp))
                            Text(
                                text = stringResource(R.string.home_check_button),
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(0xFF2A1600)
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = onOpenEasy,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp).entrance(5, motion)
                ) { Text(stringResource(R.string.home_easy_button), fontWeight = FontWeight.Bold) }

                // Always reachable, from the very top of Home.
                OutlinedButton(
                    onClick = onOpenHelpNow,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp).entrance(5, motion)
                ) { Text(stringResource(R.string.helpnow_title), fontWeight = FontWeight.ExtraBold) }

                Text(
                    text = stringResource(R.string.home_quick_tools),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.entrance(6, motion)
                )
                // Rows alternate their inset for deliberate asymmetry. Labels wrap to two lines.
                ToolRow(0.dp, R.string.home_tool_image to onOpenImage, R.string.home_tool_link to onOpenLink, motion, 7)
                ToolRow(20.dp, R.string.home_tool_phone to onOpenPhone, R.string.home_tool_tips to onOpenTips, motion, 8)
                ToolRow(8.dp, R.string.home_tool_recover to onOpenRecover, R.string.home_tool_settings to onOpenSettings, motion, 9)
                ToolRow(28.dp, R.string.easy_speak to onOpenVoice, null, motion, 10)
                ToolRow(14.dp, R.string.home_tool_qr to onOpenQr, null, motion, 11)

                Text(
                    text = stringResource(R.string.home_examples_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.entrance(11, motion)
                )
                listOf(R.string.home_example_1, R.string.home_example_2, R.string.home_example_3).forEachIndexed { i, res ->
                    val example = stringResource(res)
                    AssistChip(
                        onClick = { viewModel.onTextChange(example) },
                        label = {
                            Text(
                                text = example,
                                maxLines = 2,
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        modifier = Modifier.entrance(12 + i, motion)
                    )
                }

                Card(shape = MaterialTheme.shapes.medium, modifier = Modifier.entrance(16, motion)) {
                    Column(Modifier.padding(Space4), verticalArrangement = Arrangement.spacedBy(Space2)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(R.string.easy_mode_label),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Switch(checked = easyMode, onCheckedChange = viewModel::onEasyModeChange)
                        }
                        TextButton(onClick = {
                            context.startActivity(
                                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }) { Text(stringResource(R.string.home_notif_button)) }
                    }
                }

                Text(
                    text = stringResource(R.string.home_pull_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.history_privacy_note),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                BrandFooter()
            }

            if (scanning) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.62f))
                        .tappable(motion = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Space6)
                    ) {
                        Box(Modifier.size(220.dp)) {
                            JyotiHero(
                                state = JyotiState.Scanning,
                                motion = motion,
                                lowRam = lowRam,
                                scanProgress = scanProgress
                            )
                        }
                        Text(
                            text = stringResource(scanningLine(scanIndex)),
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun JyotiHeader(onHistory: () -> Unit, onSettings: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = Space4, end = 6.dp, top = Space3, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.brand_made_by),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onHistory, modifier = Modifier.size(48.dp)) {
            Icon(JyotiLamp, contentDescription = stringResource(R.string.home_history_button), tint = MaterialTheme.colorScheme.onBackground)
        }
        IconButton(onClick = onSettings, modifier = Modifier.size(48.dp)) {
            Icon(JyotiGear, contentDescription = stringResource(R.string.settings_title), tint = MaterialTheme.colorScheme.onBackground)
        }
    }
}

private fun scanningLine(index: Int): Int = when (index) {
    0 -> R.string.home_checking_1
    1 -> R.string.home_checking_2
    else -> R.string.home_checking_3
}

@Composable
private fun ToolRow(
    inset: androidx.compose.ui.unit.Dp,
    a: Pair<Int, () -> Unit>,
    b: Pair<Int, () -> Unit>?,
    motion: Boolean,
    index: Int
) {
    Row(
        Modifier.fillMaxWidth().padding(start = inset).entrance(index, motion),
        horizontalArrangement = Arrangement.spacedBy(Space3)
    ) {
        ToolChip(a.first, a.second, motion, Modifier.weight(1f))
        if (b != null) ToolChip(b.first, b.second, motion, Modifier.weight(1f))
    }
}

@Composable
private fun ToolChip(labelRes: Int, onClick: () -> Unit, motion: Boolean, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 60.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelLarge,
            maxLines = 2
        )
    }
}
