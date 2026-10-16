package com.bharatsuraksha.saathi

import com.bharatsuraksha.saathi.domain.engine.AdviceProvider
import com.bharatsuraksha.saathi.domain.model.Verdict
import org.junit.Assert.assertTrue
import org.junit.Test

class AdviceProviderTest {

    private val engine = TestRules.engine

    @Test
    fun dangerousScamGivesMoneyAndReportAdvice() {
        val r = engine.analyze("You are under digital arrest. Transfer money to the government account now.")
        val keys = AdviceProvider.adviceKeys(r)
        assertTrue(keys.contains("advice_police_no_arrest"))
        assertTrue(keys.contains("advice_report_1930"))
    }

    @Test
    fun credentialScamGivesOtpAdvice() {
        val r = engine.analyze("Please share your OTP with our customer care.")
        val keys = AdviceProvider.adviceKeys(r)
        assertTrue(keys.contains("advice_dont_share_otp"))
    }

    @Test
    fun linkScamGivesNoClickAdvice() {
        val r = engine.analyze("Your KYC is expiring. Update at http://sbi-kyc.xyz")
        val keys = AdviceProvider.adviceKeys(r)
        assertTrue(keys.contains("advice_dont_click"))
    }

    @Test
    fun safeMessageStillGivesGentleAdvice() {
        val r = engine.analyze("Hi, are we meeting at 5pm?")
        assertTrue(r.verdict == Verdict.SAFE)
        assertTrue(AdviceProvider.adviceKeys(r).isNotEmpty())
    }
}
