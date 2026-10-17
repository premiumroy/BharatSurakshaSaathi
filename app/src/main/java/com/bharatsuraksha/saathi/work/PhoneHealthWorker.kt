package com.bharatsuraksha.saathi.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bharatsuraksha.saathi.data.agent.DeviceInspector
import com.bharatsuraksha.saathi.data.prefs.SettingsRepository
import com.bharatsuraksha.saathi.domain.agent.PhoneHealth
import com.bharatsuraksha.saathi.service.NotificationHelper
import com.bharatsuraksha.saathi.ui.session.PhoneHealthStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * The daily phone-health check. It only LOOKS: it never uninstalls, disables or
 * changes anything. Results are counts and short labels, stored on the phone.
 */
@HiltWorker
class PhoneHealthWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val inspector: DeviceInspector,
    private val settings: SettingsRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        // Off unless the user turned it on, and silent while paused.
        if (!settings.agentPhoneHealth.first()) return Result.success()
        if (settings.isAgentPaused()) return Result.success()

        val accessibility = runCatching { inspector.unknownAccessibilityServices() }.getOrDefault(emptyList())
        val risky = runCatching { inspector.riskyPermissionApps() }.getOrDefault(emptyList())
        val apks = runCatching { inspector.recentApkDownloads() }.getOrDefault(emptyList())
        val remoteInstalled = runCatching { inspector.installedRemoteAccessApps() }.getOrDefault(emptyList())
        val overlap = runCatching { inspector.remoteAccessOverlapNow() }
            .getOrElse { com.bharatsuraksha.saathi.domain.agent.RemoteAccessApps.Overlap(null, null, null, false) }

        val report = PhoneHealth.score(
            PhoneHealth.Flags(
                unknownAccessibilityServices = accessibility.size,
                riskyPermissionApps = risky.size,
                recentApkDownloads = apks.size
            )
        )

        PhoneHealthStore.save(
            applicationContext,
            PhoneHealthStore.Snapshot(
                checkedAt = System.currentTimeMillis(),
                score = report.score,
                level = report.level.name,
                actionKeys = report.actionKeys,
                accessibilityCount = accessibility.size,
                riskyPermissionCount = risky.size,
                apkCount = apks.size,
                accessibilityNames = accessibility.take(10),
                riskyPackageNames = risky.take(10),
                apkNames = apks.take(10),
                remoteAccessInstalled = remoteInstalled,
                remoteUrgent = overlap.urgent,
                usageAccessGranted = inspector.hasUsageAccess()
            )
        )

        // The one urgent case: a takeover tool and a money app used together just now.
        if (overlap.urgent && settings.agentRemoteAlert.first()) {
            NotificationHelper.postRemoteAccessWarning(applicationContext, overlap)
        }
        return Result.success()
    }
}
