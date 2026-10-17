package com.bharatsuraksha.saathi.domain.agent

/**
 * Turns the flags found by the daily phone-health job into a 0-100 score and at
 * most three plain-language actions. It never changes anything on the phone.
 * Pure logic, unit tested.
 */
object PhoneHealth {

    data class Flags(
        val unknownAccessibilityServices: Int = 0,
        val riskyPermissionApps: Int = 0,
        val recentApkDownloads: Int = 0
    )

    enum class Level { GOOD, WATCH, RISKY }

    data class Report(
        val score: Int,
        val level: Level,
        val actionKeys: List<String>
    )

    const val ACTION_ACCESSIBILITY = "health_action_accessibility"
    const val ACTION_PERMISSIONS = "health_action_permissions"
    const val ACTION_APK = "health_action_apk"
    const val ACTION_ALL_CLEAR = "health_action_all_clear"

    fun score(flags: Flags): Report {
        var deduction = 0
        val actions = mutableListOf<String>()

        if (flags.unknownAccessibilityServices > 0) {
            deduction += (flags.unknownAccessibilityServices * 25).coerceAtMost(50)
            actions += ACTION_ACCESSIBILITY
        }
        if (flags.riskyPermissionApps > 0) {
            deduction += (flags.riskyPermissionApps * 15).coerceAtMost(30)
            actions += ACTION_PERMISSIONS
        }
        if (flags.recentApkDownloads > 0) {
            deduction += (flags.recentApkDownloads * 10).coerceAtMost(20)
            actions += ACTION_APK
        }

        val score = (100 - deduction).coerceIn(0, 100)
        val level = when {
            score >= 80 -> Level.GOOD
            score >= 55 -> Level.WATCH
            else -> Level.RISKY
        }
        return Report(score, level, if (actions.isEmpty()) listOf(ACTION_ALL_CLEAR) else actions.take(3))
    }
}
