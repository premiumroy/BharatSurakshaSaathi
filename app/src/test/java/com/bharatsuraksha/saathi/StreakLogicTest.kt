package com.bharatsuraksha.saathi

import com.bharatsuraksha.saathi.domain.learn.StreakLogic
import com.bharatsuraksha.saathi.domain.learn.StreakLogic.Stats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StreakLogicTest {

    @Test
    fun correctAnswerGrowsTheStreak() {
        var s = Stats()
        s = StreakLogic.answer(s, correct = true, wasScam = true, dayKey = "2026-10-17")
        assertEquals(1, s.streak)
        s = StreakLogic.answer(s, correct = true, wasScam = false, dayKey = "2026-10-17")
        assertEquals(2, s.streak)
        assertEquals(2, s.bestStreak)
    }

    @Test
    fun wrongAnswerResetsTheStreakButKeepsTheBest() {
        var s = Stats()
        repeat(4) { s = StreakLogic.answer(s, correct = true, wasScam = true, dayKey = "2026-10-17") }
        assertEquals(4, s.streak)
        s = StreakLogic.answer(s, correct = false, wasScam = false, dayKey = "2026-10-17")
        assertEquals(0, s.streak)
        assertEquals(4, s.bestStreak)
    }

    @Test
    fun firstCorrectAwardsTheFirstBadge() {
        val s = StreakLogic.answer(Stats(), correct = true, wasScam = false, dayKey = "2026-10-17")
        assertTrue(s.badges.contains(StreakLogic.BADGE_FIRST))
    }

    @Test
    fun fiveInARowAwardsTheStreakBadge() {
        var s = Stats()
        repeat(5) { s = StreakLogic.answer(s, correct = true, wasScam = true, dayKey = "2026-10-17") }
        assertTrue(s.badges.contains(StreakLogic.BADGE_STREAK_5))
        assertFalse(s.badges.contains(StreakLogic.BADGE_STREAK_10))
    }

    @Test
    fun perfectRoundIsAwardedWhenTheRoundCloses() {
        var s = Stats(roundSize = 10)
        repeat(10) { s = StreakLogic.answer(s, correct = true, wasScam = true, dayKey = "2026-10-17") }
        s = StreakLogic.finishRound(s)
        assertTrue(s.badges.contains(StreakLogic.BADGE_PERFECT))
        assertEquals(0, s.roundCorrect)
    }

    @Test
    fun imperfectRoundDoesNotEarnThePerfectBadge() {
        var s = Stats(roundSize = 10)
        repeat(9) { s = StreakLogic.answer(s, correct = true, wasScam = true, dayKey = "2026-10-17") }
        s = StreakLogic.answer(s, correct = false, wasScam = false, dayKey = "2026-10-17")
        s = StreakLogic.finishRound(s)
        assertFalse(s.badges.contains(StreakLogic.BADGE_PERFECT))
    }

    @Test
    fun twentyScamsSpottedAwardsTheHunterBadge() {
        var s = Stats()
        repeat(20) { s = StreakLogic.answer(s, correct = true, wasScam = true, dayKey = "2026-10-17") }
        assertTrue(s.badges.contains(StreakLogic.BADGE_SCAM_HUNTER))
    }

    @Test
    fun threeConsecutiveDaysAwardsTheRegularBadge() {
        var s = Stats()
        s = StreakLogic.answer(s, correct = true, wasScam = true, dayKey = "2026-10-15")
        s = StreakLogic.answer(s, correct = true, wasScam = true, dayKey = "2026-10-16")
        s = StreakLogic.answer(s, correct = true, wasScam = true, dayKey = "2026-10-17")
        assertTrue(s.badges.contains(StreakLogic.BADGE_REGULAR))
    }

    @Test
    fun gappedDaysDoNotCountAsConsecutive() {
        assertEquals(
            1,
            StreakLogic.consecutiveDays(listOf("2026-10-15", "2026-10-17", "2026-10-20"))
        )
    }

    @Test
    fun dayNumbersAreArithmeticallyConsecutive() {
        val a = StreakLogic.dayNumber("2026-02-28")
        val b = StreakLogic.dayNumber("2026-03-01")
        assertEquals(1, b - a)
        assertEquals(-1, StreakLogic.dayNumber("not-a-date"))
    }
}
