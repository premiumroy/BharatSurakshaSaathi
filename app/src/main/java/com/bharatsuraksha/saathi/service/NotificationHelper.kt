package com.bharatsuraksha.saathi.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.bharatsuraksha.saathi.MainActivity
import com.bharatsuraksha.saathi.R
import com.bharatsuraksha.saathi.domain.agent.PhraseMatcher
import com.bharatsuraksha.saathi.domain.model.AnalysisResult
import com.bharatsuraksha.saathi.domain.model.Verdict

/** Builds the app's notifications and channels. */
object NotificationHelper {

    const val CHANNEL_ALERTS = "scam_alerts"
    const val CHANNEL_TIPS = "call_tips"
    const val CHANNEL_GUARD = "call_guard"

    const val GUARD_NOTIFICATION_ID = 70010
    private const val GUARD_ALERT_ID = 70011
    private const val REMOTE_ALERT_ID = 70012
    private const val CALL_TIP_ID = 70001

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ALERTS,
                context.getString(R.string.channel_alerts_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = context.getString(R.string.channel_alerts_desc) }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_TIPS,
                context.getString(R.string.channel_tips_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = context.getString(R.string.channel_tips_desc) }
        )
        // Call Guard is a visible, ongoing notice. It is deliberately LOW so it
        // never makes a sound; the whole point is that it is plainly on screen.
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_GUARD,
                context.getString(R.string.channel_guard_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = context.getString(R.string.channel_guard_desc) }
        )
    }

    fun canNotify(context: Context): Boolean {
        val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        return granted && NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun postScamWarning(context: Context, notificationId: Int, result: AnalysisResult, message: String) {
        if (!canNotify(context)) return
        ensureChannels(context)

        val verdictLabel = when (result.verdict) {
            Verdict.DANGEROUS -> context.getString(R.string.verdict_dangerous)
            Verdict.CAREFUL -> context.getString(R.string.verdict_careful)
            Verdict.SAFE -> context.getString(R.string.verdict_safe)
        }
        val reason = result.reasons.firstOrNull()?.reasonEn.orEmpty()
        val title = context.getString(R.string.notif_warning_title, verdictLabel)

        val openIntent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val contentIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setSmallIcon(R.drawable.ic_stat_shield)
            .setContentTitle(title)
            .setContentText(reason)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$reason\n\n${context.getString(R.string.notif_open_to_check)}")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    /**
     * The quiet heads-up for an unknown incoming number. It states the fixed line
     * the phase requires, adds whichever check fired, and offers the ONE way to
     * start Call Guard from here: a tap by the user.
     */
    fun postCallTip(
        context: Context,
        number: String?,
        verdict: Verdict,
        reasonKeys: List<String>
    ) {
        if (!canNotify(context)) return
        ensureChannels(context)
        val title = context.getString(R.string.call_tip_title)
        val lead = context.getString(R.string.agent_call_tip_lead)
        val reason = reasonKeys.firstOrNull()?.let { context.getString(agentReasonRes(it)) }.orEmpty()
        val text = buildString {
            append(lead)
            if (reason.isNotBlank()) {
                append("\n")
                append(reason)
            }
            if (!number.isNullOrBlank()) {
                append("\n")
                append(number)
            }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_TIPS)
            .setSmallIcon(R.drawable.ic_stat_shield)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_stat_shield,
                context.getString(R.string.agent_guard_start),
                guardStartPendingIntent(context, requestCode = CALL_TIP_ID)
            )
            .build()

        NotificationManagerCompat.from(context).notify(CALL_TIP_ID, notification)
    }

    /**
     * The persistent Call Guard notice. It is the visible proof that the
     * microphone is in use, and it carries a Stop action.
     */
    fun callGuardNotification(context: Context, body: String): android.app.Notification {
        ensureChannels(context)
        val openIntent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val contentIntent = PendingIntent.getActivity(
            context,
            GUARD_NOTIFICATION_ID,
            openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(context, CHANNEL_GUARD)
            .setSmallIcon(R.drawable.ic_stat_shield)
            .setContentTitle(context.getString(R.string.agent_guard_listening))
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(contentIntent)
            .addAction(
                R.drawable.ic_stat_shield,
                context.getString(R.string.agent_guard_stop),
                guardStopPendingIntent(context)
            )
            .build()
    }

    /**
     * A live warning during a call. It names the phrases that were heard, in
     * plain words, and never quotes the transcript back.
     */
    fun postGuardAlert(context: Context, match: PhraseMatcher.Match, newKeys: List<String>) {
        if (!canNotify(context)) return
        ensureChannels(context)
        val title = context.getString(R.string.agent_guard_alert_title)
        val lines = newKeys.take(4).mapNotNull { key ->
            guardReasonRes(key)?.let { context.getString(it) }
        }
        val text = buildString {
            append(context.getString(R.string.agent_guard_alert_body))
            lines.forEach { line ->
                append("\n•  ")
                append(line)
            }
            append("\n\n")
            append(context.getString(R.string.verdict_guarantee_note))
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_TIPS)
            .setSmallIcon(R.drawable.ic_stat_shield)
            .setContentTitle(title)
            .setContentText(context.getString(R.string.agent_guard_alert_body))
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(GUARD_ALERT_ID, notification)
    }

    /** Phrase keys heard during a call, in plain language. */
    private fun guardReasonRes(key: String): Int? = when (key) {
        "cbi" -> R.string.guard_heard_cbi
        "police" -> R.string.guard_heard_police
        "customs" -> R.string.guard_heard_customs
        "ed" -> R.string.guard_heard_ed
        "narcotics" -> R.string.guard_heard_narcotics
        "digital_arrest" -> R.string.guard_heard_digital_arrest
        "arrest" -> R.string.guard_heard_arrest
        "court_case" -> R.string.guard_heard_case
        "do_not_disconnect", "stay_on_line" -> R.string.guard_heard_no_disconnect
        "secrecy" -> R.string.guard_heard_secrecy
        "otp", "otp_request" -> R.string.guard_heard_otp
        "pin", "cvv", "password" -> R.string.guard_heard_pin
        "safe_account", "transfer_money" -> R.string.guard_heard_safe_account
        "upi_request", "send_money" -> R.string.guard_heard_upi
        "urgency" -> R.string.guard_heard_urgency
        "parcel" -> R.string.guard_heard_parcel
        else -> null
    }

    /**
     * The urgent one: a remote-access tool and a money app were both used in the
     * last half hour. It says what to do, not just what happened.
     */
    fun postRemoteAccessWarning(
        context: Context,
        overlap: com.bharatsuraksha.saathi.domain.agent.RemoteAccessApps.Overlap
    ) {
        if (!canNotify(context)) return
        ensureChannels(context)
        val app = overlap.remoteAccessPackage?.let { guardLabel(context, it) }.orEmpty()
        val title = context.getString(R.string.remote_alert_title)
        val body = context.getString(R.string.remote_alert_body, app)
        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setSmallIcon(R.drawable.ic_stat_shield)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(REMOTE_ALERT_ID, notification)
    }

    private fun guardLabel(context: Context, packageName: String): String =
        com.bharatsuraksha.saathi.domain.agent.RemoteAccessApps.label(packageName)

    fun guardStartPendingIntent(context: Context, requestCode: Int = GUARD_NOTIFICATION_ID): PendingIntent =
        PendingIntent.getForegroundService(
            context,
            requestCode,
            Intent(context, CallGuardService::class.java).setAction(CallGuardService.ACTION_START),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

    fun guardStopPendingIntent(context: Context): PendingIntent =
        PendingIntent.getService(
            context,
            GUARD_NOTIFICATION_ID + 1,
            Intent(context, CallGuardService::class.java).setAction(CallGuardService.ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

    private fun agentReasonRes(key: String): Int = when (key) {
        "agent_reason_blocked" -> R.string.agent_reason_blocked
        "agent_reason_intl" -> R.string.agent_reason_intl
        "agent_reason_robo" -> R.string.agent_reason_robo
        "agent_reason_not_in_contacts" -> R.string.agent_reason_not_in_contacts
        "agent_reason_trusted_bank" -> R.string.agent_reason_trusted_bank
        "agent_reason_bank_lookalike" -> R.string.agent_reason_bank_lookalike
        "agent_reason_private" -> R.string.agent_reason_private
        else -> R.string.agent_call_tip_lead
    }
}
