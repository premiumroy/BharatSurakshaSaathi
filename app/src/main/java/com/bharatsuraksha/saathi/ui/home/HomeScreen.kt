package com.bharatsuraksha.saathi.ui.home

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bharatsuraksha.saathi.R
import com.bharatsuraksha.saathi.ui.common.BrandFooter
import com.bharatsuraksha.saathi.ui.theme.Saffron
import com.bharatsuraksha.saathi.ui.theme.ShieldEmblem
import com.bharatsuraksha.saathi.ui.theme.pressScale
import com.bharatsuraksha.saathi.ui.theme.rememberReduceMotion
import com.bharatsuraksha.saathi.ui.theme.reveal
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
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
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val easyMode by viewModel.easyMode.collectAsStateWithLifecycle()
    val kidsMode by viewModel.kidsMode.collectAsStateWithLifecycle()
    val incoming by viewModel.incomingText.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val reduce = rememberReduceMotion()
    var scanning by remember { mutableStateOf(false) }

    LaunchedEffect(incoming) {
        incoming?.let {
            viewModel.onTextChange(it)
            viewModel.onIncomingConsumed()
        }
    }
    // The scan is held for at least 700ms so it reads as a deliberate gesture.
    LaunchedEffect(Unit) {
        viewModel.navigateToResult.collect {
            if (!reduce) delay(700)
            scanning = false
            onOpenResult()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = { SaathiHeader(onHistory = onOpenHistory, onSettings = onOpenSettings) }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (kidsMode) {
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier.reveal(0, reduce)
                    ) {
                        Text(
                            stringResource(R.string.home_kids_badge),
                            modifier = Modifier.padding(14.dp),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // ---- hero: offset shield, left-aligned heavy type, deliberate overlap ----
                Box(
                    Modifier.fillMaxWidth().height(190.dp).reveal(1, reduce)
                ) {
                    ShieldEmblem(
                        modifier = Modifier
                            .size(180.dp)
                            .align(Alignment.CenterEnd)
                            .offset(x = 26.dp, y = (-4).dp),
                        tint = Saffron,
                        reduceMotion = reduce
                    )
                    Column(
                        Modifier.align(Alignment.CenterStart).fillMaxWidth(0.70f)
                    ) {
                        Text(
                            stringResource(R.string.home_title),
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            stringResource(R.string.home_subtitle),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                OutlinedTextField(
                    value = state.text,
                    onValueChange = viewModel::onTextChange,
                    modifier = Modifier.fillMaxWidth().height(160.dp).reveal(2, reduce),
                    label = { Text(stringResource(R.string.home_input_label)) },
                    placeholder = { Text(stringResource(R.string.home_input_hint)) },
                    shape = RoundedCornerShape(8.dp),
                    isError = state.errorRes != null
                )

                state.errorRes?.let {
                    Text(
                        stringResource(it),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.reveal(3, reduce)) {
                    OutlinedButton(
                        onClick = {
                            val clip = clipboard.getText()?.text
                            if (!clip.isNullOrBlank()) viewModel.onTextChange(clip)
                        },
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(Icons.Filled.ContentPaste, contentDescription = null)
                        Spacer(Modifier.padding(4.dp))
                        Text(stringResource(R.string.home_paste_button))
                    }
                    OutlinedButton(
                        onClick = { viewModel.onTextChange("") },
                        shape = RoundedCornerShape(50)
                    ) { Text(stringResource(R.string.home_clear_button)) }
                }

                // ---- the single saffron primary action ----
                PillButton(
                    text = stringResource(R.string.home_check_button),
                    icon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Color(0xFF2A1600)) },
                    reduceMotion = reduce,
                    modifier = Modifier.reveal(4, reduce)
                ) {
                    scanning = true
                    viewModel.check()
                }

                OutlinedButton(
                    onClick = onOpenEasy,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth().height(54.dp).reveal(5, reduce)
                ) {
                    Text(stringResource(R.string.home_easy_button), fontWeight = FontWeight.Bold)
                }

                Text(
                    stringResource(R.string.home_quick_tools),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.reveal(6, reduce)
                )
                // Asymmetric tool grid: rows alternate their inset.
                ToolRow(0.dp, stringResource(R.string.home_tool_image), onOpenImage, stringResource(R.string.home_tool_link), onOpenLink, reduce, 7)
                ToolRow(22.dp, stringResource(R.string.home_tool_phone), onOpenPhone, stringResource(R.string.home_tool_tips), onOpenTips, reduce, 8)
                ToolRow(8.dp, stringResource(R.string.home_tool_recover), onOpenRecover, stringResource(R.string.home_tool_settings), onOpenSettings, reduce, 9)
                ToolRow(30.dp, stringResource(R.string.easy_speak), onOpenVoice, null, null, reduce, 10)

                Text(
                    stringResource(R.string.home_examples_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.reveal(11, reduce)
                )
                listOf(R.string.home_example_1, R.string.home_example_2, R.string.home_example_3).forEachIndexed { i, res ->
                    val example = stringResource(res)
                    AssistChip(
                        onClick = { viewModel.onTextChange(example) },
                        label = { Text(example.take(52) + if (example.length > 52) "…" else "", maxLines = 1) },
                        modifier = Modifier.reveal(12 + i, reduce)
                    )
                }

                Card(
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.reveal(16, reduce)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(R.string.easy_mode_label),
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
                    stringResource(R.string.history_privacy_note),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                BrandFooter()
            }

            if (scanning) ScanOverlay(Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun SaathiHeader(onHistory: () -> Unit, onSettings: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 20.dp, end = 6.dp, top = 10.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                stringResource(R.string.brand_made_by),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onHistory) {
            Icon(Icons.Filled.History, stringResource(R.string.home_history_button), tint = MaterialTheme.colorScheme.onBackground)
        }
        IconButton(onClick = onSettings) {
            Icon(Icons.Filled.Settings, stringResource(R.string.settings_title), tint = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
private fun PillButton(
    text: String,
    icon: @Composable () -> Unit,
    reduceMotion: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(66.dp)
            .pressScale(reduceMotion)
            .clip(RoundedCornerShape(50))
            .background(Saffron)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            icon()
            Text(
                text,
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF2A1600)
            )
        }
    }
}

@Composable
private fun ToolRow(
    inset: androidx.compose.ui.unit.Dp,
    labelA: String, onClickA: () -> Unit,
    labelB: String?, onClickB: (() -> Unit)?,
    reduceMotion: Boolean,
    index: Int
) {
    Row(
        Modifier.fillMaxWidth().padding(start = inset).reveal(index, reduceMotion),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ToolChip(labelA, onClickA, Modifier.weight(1f))
        if (labelB != null && onClickB != null) ToolChip(labelB, onClickB, Modifier.weight(1f))
    }
}

@Composable
private fun ToolChip(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(8.dp)
    ) { Text(label, maxLines = 1, style = MaterialTheme.typography.labelLarge) }
}

/** The "checking" gesture: a saffron scan line sweeping over a dimmed shield. */
@Composable
private fun ScanOverlay(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "scan")
    val y by transition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.88f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Reverse),
        label = "scanline"
    )
    val lines = listOf(R.string.home_checking_1, R.string.home_checking_2, R.string.home_checking_3)
    var lineIndex by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(900)
            lineIndex = (lineIndex + 1) % lines.size
        }
    }
    Box(
        modifier
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Box(Modifier.size(200.dp)) {
                ShieldEmblem(Modifier.fillMaxSize(), tint = Saffron, breath = false, reduceMotion = true)
                Canvas(Modifier.fillMaxSize()) {
                    val yy = size.height * y
                    drawLine(Saffron, androidx.compose.ui.geometry.Offset(0f, yy), androidx.compose.ui.geometry.Offset(size.width, yy), 2.dp.toPx())
                }
            }
            Text(
                stringResource(lines[lineIndex]),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        }
    }
}
