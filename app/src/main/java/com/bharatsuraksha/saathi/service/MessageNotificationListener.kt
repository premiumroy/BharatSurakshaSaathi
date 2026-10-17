package com.bharatsuraksha.saathi.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.bharatsuraksha.saathi.data.prefs.SettingsRepository
import com.bharatsuraksha.saathi.data.repository.HistoryRepository
import com.bharatsuraksha.saathi.domain.agent.RateLimiter
import com.bharatsuraksha.saathi.domain.engine.ScamEngine
import com.bharatsuraksha.saathi.domain.model.Verdict
import com.bharatsuraksha.saathi.ui.session.AgentActivityStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Message watch. Reads the text of new message notifications from the apps the
 * user chose, runs the offline engine, and shows a warning ONLY when the verdict
 * is Careful or Dangerous.
 *
 * Quiet by design: warnings are rate limited per sender and the same warning is
 * never repeated. Trusted senders are skipped, muted apps are skipped, and the
 * whole thing is off until the user turns it on.
 *
 * It reads notification text only. It never records calls, never reads the SMS
 * store and never writes anything to disk.
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
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString()
            ?: return
        if (text.isBlank()) return

        val notificationId = (sbn.key?.hashCode() ?: packageName.hashCode()).let {
            if (it == Int.MIN_VALUE) 1 else kotlin.math.abs(it)
        }

        scope.launch {
            // Opt-in, and the user may have paused the agent.
            if (!settingsRepository.agentMessageWatch.first()) return@launch
            if (settingsRepository.isAgentPaused()) return@launch

            val muted = settingsRepository.mutedApps.first()
            if (packageName in muted) return@launch

            val watched = settingsRepository.currentWatchedApps()
            val effective = if (watched.isEmpty()) DEFAULT_WATCHED else watched
            if (packageName !in effective) return@launch

            val sender = senderFrom(title, text)
            val trusted = settingsRepository.trustedSenders.first()
            if (sender != null && trusted.any { it.equals(sender, ignoreCase = true) }) return@launch

            val result = engine.analyze(text, settingsRepository.currentSensitivity())
            // Only Careful and Dangerous ever reach the user.
            if (result.verdict == Verdict.SAFE) return@launch

            val key = RateLimiter.senderKey(packageName, sender)
            val fingerprint = RateLimiter.fingerprint(packageName, sender, result.matchedRuleIds)
            when (limiter.decide(key, fingerprint, System.currentTimeMillis())) {
                RateLimiter.Decision.ALERT -> Unit
                else -> {
                    AgentActivityStore.recordMessage(this@MessageNotificationListener, warned = false)
                    return@launch
                }
            }

            historyRepository.add(text, result)
            NotificationHelper.postScamWarning(
                this@MessageNotificationListener,
                notificationId,
                result,
                text
            )
            AgentActivityStore.recordMessage(this@MessageNotificationListener, warned = true)
        }
    }

    /** The sender is usually the notification title; fall back to the first line. */
    private fun senderFrom(title: String, text: String): String? {
        val t = title.trim()
        if (t.isNotEmpty() && t.length <= 40) return t
        val firstLine = text.lineSequence().firstOrNull()?.trim().orEmpty()
        return firstLine.takeIf { it.isNotEmpty() && it.length <= 40 }
    }

    companion object {
        /** One limiter for the process, so the budget is shared across apps. */
        private val limiter = RateLimiter.Limiter()

        /**
         * Default apps watched when the user has not customised the list:
         * SMS, WhatsApp, Telegram, Instagram DMs and the common mail apps.
         */
        val DEFAULT_WATCHED = setOf(
            "com.whatsapp",
            "com.whatsapp.w4b",
            "com.google.android.apps.messaging",
            "com.android.messaging",
            "com.android.mms",
            "com.samsung.android.messaging",
            "org.telegram.messenger",
            "org.thunderdog.challegram",
            "com.instagram.android",
            "com.google.android.gm",
            "com.microsoft.outlook",
            "com.samsung.android.email.provider"
        )
    }
}
