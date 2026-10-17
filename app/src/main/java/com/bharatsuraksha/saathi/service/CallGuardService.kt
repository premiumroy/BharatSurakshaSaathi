package com.bharatsuraksha.saathi.service

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.bharatsuraksha.saathi.data.prefs.SettingsRepository
import com.bharatsuraksha.saathi.domain.agent.PhraseMatcher
import com.bharatsuraksha.saathi.domain.model.Verdict
import com.bharatsuraksha.saathi.ui.session.AgentActivityStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.min

/**
 * CALL GUARD — live help during a call, and ONLY when the user asks for it.
 *
 * It is started by a tap: the action on the call-tip notification, or the Quick
 * Settings tile. It is never started by a call, never by a timer, and never by
 * the app in the background.
 *
 * While it runs it shows a persistent notification and an on-screen "listening"
 * indicator, so there is no way for it to be on without the user knowing.
 *
 * WHAT IT CAN AND CANNOT DO — Android does not let any app record call audio.
 * This service listens through the ordinary microphone, so it only works when the
 * call is on speakerphone, and it will pick up the room as well as the call. It
 * never writes audio or text anywhere: everything is matched in memory and thrown
 * away. It stops when the call ends, or after thirty minutes, whichever is first.
 */
@AndroidEntryPoint
class CallGuardService : Service() {

    @Inject lateinit var settings: SettingsRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var recognizer: SpeechRecognizer? = null
    private var autoStopJob: Job? = null
    private var callWatchJob: Job? = null
    private var restartJob: Job? = null

