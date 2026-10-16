package com.bharatsuraksha.saathi.ui.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.bharatsuraksha.saathi.R
import com.bharatsuraksha.saathi.data.prefs.SettingsRepository
import com.bharatsuraksha.saathi.data.repository.HistoryRepository
import com.bharatsuraksha.saathi.domain.engine.ScamEngine
import com.bharatsuraksha.saathi.domain.model.AnalysisResult
import com.bharatsuraksha.saathi.ui.common.BigButton
import com.bharatsuraksha.saathi.ui.common.VerdictPill
import com.bharatsuraksha.saathi.ui.common.isHindiLocale
import com.bharatsuraksha.saathi.ui.session.LastResultStore
import com.bharatsuraksha.saathi.ui.theme.BrandGreen
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

data class VoiceState(
    val listening: Boolean = false,
    val heard: String = "",
    val result: AnalysisResult? = null,
    val errorRes: Int? = null,
    val speaking: Boolean = false
)

@HiltViewModel
class VoiceViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val engine: ScamEngine,
    private val settings: SettingsRepository,
    private val historyRepository: HistoryRepository
) : ViewModel(), RecognitionListener {

    private val _state = MutableStateFlow(VoiceState())
    val state: StateFlow<VoiceState> = _state.asStateFlow()

    private var recognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var hindi = false

    init {
        tts = TextToSpeech(context) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
            if (ttsReady) {
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _state.update { it.copy(speaking = true) }
                    }

                    override fun onDone(utteranceId: String?) {
                        _state.update { it.copy(speaking = false) }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _state.update { it.copy(speaking = false) }
                    }
                })
            }
        }
    }

    fun isRecognitionAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun startListening(hindi: Boolean) {
        this.hindi = hindi
        if (!isRecognitionAvailable()) {
            _state.update { it.copy(errorRes = R.string.voice_not_available) }
            return
        }
        if (recognizer == null) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(context).also {
                it.setRecognitionListener(this)
            }
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (hindi) "hi-IN" else "en-IN")
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        _state.update { it.copy(listening = true, errorRes = null, heard = "") }
        recognizer?.startListening(intent)
    }

    fun stopListening() {
        recognizer?.stopListening()
        _state.update { it.copy(listening = false) }
    }

    fun speak(text: String) {
        if (!ttsReady || text.isBlank()) return
        tts?.language = if (hindi) Locale("hi", "IN") else Locale("en", "IN")
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "saathi")
    }

    fun stopSpeaking() {
        tts?.stop()
        _state.update { it.copy(speaking = false) }
    }

    /** Speaks the last result aloud. */
    fun speakResult() {
        val result = _state.value.result ?: return
        speak(buildSpeech(result))
    }

    private fun buildSpeech(result: AnalysisResult): String {
        val verdictWord = context.getString(
            when (result.verdict) {
                com.bharatsuraksha.saathi.domain.model.Verdict.SAFE -> R.string.verdict_safe
                com.bharatsuraksha.saathi.domain.model.Verdict.CAREFUL -> R.string.verdict_careful
                com.bharatsuraksha.saathi.domain.model.Verdict.DANGEROUS -> R.string.verdict_dangerous
            }
        )
        val reason = result.reasons.firstOrNull()?.text(hindi).orEmpty()
        return if (reason.isBlank()) verdictWord else "$verdictWord. $reason"
    }

    private fun analyze(text: String) {
        viewModelScope.launch {
            val sensitivity = settings.currentSensitivity()
            val result = withContext(Dispatchers.Default) { engine.analyze(text, sensitivity) }
            LastResultStore.message = text
            LastResultStore.result = result
            historyRepository.add(text, result)
            _state.update { it.copy(result = result) }
            speak(buildSpeech(result))
        }
    }

    // ---- RecognitionListener ----

    override fun onResults(results: Bundle?) {
        val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
        _state.update { it.copy(listening = false, heard = text) }
        if (text.isNotBlank()) analyze(text)
    }

    override fun onError(error: Int) {
        _state.update { it.copy(listening = false, errorRes = R.string.voice_not_available) }
    }

    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() { _state.update { it.copy(listening = false) } }
    override fun onPartialResults(partialResults: Bundle?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}

    override fun onCleared() {
        recognizer?.destroy()
        tts?.shutdown()
        super.onCleared()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceScreen(
    onBack: () -> Unit,
    viewModel: VoiceViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val hindi = isHindiLocale()

    val micLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.startListening(hindi)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.voice_title), fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(stringResource(R.string.voice_intro), style = MaterialTheme.typography.bodyLarge)

            if (!viewModel.isRecognitionAvailable()) {
                Text(stringResource(R.string.voice_not_available), color = MaterialTheme.colorScheme.error)
            }

            BigButton(
                text = stringResource(if (state.listening) R.string.voice_listening else R.string.voice_tap),
                onClick = {
                    if (state.listening) {
                        viewModel.stopListening()
                    } else {
                        micLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                    }
                }
            )

            state.errorRes?.let {
                Text(stringResource(it), color = MaterialTheme.colorScheme.error)
            }

            if (state.heard.isNotBlank()) {
                Text(stringResource(R.string.voice_heard), fontWeight = FontWeight.Bold)
                Card {
                    Text(
                        state.heard,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            state.result?.let { result ->
                VerdictPill(result.verdict)
                BigButton(
                    text = stringResource(
                        if (state.speaking) R.string.voice_speaking else R.string.voice_speak_result
                    ),
                    onClick = {
                        if (state.speaking) viewModel.stopSpeaking() else viewModel.speakResult()
                    }
                )
            }

            BigButton(
                text = stringResource(R.string.voice_again),
                onClick = { micLauncher.launch(android.Manifest.permission.RECORD_AUDIO) },
                container = BrandGreen
            )
        }
    }
}
