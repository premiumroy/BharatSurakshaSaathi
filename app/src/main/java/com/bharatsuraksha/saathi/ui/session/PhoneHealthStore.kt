package com.bharatsuraksha.saathi.ui.session

import android.content.Context
import com.bharatsuraksha.saathi.domain.agent.PhoneHealth
import com.bharatsuraksha.saathi.domain.agent.RemoteAccessApps
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * The result of the last phone-health check, kept on the phone so the screen can
 * show it. Only counts and short labels are stored — never app contents.
 */
object PhoneHealthStore {

    private const val PREFS = "saathi_phone_health"

    data class Snapshot(
        val checkedAt: Long = 0L,
        val score: Int = 100,
        val level: String = PhoneHealth.Level.GOOD.name,
        val actionKeys: List<String> = listOf(PhoneHealth.ACTION_ALL_CLEAR),
        val accessibilityCount: Int = 0,
        val riskyPermissionCount: Int = 0,
        val apkCount: Int = 0,
        val accessibilityNames: List<String> = emptyList(),
        val riskyPackageNames: List<String> = emptyList(),
        val apkNames: List<String> = emptyList(),
        val remoteAccessInstalled: List<String> = emptyList(),
        val remoteUrgent: Boolean = false,
        val usageAccessGranted: Boolean = false
    )

    private val _snapshot = MutableStateFlow(Snapshot())
    val snapshot: StateFlow<Snapshot> = _snapshot

    fun load(context: Context) {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        _snapshot.value = Snapshot(
            checkedAt = p.getLong("checked_at", 0L),
            score = p.getInt("score", 100),
            level = p.getString("level", PhoneHealth.Level.GOOD.name).orEmpty(),
            actionKeys = p.getString("actions", PhoneHealth.ACTION_ALL_CLEAR)
                .orEmpty().split(",").filter { it.isNotBlank() },
            accessibilityCount = p.getInt("acc_count", 0),
            riskyPermissionCount = p.getInt("perm_count", 0),
            apkCount = p.getInt("apk_count", 0),
            accessibilityNames = p.getString("acc_names", "").orEmpty().split("\n").filter { it.isNotBlank() },
            riskyPackageNames = p.getString("perm_names", "").orEmpty().split("\n").filter { it.isNotBlank() },
            apkNames = p.getString("apk_names", "").orEmpty().split("\n").filter { it.isNotBlank() },
            remoteAccessInstalled = p.getString("remote", "").orEmpty().split("\n").filter { it.isNotBlank() },
            remoteUrgent = p.getBoolean("remote_urgent", false),
            usageAccessGranted = p.getBoolean("usage_access", false)
        )
    }

    fun save(context: Context, snapshot: Snapshot) {
        _snapshot.value = snapshot
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putLong("checked_at", snapshot.checkedAt)
            .putInt("score", snapshot.score)
            .putString("level", snapshot.level)
            .putString("actions", snapshot.actionKeys.joinToString(","))
            .putInt("acc_count", snapshot.accessibilityCount)
            .putInt("perm_count", snapshot.riskyPermissionCount)
            .putInt("apk_count", snapshot.apkCount)
            .putString("acc_names", snapshot.accessibilityNames.joinToString("\n"))
            .putString("perm_names", snapshot.riskyPackageNames.joinToString("\n"))
            .putString("apk_names", snapshot.apkNames.joinToString("\n"))
            .putString("remote", snapshot.remoteAccessInstalled.joinToString("\n"))
            .putBoolean("remote_urgent", snapshot.remoteUrgent)
            .putBoolean("usage_access", snapshot.usageAccessGranted)
            .apply()
    }

    fun labelFor(packageName: String): String = RemoteAccessApps.label(packageName)
}
