package com.bharatsuraksha.saathi.ui.session

import android.content.Context
import com.bharatsuraksha.saathi.domain.learn.StreakLogic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Game progress (streak, badges, daily round) kept on this phone only. It is a
 * UI-layer store, so nothing else in the app has to change.
 */
object GameStatsStore {

    private const val PREFS = "saathi_game"

    private val _stats = MutableStateFlow(StreakLogic.Stats())
    val stats: StateFlow<StreakLogic.Stats> = _stats

    private var roundDay: String? = null

    fun load(context: Context) {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        _stats.value = StreakLogic.Stats(
            streak = p.getInt("streak", 0),
            bestStreak = p.getInt("best", 0),
            correct = p.getInt("correct", 0),
            answered = p.getInt("answered", 0),
            roundCorrect = 0,
            roundSize = p.getInt("round_size", 10),
            scamsSpotted = p.getInt("spotted", 0),
            days = p.getString("days", "").orEmpty().split(",").filter { it.isNotBlank() },
            badges = p.getString("badges", "").orEmpty().split(",").filter { it.isNotBlank() }.toSet()
        )
        roundDay = p.getString("round_day", null)
    }

    fun save(context: Context, stats: StreakLogic.Stats) {
        _stats.value = stats
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putInt("streak", stats.streak)
            .putInt("best", stats.bestStreak)
            .putInt("correct", stats.correct)
            .putInt("answered", stats.answered)
            .putInt("round_size", stats.roundSize)
            .putInt("spotted", stats.scamsSpotted)
            .putString("days", stats.days.joinToString(","))
            .putString("badges", stats.badges.joinToString(","))
            .apply()
    }

    /** True when today's round has not been recorded yet. */
    fun isNewDay(context: Context, dayKey: String): Boolean {
        if (roundDay == null) {
            roundDay = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("round_day", null)
        }
        return roundDay != dayKey
    }

    fun markDay(context: Context, dayKey: String) {
        roundDay = dayKey
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString("round_day", dayKey)
            .apply()
    }

    fun reset(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
        _stats.value = StreakLogic.Stats()
    }
}
