package com.bharatsuraksha.saathi.ui.easy

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.bharatsuraksha.saathi.R
import com.bharatsuraksha.saathi.data.prefs.SettingsRepository
import com.bharatsuraksha.saathi.ui.common.isHindiLocale
import com.bharatsuraksha.saathi.ui.theme.BrandGreen
import com.bharatsuraksha.saathi.ui.theme.BrandSaffron
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

    LaunchedEffect(Unit) { viewModel.guide(hindi) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = stringResource(R.string.easy_title),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = stringResource(R.string.easy_hint),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            HugeButton(stringResource(R.string.easy_check_message), BrandGreen, onCheckMessage)
            HugeButton(stringResource(R.string.easy_speak), BrandSaffron, onSpeak)

            val family = contacts.firstOrNull()
            HugeButton(stringResource(R.string.easy_call_family), BrandGreen) {
                if (family != null) {
                    context.startActivity(
                        Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(family)}"))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            }
            if (family == null) {
                Text(
                    text = stringResource(R.string.easy_call_none),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.action_back),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Text(stringResource(R.string.action_back))
            }
        }
    }
}

@Composable
private fun HugeButton(text: String, color: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}
