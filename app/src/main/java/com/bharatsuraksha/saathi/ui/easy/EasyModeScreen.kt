package com.bharatsuraksha.saathi.ui.easy

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.bharatsuraksha.saathi.R
import com.bharatsuraksha.saathi.data.prefs.SettingsRepository
import com.bharatsuraksha.saathi.ui.common.isHindiLocale
import com.bharatsuraksha.saathi.ui.theme.Saffron
import com.bharatsuraksha.saathi.ui.theme.ShieldEmblem
import com.bharatsuraksha.saathi.ui.theme.pressScale
import com.bharatsuraksha.saathi.ui.theme.rememberReduceMotion
import com.bharatsuraksha.saathi.ui.theme.reveal
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class EasyModeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: SettingsRepository
) : ViewModel() {

    val familyContacts: StateFlow<Set<String>> =
        settings.familyContacts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    private var tts: TextToSpeech? = null
    private var ready = false

    init {
        tts = TextToSpeech(context) { status -> ready = status == TextToSpeech.SUCCESS }
    }

    /** Reads the screen hint aloud (voice guidance on each screen). */
    fun guide(hindi: Boolean) {
        if (!ready) return
        tts?.language = if (hindi) Locale("hi", "IN") else Locale("en", "IN")
        tts?.speak(context.getString(R.string.easy_hint), TextToSpeech.QUEUE_FLUSH, null, "guide")
    }

    override fun onCleared() {
        tts?.shutdown()
        super.onCleared()
    }
}

/**
 * Easy Mode: the same identity, calmer and larger. Three huge buttons, one
 * gentle entrance, no looping animation.
 */
@Composable
fun EasyModeScreen(
    onBack: () -> Unit,
    onCheckMessage: () -> Unit,
    onSpeak: () -> Unit,
    viewModel: EasyModeViewModel = hiltViewModel()
) {
    val contacts by viewModel.familyContacts.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val hindi = isHindiLocale()
    val reduce = rememberReduceMotion()

    LaunchedEffect(Unit) { viewModel.guide(hindi) }

    Scaffold(containerColor = Color.Transparent) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))
            ShieldEmblem(
                modifier = Modifier.size(112.dp).reveal(0, reduce),
                tint = Saffron,
                breath = true,
                reduceMotion = reduce
            )
            Text(
                text = stringResource(R.string.easy_title),
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth().reveal(1, reduce)
            )
            Text(
                text = stringResource(R.string.easy_hint),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().reveal(2, reduce)
            )

            HugeButton(
                text = stringResource(R.string.easy_check_message),
                primary = true,
                reduceMotion = reduce,
                index = 3,
                onClick = onCheckMessage
            )
            HugeButton(
                text = stringResource(R.string.easy_speak),
                primary = false,
                reduceMotion = reduce,
                index = 4,
                onClick = onSpeak
            )

            val family = contacts.firstOrNull()
            HugeButton(
                text = stringResource(R.string.easy_call_family),
                primary = false,
                reduceMotion = reduce,
                index = 5,
                onClick = {
                    if (family != null) {
                        context.startActivity(
                            Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(family)}"))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
                }
            )
            if (family == null) {
                Text(
                    text = stringResource(R.string.easy_call_none),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().reveal(6, reduce)
                )
            }

            Spacer(Modifier.height(4.dp))
            HugeButton(
                text = stringResource(R.string.action_back),
                primary = false,
                reduceMotion = reduce,
                index = 7,
                compact = true,
                onClick = onBack
            )
        }
    }
}

@Composable
private fun HugeButton(
    text: String,
    primary: Boolean,
    reduceMotion: Boolean,
    index: Int,
    compact: Boolean = false,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(topStart = 28.dp, bottomEnd = 28.dp, topEnd = 10.dp, bottomStart = 10.dp)
    val container = if (primary) Saffron else MaterialTheme.colorScheme.surface
    val content = if (primary) Color(0xFF2A1600) else MaterialTheme.colorScheme.onSurface
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (compact) 84.dp else 132.dp)
            .reveal(index, reduceMotion)
            .pressScale(reduceMotion)
            .clip(shape)
            .background(container)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center,
            color = content,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
