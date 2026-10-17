package com.bharatsuraksha.saathi

import com.bharatsuraksha.saathi.domain.learn.TriageScoring
import com.bharatsuraksha.saathi.domain.model.Verdict
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TriageScoringTest {

    @Test
    fun allNoIsSafe() {
        val out = TriageScoring.score(TriageScoring.Answers())
        assertEquals(Verdict.SAFE, out.verdict)
        assertEquals(0, out.score)
        assertTrue(out.reasonKeys.isEmpty())
    }

    @Test
    fun claimingAuthorityAloneIsAtLeastCareful() {
        val out = TriageScoring.score(TriageScoring.Answers(claimedAuthority = true))
        assertEquals(Verdict.CAREFUL, out.verdict)
        assertTrue(out.reasonKeys.contains(TriageScoring.REASON_AUTHORITY))
    }

    @Test
    fun askingForMoneyOrOtpIsDangerous() {
        val out = TriageScoring.score(TriageScoring.Answers(askedMoneyOrOtp = true))
        assertEquals(Verdict.DANGEROUS, out.verdict)
        assertTrue(out.reasonKeys.contains(TriageScoring.REASON_MONEY))
    }

    @Test
    fun authorityPlusSecrecyPlusPressureIsDangerous() {
        val out = TriageScoring.score(
            TriageScoring.Answers(
                claimedAuthority = true,
                askedSecrecy = true,
                createdPanic = true
            )
        )
        assertEquals(Verdict.DANGEROUS, out.verdict)
        assertTrue(out.reasonKeys.contains(TriageScoring.REASON_SECRECY))
        assertTrue(out.reasonKeys.contains(TriageScoring.REASON_PANIC))
    }

    @Test
    fun videoCallAloneStaysSafe() {
        val out = TriageScoring.score(TriageScoring.Answers(videoCall = true))
        assertEquals(Verdict.SAFE, out.verdict)
        assertEquals(15, out.score)
    }

    @Test
    fun scoreNeverExceedsOneHundred() {
        val out = TriageScoring.score(
            TriageScoring.Answers(
                claimedAuthority = true,
                videoCall = true,
                askedMoneyOrOtp = true,
                askedSecrecy = true,
                createdPanic = true
            )
        )
        assertTrue(out.score <= 100)
        assertEquals(Verdict.DANGEROUS, out.verdict)
    }
}
