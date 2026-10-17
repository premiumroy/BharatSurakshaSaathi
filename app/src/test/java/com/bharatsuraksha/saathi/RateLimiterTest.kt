package com.bharatsuraksha.saathi

import com.bharatsuraksha.saathi.domain.agent.RateLimiter
import org.junit.Assert.assertEquals
import org.junit.Test

class RateLimiterTest {

    private val window = 10L * 60L * 1000L

    @Test
    fun theFirstWarningGoesOut() {
        val limiter = RateLimiter.Limiter(window, maxPerWindow = 2)
        assertEquals(
            RateLimiter.Decision.ALERT,
            limiter.decide("whatsapp|rahul", "fp1", nowMillis = 1_000L)
        )
    }

    @Test
    fun theSameWarningIsNeverShownTwice() {
        val limiter = RateLimiter.Limiter(window, maxPerWindow = 2)
        limiter.decide("whatsapp|rahul", "fp1", 1_000L)
        assertEquals(
            RateLimiter.Decision.DUPLICATE,
            limiter.decide("whatsapp|rahul", "fp1", 2_000L)
        )
    }

    @Test
    fun aDifferentWarningFromTheSameSenderIsAllowed() {
        val limiter = RateLimiter.Limiter(window, maxPerWindow = 2)
        limiter.decide("whatsapp|rahul", "fp1", 1_000L)
        assertEquals(
            RateLimiter.Decision.ALERT,
            limiter.decide("whatsapp|rahul", "fp2", 2_000L)
        )
    }

    @Test
    fun theSenderIsRateLimitedAfterTheCap() {
        val limiter = RateLimiter.Limiter(window, maxPerWindow = 2)
        limiter.decide("whatsapp|rahul", "fp1", 1_000L)
        limiter.decide("whatsapp|rahul", "fp2", 2_000L)
        assertEquals(
            RateLimiter.Decision.RATE_LIMITED,
            limiter.decide("whatsapp|rahul", "fp3", 3_000L)
        )
    }

    @Test
    fun theWindowRollsOver() {
        val limiter = RateLimiter.Limiter(window, maxPerWindow = 1)
        limiter.decide("whatsapp|rahul", "fp1", 1_000L)
        assertEquals(
            RateLimiter.Decision.RATE_LIMITED,
            limiter.decide("whatsapp|rahul", "fp2", 2_000L)
        )
        assertEquals(
            RateLimiter.Decision.ALERT,
            limiter.decide("whatsapp|rahul", "fp3", 1_000L + window + 1)
        )
    }

    @Test
    fun sendersDoNotAffectEachOther() {
        val limiter = RateLimiter.Limiter(window, maxPerWindow = 1)
        limiter.decide("whatsapp|rahul", "fp1", 1_000L)
        assertEquals(
            RateLimiter.Decision.ALERT,
            limiter.decide("whatsapp|priya", "fp2", 1_000L)
        )
    }

    @Test
    fun forgettingASenderFreesTheBudget() {
        val limiter = RateLimiter.Limiter(window, maxPerWindow = 1)
        limiter.decide("whatsapp|rahul", "fp1", 1_000L)
        limiter.forgetSender("whatsapp|rahul")
        assertEquals(
            RateLimiter.Decision.ALERT,
            limiter.decide("whatsapp|rahul", "fp2", 2_000L)
        )
    }

    @Test
    fun fingerprintsAreStableWhateverTheOrderOfReasons() {
        val a = RateLimiter.fingerprint("com.whatsapp", "Rahul", listOf("r2", "r1"))
        val b = RateLimiter.fingerprint("com.whatsapp", "rahul", listOf("r1", "r2"))
        assertEquals(a, b)
    }

    @Test
    fun senderKeyIsCaseInsensitive() {
        assertEquals(
            RateLimiter.senderKey("com.whatsapp", "RAHUL"),
            RateLimiter.senderKey("com.whatsapp", "rahul")
        )
    }
}
