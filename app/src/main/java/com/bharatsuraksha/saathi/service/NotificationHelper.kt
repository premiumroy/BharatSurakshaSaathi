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
import com.bharatsuraksha.saathi.domain.model.AnalysisResult
import com.bharatsuraksha.saathi.domain.model.Verdict

/** Builds the app's notifications and channels. */
object NotificationHelper {

    const val CHANNEL_ALERTS = "scam_alerts"
    const val CHANNEL_TIPS = "call_tips"

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

    fun postCallTip(context: Context, number: String?) {
        if (!canNotify(context)) return
        ensureChannels(context)
        val title = context.getString(R.string.call_tip_title)
        val text = context.getString(R.string.call_tip_body, number ?: context.getString(R.string.call_tip_unknown))

        val notification = NotificationCompat.Builder(context, CHANNEL_TIPS)
            .setSmallIcon(R.drawable.ic_stat_shield)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(CALL_TIP_ID, notification)
    }

    private const val CALL_TIP_ID = 70001
}
