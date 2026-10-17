package com.bharatsuraksha.saathi.domain.agent

/**
 * Keeps the agent quiet. A sender gets at most [maxPerWindow] alerts in
 * [windowMillis], and the same warning is never shown twice. Pure logic, unit
 * tested, so the notification path is easy to reason about.
 */
object RateLimiter {

    enum class Decision { ALERT, RATE_LIMITED, DUPLICATE }

    const val DEFAULT_WINDOW_MILLIS = 10L * 60L * 1000L
    const val DEFAULT_MAX_PER_WINDOW = 2

    class Limiter(
        private val windowMillis: Long = DEFAULT_WINDOW_MILLIS,
        private val maxPerWindow: Int = DEFAULT_MAX_PER_WINDOW,
        private val memoryMillis: Long = 6L * 60L * 60L * 1000L
    ) {
        private val alerts = mutableMapOf<String, MutableList<Long>>()
        private val seenFingerprints = mutableMapOf<String, Long>()

        fun decide(senderKey: String, fingerprint: String, nowMillis: Long): Decision {
            prune(nowMillis)

            val lastSeen = seenFingerprints[fingerprint]
            if (lastSeen != null && nowMillis - lastSeen <= memoryMillis) {
                return Decision.DUPLICATE
            }

            val times = alerts.getOrPut(senderKey) { mutableListOf() }
            if (times.size >= maxPerWindow) {
                return Decision.RATE_LIMITED
            }

            times += nowMillis
            seenFingerprints[fingerprint] = nowMillis
            return Decision.ALERT
        }

        fun forgetSender(senderKey: String) {
            alerts.remove(senderKey)
        }

        fun reset() {
            alerts.clear()
            seenFingerprints.clear()
        }

        private fun prune(nowMillis: Long) {
            alerts.values.forEach { list -> list.removeAll { nowMillis - it > windowMillis } }
            seenFingerprints.entries.removeAll { nowMillis - it.value > memoryMillis }
        }
    }

    /** A stable key for one sender inside one app. */
    fun senderKey(packageName: String, sender: String?): String =
        packageName + "|" + (sender?.lowercase()?.trim() ?: "unknown")

    /** A stable fingerprint for the warning itself, so repeats collapse. */
    fun fingerprint(packageName: String, sender: String?, reasonIds: List<String>): String =
        senderKey(packageName, sender) + "|" + reasonIds.sorted().joinToString(",")
}
