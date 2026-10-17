package com.bharatsuraksha.saathi.domain.learn

import android.content.Context
import kotlinx.serialization.json.Json
import kotlin.random.Random

/**
 * Bundled learning content. Parsing is pure so it can be unit tested; the two
 * loaders just read the assets that ship inside the APK.
 */
object LearnData {

    private val json = Json { ignoreUnknownKeys = true }

    fun parseCards(raw: String): List<GameCard> = json.decodeFromString(raw)

    fun parseWeekly(raw: String): List<WeeklyScam> = json.decodeFromString(raw)

    fun loadCards(context: Context): List<GameCard> =
        parseCards(context.assets.open("spot_the_scam.json").bufferedReader().use { it.readText() })

    fun loadWeekly(context: Context): List<WeeklyScam> =
        parseWeekly(context.assets.open("weekly_scams.json").bufferedReader().use { it.readText() })

    /**
     * The daily round: the same ten cards all day, a different ten tomorrow.
     * Seeded by the day key so it never reshuffles mid-session.
     */
    fun dailyRound(all: List<GameCard>, dayKey: String, size: Int = 10): List<GameCard> {
        if (all.isEmpty()) return emptyList()
        return all.shuffled(Random(dayKey.hashCode().toLong())).take(size)
    }
}
