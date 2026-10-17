package com.bharatsuraksha.saathi.domain.safety

/**
 * The "golden hour": the first hour after money leaves, when a report gives the
 * best chance of freezing it. Pure logic, unit tested.
 */
object GoldenHour {

    const val WINDOW_MILLIS: Long = 60L * 60L * 1000L

    enum class Level { CALM, SOON, URGENT, EXPIRED }

    data class Status(
        val elapsedMillis: Long,
        val remainingMillis: Long,
        val expired: Boolean
    )

    fun status(startedAt: Long, now: Long): Status {
        val elapsed = (now - startedAt).coerceAtLeast(0L)
        val remaining = (WINDOW_MILLIS - elapsed).coerceAtLeast(0L)
        return Status(elapsed, remaining, elapsed >= WINDOW_MILLIS)
    }

    fun level(remainingMillis: Long): Level = when {
        remainingMillis <= 0L -> Level.EXPIRED
        remainingMillis <= 10L * 60L * 1000L -> Level.URGENT
        remainingMillis <= 30L * 60L * 1000L -> Level.SOON
        else -> Level.CALM
    }

    /** mm:ss, always two digits, for the on-screen countdown. */
    fun format(millis: Long): String {
        val total = (millis.coerceAtLeast(0L) / 1000L)
        val minutes = total / 60L
        val seconds = total % 60L
        return "%02d:%02d".format(minutes, seconds)
    }
}
