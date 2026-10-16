package com.bharatsuraksha.saathi

import com.bharatsuraksha.saathi.domain.engine.ScamEngine
import com.bharatsuraksha.saathi.domain.model.Confidence
import com.bharatsuraksha.saathi.domain.model.Verdict
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScamEngineTest {

    private val engine: ScamEngine = TestRules.engine

    @Test
    fun emptyInputIsSafeWithZeroScore() {
        val r = engine.analyze("")
        assertEquals(Verdict.SAFE, r.verdict)
        assertEquals(0, r.score)
        assertEquals(Confidence.HIGH, r.confidence)
    }

    @Test
    fun scoreIsAlwaysBetweenZeroAndHundred() {
        val r = engine.analyze(
            "CBI police digital arrest FIR warrant court OTP CVV UPI PIN account blocked " +
                "KYC suspended lottery won prize loan recovery agent AnyDesk screen share " +
                "transfer money http://sbi-kyc.xyz http://hdfc.verify.xyz http://1.2.3.4/x"
        )
        assertTrue("score=${r.score}", r.score in 0..100)
        assertEquals(Verdict.DANGEROUS, r.verdict)
    }

    @Test
    fun reasonsAreAtMostThree() {
        val r = engine.analyze(
            "CBI digital arrest FIR warrant account blocked KYC lottery won transfer money http://sbi-kyc.xyz"
        )
        assertTrue("reasons=${r.reasons.size}", r.reasons.size <= 3)
    }

    @Test
    fun reasonsAreOrderedByWeightDescending() {
        val r = engine.analyze(
            "Your KYC is expiring. Account will be blocked. Update immediately at http://sbi-kyc.xyz"
        )
        val weights = r.reasons.map { it.weight }
        assertEquals(weights.sortedDescending(), weights)
    }

    @Test
    fun kycScamIsDangerousAndNamesTheRule() {
        val r = engine.analyze("Your KYC is expiring. Update at http://sbi-kyc-update.xyz or account will be blocked.")
        assertEquals(Verdict.DANGEROUS, r.verdict)
        assertTrue(r.matchedRuleIds.contains("kyc_block"))
        assertTrue(r.matchedRuleIds.contains("link_lookalike_brand"))
    }

    @Test
    fun legitOtpDeliveryIsNotFlagged() {
        val r = engine.analyze("Your OTP for login is 4821. Do not share it with anyone.")
        assertEquals(Verdict.SAFE, r.verdict)
        assertFalse(r.matchedRuleIds.contains("otp_request"))
    }

    @Test
    fun askingForOtpIsFlagged() {
        val r = engine.analyze("Please share your OTP with our customer care to complete verification.")
        assertEquals(Verdict.CAREFUL, r.verdict)
        assertTrue(r.matchedRuleIds.contains("otp_request"))
    }

    @Test
    fun digitalArrestIsAlwaysDangerous() {
        val r = engine.analyze("You are under digital arrest. Do not hang up.")
        assertEquals(Verdict.DANGEROUS, r.verdict)
        assertTrue(r.matchedRuleIds.contains("digital_arrest"))
    }

    @Test
    fun hindiDevangariScamIsCaught() {
        val r = engine.analyze("आपका खाता बंद हो जाएगा। तुरंत केवाईसी अपडेट करें।")
        assertTrue(r.verdict != Verdict.SAFE)
        assertTrue(r.matchedRuleIds.contains("kyc_block") || r.matchedRuleIds.contains("urgency_pressure"))
    }

    @Test
    fun sameInputGivesSameOutput() {
        val text = "Your KYC is expiring. Update at http://sbi-kyc.xyz"
        assertEquals(engine.analyze(text), engine.analyze(text))
    }
}
