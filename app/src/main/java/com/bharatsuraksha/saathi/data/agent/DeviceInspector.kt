package com.bharatsuraksha.saathi.data.agent

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.MediaStore
import android.provider.Settings
import com.bharatsuraksha.saathi.domain.agent.RemoteAccessApps
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads the few phone facts the health check needs. Everything here is read-only:
 * nothing is changed, uninstalled or blocked, ever.
 *
 * Honest limits: Android hides other apps from us unless we ask about them by
 * name, so the app list is short by design. Reading other apps' downloaded files
 * needs a permission we deliberately do not request, so that check may simply
 * return nothing.
 */
@Singleton
class DeviceInspector @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /** Accessibility services that are on and that we do not recognise. */
    fun unknownAccessibilityServices(): List<String> {
        val raw = runCatching {
            Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
        }.getOrNull().orEmpty()
        if (raw.isBlank()) return emptyList()

        val known = setOf(
            "com.google.android.marvin.talkback",
            "com.android.talkback",
            "com.samsung.accessibility",
            "com.google.android.apps.accessibility.voiceaccess",
            "com.google.android.accessibility.switchaccess"
        )
        return raw.split(':')
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .filterNot { it.startsWith(context.packageName) }
            .filterNot { entry -> known.any { entry.startsWith(it) } }
            .filterNot { it.startsWith("com.android.") }
            .distinct()
    }

    /**
     * Installed apps that ask for the permissions a takeover tool needs. Only
     * apps Android lets us see are considered, so this list is short by design.
     */
    fun riskyPermissionApps(): List<String> {
        val risky = setOf(
            "android.permission.REQUEST_INSTALL_PACKAGES",
            "android.permission.SYSTEM_ALERT_WINDOW",
            "android.permission.BIND_ACCESSIBILITY_SERVICE"
        )
        val packages = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getInstalledPackages(
                    PackageManager.PackageInfoFlags.of(0L)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getInstalledPackages(0)
            }
        }.getOrDefault(emptyList())

        return packages.asSequence()
            .filter { it.packageName != context.packageName }
            .filter { it.applicationInfo?.flags?.and(android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0 }
            .filter { info ->
                info.requestedPermissions?.any { it in risky } == true
            }
            .map { it.packageName }
            .distinct()
            .toList()
    }

    /**
     * Recently downloaded .apk files. Reading other apps' downloads needs a
     * permission we do not ask for, so this may legitimately come back empty.
     */
    fun recentApkDownloads(windowMillis: Long = 7L * 24L * 60L * 60L * 1000L): List<String> {
        val since = System.currentTimeMillis() - windowMillis
        return runCatching {
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Downloads.EXTERNAL_CONTENT_URI
            } else {
                MediaStore.Files.getContentUri("external")
            }
            val projection = arrayOf(
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.DATE_ADDED
            )
            context.contentResolver.query(
                collection, projection, null, null, "${MediaStore.MediaColumns.DATE_ADDED} DESC"
            )?.use { cursor ->
                val nameIdx = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                val dateIdx = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_ADDED)
                val out = mutableListOf<String>()
                while (cursor.moveToNext() && out.size < 20) {
                    val name = if (nameIdx >= 0) cursor.getString(nameIdx).orEmpty() else ""
                    val added = if (dateIdx >= 0) cursor.getLong(dateIdx) * 1000L else 0L
                    if (name.endsWith(".apk", ignoreCase = true) && added >= since) out += name
                }
                out
            } ?: emptyList()
        }.getOrDefault(emptyList())
    }

    /** The remote-access tools from the phase list that are actually installed. */
    fun installedRemoteAccessApps(): List<String> =
        RemoteAccessApps.PACKAGES.filter { isInstalled(it) }

    private fun isInstalled(packageName: String): Boolean = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                packageName, PackageManager.PackageInfoFlags.of(0L)
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(packageName, 0)
        }
        true
    }.getOrDefault(false)

    // ------------------------------------------------------------ usage stats

    /** True only when the user has granted usage access in Settings. */
    fun hasUsageAccess(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /** When a package was last in the foreground, or null if we cannot know. */
    fun lastUsed(packageName: String, lookBackMillis: Long = 60L * 60L * 1000L): Long? {
        if (!hasUsageAccess()) return null
        val manager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return null
        val end = System.currentTimeMillis()
        return runCatching {
            manager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, end - lookBackMillis, end
            )
                ?.filter { it.packageName == packageName }
                ?.maxOfOrNull { it.lastTimeUsed }
        }.getOrNull()
    }

    /** True when a remote-access tool and a money app were both used just now. */
    fun remoteAccessOverlapNow(): RemoteAccessApps.Overlap {
        val now = System.currentTimeMillis()
        if (!hasUsageAccess()) {
            return RemoteAccessApps.Overlap(null, null, null, urgent = false)
        }
        val remotePackage = installedRemoteAccessApps().firstOrNull()
        val remoteUsed = remotePackage?.let { lastUsed(it) }
        val bankPackage = RemoteAccessApps.BANKING_UPI.firstOrNull { isInstalled(it) }
        val bankUsed = bankPackage?.let { lastUsed(it) }
        val urgent = RemoteAccessApps.isUrgentOverlap(remoteUsed, bankUsed, now)
        return RemoteAccessApps.Overlap(
            remoteAccessPackage = remotePackage,
            remoteUsedMillisAgo = remoteUsed?.let { now - it },
            bankUsedMillisAgo = bankUsed?.let { now - it },
            urgent = urgent
        )
    }
}
