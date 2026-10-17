package com.bharatsuraksha.saathi

import com.bharatsuraksha.saathi.domain.agent.NumberHeuristics
import com.bharatsuraksha.saathi.domain.model.Verdict
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NumberHeuristicsTest {

    private val trusted = setOf("1800123456", "+911800123456")

    @Test
    fun aNumberFromContactsIsQuiet() {
        val a = NumberHeuristics.analyze("9876543210", isInContacts = true)
        assertEquals(Verdict.SAFE, a.verdict)
    }

    @Test
    fun aNumberNotInContactsIsStillSafeButNoted() {
        val a = NumberHeuristics.analyze("9876123450", isInContacts = false)
        assertEquals(Verdict.SAFE, a.verdict)
        assertTrue(a.findings.any { it.ruleId == NumberHeuristics.RULE_NOT_IN_CONTACTS })
    }

    @Test
    fun aHighRiskCountryCodeIsCareful() {
        val a = NumberHeuristics.analyze("+92 301 9876123", isInContacts = false)
        assertEquals(Verdict.CAREFUL, a.verdict)
        assertTrue(a.findings.any { it.ruleId == NumberHeuristics.RULE_INTL })
    }

    @Test
    fun aBangladeshPrefixIsCareful() {
        val a = NumberHeuristics.analyze("+880 171 9876123", isInContacts = false)
        assertTrue(a.findings.any { it.ruleId == NumberHeuristics.RULE_INTL })
    }

    @Test
    fun aRoboPatternIsFlagged() {
        val a = NumberHeuristics.analyze("+91 9999999999", isInContacts = false)
        assertTrue(a.findings.any { it.ruleId == NumberHeuristics.RULE_ROBO })
    }

    @Test
    fun anAscendingRunIsFlagged() {
        assertTrue(NumberHeuristics.isRoboPattern("1234567890"))
        assertTrue(NumberHeuristics.isRoboPattern("9876543210"))
        assertFalse(NumberHeuristics.isRoboPattern("9871245690"))
    }

    @Test
    fun aShortNumberIsNotRobo() {
        assertFalse(NumberHeuristics.isRoboPattern("12345"))
    }

    @Test
    fun aSavedBankNumberLowersTheRisk() {
        val a = NumberHeuristics.analyze("1800123456", isInContacts = false, trustedBankNumbers = trusted)
        assertEquals(Verdict.SAFE, a.verdict)
        assertTrue(a.knownTrustedBank)
        assertTrue(a.findings.any { it.ruleId == NumberHeuristics.RULE_TRUSTED_BANK })
    }

    @Test
    fun anUnsavedShortCodeIsOnlyNoted() {
        val a = NumberHeuristics.analyze("1800999999", isInContacts = false, trustedBankNumbers = trusted)
        assertTrue(a.findings.any { it.ruleId == NumberHeuristics.RULE_BANK_LOOKALIKE })
        assertFalse(a.knownTrustedBank)
    }

    @Test
    fun aBlockedNumberIsDangerous() {
        val a = NumberHeuristics.analyze("9876543210", isInContacts = true, blocklist = setOf("+919876543210"))
        assertEquals(Verdict.DANGEROUS, a.verdict)
        assertTrue(a.findings.any { it.ruleId == NumberHeuristics.RULE_BLOCKED })
    }

    @Test
    fun aPrivateNumberIsNoted() {
        val a = NumberHeuristics.analyze(null, isInContacts = false)
        assertTrue(a.findings.any { it.ruleId == NumberHeuristics.RULE_PRIVATE })
        assertEquals(Verdict.SAFE, a.verdict)
    }

    @Test
    fun normaliseKeepsTheLastTenDigits() {
        assertEquals("9876543210", NumberHeuristics.normalise("+91 98765 43210"))
        assertEquals("1800123456", NumberHeuristics.normalise("1800123456"))
    }
}
