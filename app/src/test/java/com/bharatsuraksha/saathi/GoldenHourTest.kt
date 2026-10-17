package com.bharatsuraksha.saathi

import com.bharatsuraksha.saathi.domain.safety.GoldenHour
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GoldenHourTest {

    private val start = 1_700_000_000_000L

    @Test
    fun startsWithTheWholeWindowLeft() {
        val s = GoldenHour.status(start, start)
        assertEquals(GoldenHour.WINDOW_MILLIS, s.remainingMillis)
        assertFalse(s.expired)
    }

    @Test
    fun countsDownAsTimePasses() {
        val s = GoldenHour.status(start, start + 30L * 60L * 1000L)
        assertEquals(30L * 60L * 1000L, s.remainingMillis)
        assertFalse(s.expired)
    }

    @Test
    fun expiresAfterTheWindow() {
        val s = GoldenHour.status(start, start + 61L * 60L * 1000L)
        assertTrue(s.expired)
        assertEquals(0L, s.remainingMillis)
    }

    @Test
    fun neverGoesNegativeIfTheClockMovesBack() {
        val s = GoldenHour.status(start, start - 5_000L)
        assertEquals(0L, s.elapsedMillis)
        assertEquals(GoldenHour.WINDOW_MILLIS, s.remainingMillis)
    }

    @Test
    fun urgencyRisesAsTimeRunsOut() {
        assertEquals(GoldenHour.Level.CALM, GoldenHour.level(45L * 60L * 1000L))
        assertEquals(GoldenHour.Level.SOON, GoldenHour.level(20L * 60L * 1000L))
        assertEquals(GoldenHour.Level.URGENT, GoldenHour.level(5L * 60L * 1000L))
        assertEquals(GoldenHour.Level.EXPIRED, GoldenHour.level(0L))
    }

    @Test
    fun formatsAsMinutesAndSeconds() {
        assertEquals("60:00", GoldenHour.format(GoldenHour.WINDOW_MILLIS))
        assertEquals("01:30", GoldenHour.format(90_000L))
        assertEquals("00:00", GoldenHour.format(-10L))
    }
}
