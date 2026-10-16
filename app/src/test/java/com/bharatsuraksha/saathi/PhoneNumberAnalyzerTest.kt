package com.bharatsuraksha.saathi

import com.bharatsuraksha.saathi.domain.engine.PhoneNumberAnalyzer
import com.bharatsuraksha.saathi.domain.model.Verdict
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneNumberAnalyzerTest {

    @Test
    fun indianMobileIsSafe() {
        val r = PhoneNumberAnalyzer.analyze("+91 98200 45123")
        assertEquals(Verdict.SAFE, r.verdict)
        assertEquals("+919820045123", r.normalized)
    }

    @Test
    fun plainTenDigitMobileIsSafe() {
        val r = PhoneNumberAnalyzer.analyze("9820045123")
        assertEquals(Verdict.SAFE, r.verdict)
    }

    @Test
    fun internationalNumberIsCareful() {
        val r = PhoneNumberAnalyzer.analyze("+1 202 555 0147")
        assertTrue(r.signals.any { it.id == "non_india" })
        assertTrue(r.verdict != Verdict.SAFE)
    }

    @Test
    fun uaeNumberIsCareful() {
        val r = PhoneNumberAnalyzer.analyze("+971 50 123 4567")
        assertTrue(r.signals.any { it.id == "non_india" })
    }

    @Test
    fun repeatedDigitsAreFlagged() {
        val r = PhoneNumberAnalyzer.analyze("9999999999")
        assertTrue(r.signals.any { it.id == "repeated_digits" })
        assertTrue(r.verdict != Verdict.SAFE)
    }

    @Test
    fun sequentialDigitsAreFlagged() {
        val r = PhoneNumberAnalyzer.analyze("1234567890")
        assertTrue(r.signals.any { it.id == "sequential_digits" })
    }

    @Test
    fun shortCodeIsFlagged() {
        val r = PhoneNumberAnalyzer.analyze("56789")
        assertTrue(r.signals.any { it.id == "short_code" })
    }

    @Test
    fun blocklistedNumberIsDangerous() {
        val r = PhoneNumberAnalyzer.analyze("+91 98200 45123", blocklist = setOf("9820045123"))
        assertTrue(r.signals.any { it.id == "blocklisted" })
        assertEquals(Verdict.DANGEROUS, r.verdict)
    }

    @Test
    fun withheldCallerIsFlagged() {
        val r = PhoneNumberAnalyzer.analyze("Private number")
        assertTrue(r.signals.any { it.id == "withheld" })
    }

    @Test
    fun wrongLengthIsFlagged() {
        val r = PhoneNumberAnalyzer.analyze("+91 98765")
        assertTrue(r.signals.any { it.id == "odd_length" })
    }

    @Test
    fun emptyInputIsSafe() {
        val r = PhoneNumberAnalyzer.analyze("")
        assertEquals(Verdict.SAFE, r.verdict)
        assertEquals(0, r.score)
    }

    @Test
    fun scoreStaysInRange() {
        val r = PhoneNumberAnalyzer.analyze("+1 000 000 0000", blocklist = setOf("0000000000"))
        assertTrue(r.score in 0..100)
    }
}
