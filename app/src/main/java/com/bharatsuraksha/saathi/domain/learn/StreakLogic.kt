package com.bharatsuraksha.saathi.domain.learn

/**
 * Streaks and badges. Pure logic, no Android, so it is fully unit tested.
 */
object StreakLogic {

    const val BADGE_FIRST = "first_correct"
    const val BADGE_STREAK_5 = "streak_5"
    const val BADGE_STREAK_10 = "streak_10"
    const val BADGE_PERFECT = "perfect_round"
    const val BADGE_SCAM_HUNTER = "scam_hunter_20"
    const val BADGE_REGULAR = "regular_3"

    val ALL_BADGES = listOf(
        BADGE_FIRST, BADGE_STREAK_5, BADGE_STREAK_10,
        BADGE_PERFECT, BADGE_SCAM_HUNTER, BADGE_REGULAR
    )

    data class Stats(
        val streak: Int = 0,
        val bestStreak: Int = 0,
        val correct: Int = 0,
        val answered: Int = 0,
        val roundCorrect: Int = 0,
        val roundSize: Int = 10,
        val scamsSpotted: Int = 0,
        val days: List<String> = emptyList(),
        val badges: Set<String> = emptySet()
    ) {
        val accuracy: Int get() = if (answered == 0) 0 else (correct * 100) / answered
    }

    /** Records one answer and awards any badge it unlocks. */
    fun answer(stats: Stats, correct: Boolean, wasScam: Boolean, dayKey: String): Stats {
        val newStreak = if (correct) stats.streak + 1 else 0
        val updated = stats.copy(
            streak = newStreak,
            bestStreak = maxOf(stats.bestStreak, newStreak),
            correct = stats.correct + if (correct) 1 else 0,
            answered = stats.answered + 1,
            roundCorrect = stats.roundCorrect + if (correct) 1 else 0,
            scamsSpotted = stats.scamsSpotted + if (correct && wasScam) 1 else 0,
            days = (stats.days + dayKey).distinct().takeLast(40)
        )
        return updated.copy(badges = updated.badges + earned(updated))
    }

    /** Closes a round: a clean sweep earns the perfect-round badge. */
    fun finishRound(stats: Stats): Stats {
        val badges = if (stats.roundCorrect >= stats.roundSize) stats.badges + BADGE_PERFECT else stats.badges
        return stats.copy(roundCorrect = 0, badges = badges)
    }

    fun earned(stats: Stats): Set<String> {
        val out = mutableSetOf<String>()
        if (stats.correct >= 1) out += BADGE_FIRST
        if (stats.streak >= 5) out += BADGE_STREAK_5
        if (stats.streak >= 10) out += BADGE_STREAK_10
        if (stats.scamsSpotted >= 20) out += BADGE_SCAM_HUNTER
        if (consecutiveDays(stats.days) >= 3) out += BADGE_REGULAR
        return out
    }

    /** Longest run of consecutive calendar days (keys look like 2026-10-17). */
    fun consecutiveDays(days: List<String>): Int {
        val numbers = days.map { dayNumber(it) }.filter { it > 0 }.distinct().sorted()
        if (numbers.isEmpty()) return 0
        var run = 1
        var best = 1
        for (i in 1 until numbers.size) {
            if (numbers[i] == numbers[i - 1] + 1) {
                run += 1
                best = maxOf(best, run)
            } else {
                run = 1
            }
        }
        return best
    }

    /**
     * Days since the civil epoch for a yyyy-MM-dd key, computed arithmetically
     * so it needs no java.time and works on every API level.
     */
    fun dayNumber(key: String): Int {
        val parts = key.split("-")
        if (parts.size != 3) return -1
        val y = parts[0].toIntOrNull() ?: return -1
        val m = parts[1].toIntOrNull() ?: return -1
        val d = parts[2].toIntOrNull() ?: return -1
        if (m !in 1..12 || d !in 1..31) return -1
        var yy = y
        var mm = m
        if (mm <= 2) {
            yy -= 1
            mm += 12
        }
        val era = (if (yy >= 0) yy else yy - 399) / 400
        val yoe = yy - era * 400
        val doy = (153 * (mm + (if (mm > 2) -3 else 9)) + 2) / 5 + d - 1
        val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
        return era * 146097 + doe
    }
}
