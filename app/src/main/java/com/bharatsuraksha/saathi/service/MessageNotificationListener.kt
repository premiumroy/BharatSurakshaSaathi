package com.bharatsuraksha.saathi.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.bharatsuraksha.saathi.data.prefs.SettingsRepository
import com.bharatsuraksha.saathi.data.repository.HistoryRepository
import com.bharatsuraksha.saathi.domain.engine.ScamEngine
import com.bharatsuraksha.saathi.domain.model.Verdict
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Watches incoming message notifications (SMS / WhatsApp / Telegram / others),
 * runs the offline engine on the text, and — ONLY when the verdict is Careful or
 * Dangerous — shows a warning notification.
 *
 * Requires Notification Access (granted manually). It reads only notification
 * text from messaging apps; it never records calls and never reads the SMS store.
 */
@AndroidEntryPoint
class MessageNotificationListener : NotificationListenerService() {

    @Inject lateinit var engine: ScamEngine
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var historyRepository: HistoryRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannels(this)
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val notification = sbn?.notification ?: return
        val packageName = sbn.packageName ?: return

        val extras = notification.extras
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            ?: return
        if (text.isBlank()) return

        val notificationId = (sbn.key?.hashCode() ?: packageName.hashCode()).let {
            if (it == Int.MIN_VALUE) 1 else kotlin.math.abs(it)
        }

        scope.launch {
            if (!settingsRepository.currentAutoCheck()) return@launch

            val watched = settingsRepository.currentWatchedApps()
            val effective = if (watched.isEmpty()) DEFAULT_WATCHED else watched
            if (packageName !in effective) return@launch

            val sensitivity = settingsRepository.currentSensitivity()
            val result = engine.analyze(text, sensitivity)
            if (result.verdict == Verdict.SAFE) return@launch

            historyRepository.add(text, result)
            NotificationHelper.postScamWarning(this@MessageNotificationListener, notificationId, result, text)
        }
    }

    companion object {
        /** Default apps watched when the user has not customised the list. */
        val DEFAULT_WATCHED = setOf(
            "com.whatsapp",
            "com.whatsapp.w4b",
            "com.google.android.apps.messaging",
            "com.android.messaging",
            "com.android.mms",
            "com.samsung.android.messaging",
            "org.telegram.messenger",
            "org.thunderdog.challegram"
        )
    }
}