    private var listening = false
    private var onDevice = false
    private var consecutiveErrors = 0
    private var startedAt = 0L
    private val warnedKeys = LinkedHashSet<String>()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopEverything()
            return START_NOT_STICKY
        }

        // The microphone is only available to a foreground service, and only when
        // the user has already granted it. If not, say so plainly and stop.
        val micGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (!micGranted) {
            stopEverything()
            return START_NOT_STICKY
        }

        startedAt = System.currentTimeMillis()
        warnedKeys.clear()
        state.value = GuardState(
            running = true,
            onDevice = false,
            text = "",
            verdict = Verdict.SAFE,
            findings = emptyList()
        )

        startForeground(
            NotificationHelper.GUARD_NOTIFICATION_ID,
            NotificationHelper.callGuardNotification(
                this,
                getString(R.string.agent_guard_notif_body)
            )
        )

        beginListening()
        scheduleAutoStop()
        watchForCallEnd()
        return START_NOT_STICKY // never resurrect itself
    }

    // ------------------------------------------------------------- listening

    private fun beginListening() {
        val engineReady = SpeechRecognizer.isRecognitionAvailable(this)
        if (!engineReady) {
            publish(getString(R.string.agent_guard_no_engine))
            stopEverything()
            return
        }

        recognizer = createRecognizer()
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        listening = true
        runCatching { recognizer?.startListening(intent) }
    }

    private fun createRecognizer(): SpeechRecognizer? {
        onDevice = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            SpeechRecognizer.isOnDeviceRecognitionAvailable(this)
        val created = try {
            if (onDevice) {
                SpeechRecognizer.createOnDeviceSpeechRecognizer(this)
            } else {
                SpeechRecognizer.createSpeechRecognizer(this)
            }
        } catch (e: Exception) {
            null
        }
        created?.setRecognitionListener(listener)
        return created
    }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            consecutiveErrors = 0
        }

        override fun onBeginningOfSpeech() = Unit
        override fun onRmsChanged(rmsdB: Float) = Unit
        override fun onBufferReceived(buffer: ByteArray?) = Unit
        override fun onEndOfSpeech() = Unit
        override fun onEvent(eventType: Int, params: Bundle?) = Unit

        override fun onPartialResults(partialResults: Bundle?) {
            val text = partialResults
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                .orEmpty()
            examine(text)
        }

        override fun onResults(results: Bundle?) {
            val text = results
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                .orEmpty()
            examine(text)
            restartListening(delayMillis = 500L)
        }

        override fun onError(error: Int) {
            consecutiveErrors += 1
            // Back off a little when the engine keeps returning nothing, so we
            // never spin in a tight loop.
            val backoff = min(500L * consecutiveErrors, 5_000L)
            restartListening(delayMillis = backoff)
        }
    }

    private fun restartListening(delayMillis: Long) {
        if (!listening) return
        restartJob?.cancel()
        restartJob = scope.launch {
            delay(delayMillis)
            if (!listening) return@launch
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            runCatching { recognizer?.startListening(intent) }
        }
    }

    /**
     * The only thing we ever do with recognised text: match it against the scam
     * phrases, in memory, and warn if something new appears. Nothing is written
     * to disk, and nothing is sent anywhere.
     */
    private fun examine(text: String) {
        if (text.isBlank()) return
        val match = PhraseMatcher.match(text)
        state.value = GuardState(
            running = true,
            onDevice = onDevice,
            text = text,
            verdict = match.verdict,
            findings = match.hits.map { it.phraseKey }
        )

        if (match.verdict == Verdict.SAFE) return

        val newKeys = match.hits.map { it.phraseKey }.filterNot { it in warnedKeys }
        if (newKeys.isEmpty()) return
        warnedKeys.addAll(match.hits.map { it.phraseKey })

        AgentActivityStore.recordCall(this)
        NotificationHelper.postGuardAlert(this, match, newKeys)
    }

    // ------------------------------------------------------------- stopping

    private fun scheduleAutoStop() {
        autoStopJob?.cancel()
        autoStopJob = scope.launch {
            delay(MAX_MILLIS)
            stopEverything()
        }
    }

    /**
     * Watches the audio mode to notice the call ending. This needs no permission
     * at all: AudioManager reports MODE_IN_CALL while a call is up. We allow a
     * short grace period at the start and require two clear readings in a row.
     */
    private fun watchForCallEnd() {
        callWatchJob?.cancel()
        callWatchJob = scope.launch {
            val audio = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            var clearReadings = 0
            while (isActive && listening) {
                delay(5_000)
                val mode = audio?.mode ?: return@launch
                val inCall = mode == AudioManager.MODE_IN_CALL ||
                    mode == AudioManager.MODE_IN_COMMUNICATION
                val elapsed = System.currentTimeMillis() - startedAt
                clearReadings = if (!inCall) clearReadings + 1 else 0
                if (!inCall && clearReadings >= 2 && elapsed > 20_000L) {
                    stopEverything()
                    return@launch
                }
            }
        }
    }

    private fun stopEverything() {
        listening = false
        autoStopJob?.cancel()
        callWatchJob?.cancel()
        restartJob?.cancel()
        runCatching { recognizer?.stopListening() }
        runCatching { recognizer?.destroy() }
        recognizer = null
        state.value = state.value.copy(running = false, text = "", findings = emptyList())
        runCatching { stopForeground(STOP_FOREGROUND_REMOVE) }
        stopSelf()
    }

    override fun onDestroy() {
        listening = false
        runCatching { recognizer?.destroy() }
        recognizer = null
        scope.cancel()
        super.onDestroy()
    }

    private fun publish(message: String) {
        if (!NotificationHelper.canNotify(this)) return
        NotificationManagerCompat.from(this).notify(
            NotificationHelper.GUARD_NOTIFICATION_ID + 2,
            NotificationHelper.callGuardNotification(this, message)
        )
    }

    data class GuardState(
        val running: Boolean = false,
        val onDevice: Boolean = false,
        val text: String = "",
        val verdict: Verdict = Verdict.SAFE,
        val findings: List<String> = emptyList()
    )

    companion object {
        const val ACTION_START = "com.bharatsuraksha.saathi.action.CALL_GUARD_START"
        const val ACTION_STOP = "com.bharatsuraksha.saathi.action.CALL_GUARD_STOP"
        const val MAX_MILLIS = 30L * 60L * 1000L

        /** Live state for the on-screen indicator. Process-wide, memory only. */
        val state = MutableStateFlow(GuardState())

        fun isRunning(): Boolean = state.value.running

        fun start(context: Context) {
            val intent = Intent(context, CallGuardService::class.java).setAction(ACTION_START)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, CallGuardService::class.java).setAction(ACTION_STOP)
            runCatching { context.startService(intent) }
        }
    }
}
