package com.bharatsuraksha.saathi

import com.bharatsuraksha.saathi.domain.learn.CodeWordFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CodeWordFlowTest {

    @Test
    fun quietMessageFromAKnownPersonLooksReal() {
        val advice = CodeWordFlow.assess(CodeWordFlow.Situation())
        assertEquals(CodeWordFlow.Level.LIKELY_REAL, advice.level)
    }

    @Test
    fun newNumberAloneAsksForVerification() {
        val advice = CodeWordFlow.assess(CodeWordFlow.Situation(fromNewNumber = true))
        assertEquals(CodeWordFlow.Level.VERIFY_FIRST, advice.level)
        assertTrue(advice.reasonKeys.contains(CodeWordFlow.REASON_NEW_NUMBER))
    }

    @Test
    fun newNumberPlusMoneyPlusUrgencyIsLikelyFraud() {
        val advice = CodeWordFlow.assess(
            CodeWordFlow.Situation(fromNewNumber = true, asksMoney = true, urgent = true)
        )
        assertEquals(CodeWordFlow.Level.LIKELY_FRAUD, advice.level)
        assertTrue(advice.reasonKeys.contains(CodeWordFlow.REASON_MONEY))
    }

    @Test
    fun theRightCodeWordLowersTheRisk() {
        val without = CodeWordFlow.assess(
            CodeWordFlow.Situation(hasCodeWord = true, asksMoney = true)
        )
        val with = CodeWordFlow.assess(
            CodeWordFlow.Situation(hasCodeWord = true, codeWordMatches = true, asksMoney = true)
        )
        assertEquals(CodeWordFlow.Level.VERIFY_FIRST, without.level)
        assertEquals(CodeWordFlow.Level.LIKELY_REAL, with.level)
        assertTrue(with.reasonKeys.contains(CodeWordFlow.REASON_MATCHED))
    }

    @Test
    fun aCodeWordMustBeUsableButNotGuessable() {
        assertFalse(CodeWordFlow.isCodeWordAcceptable("1234"))
        assertFalse(CodeWordFlow.isCodeWordAcceptable("abc"))
        assertFalse(CodeWordFlow.isCodeWordAcceptable("password"))
        assertTrue(CodeWordFlow.isCodeWordAcceptable("peela ghoda"))
    }

    @Test
    fun checklistAndCloneWarningsArePresent() {
        assertEquals(3, CodeWordFlow.CHECKLIST_KEYS.size)
        assertEquals(3, CodeWordFlow.VOICE_CLONE_KEYS.size)
    }
}
