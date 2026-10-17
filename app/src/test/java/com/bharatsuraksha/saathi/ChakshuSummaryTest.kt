package com.bharatsuraksha.saathi

import com.bharatsuraksha.saathi.domain.safety.ChakshuSummary
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChakshuSummaryTest {

    private fun report(
        number: String = "9876543210",
        dateText: String = "17/10/2026",
        timeText: String = "14:30",
        medium: String = ChakshuSummary.MEDIUM_CALL,
        category: String = "Digital arrest",
        hasScreenshot: Boolean = false,
        notes: String = ""
    ) = ChakshuSummary.Report(number, dateText, timeText, medium, category, hasScreenshot, notes)

    @Test
    fun summaryCarriesEveryField() {
        val text = ChakshuSummary.build(report(), hindi = false)
        assertTrue(text.contains("9876543210"))
        assertTrue(text.contains("17/10/2026"))
        assertTrue(text.contains("14:30"))
        assertTrue(text.contains("Call"))
        assertTrue(text.contains("Digital arrest"))
    }

    @Test
    fun summarySaysTheAppSentNothing() {
        assertTrue(ChakshuSummary.build(report(), hindi = false).contains("sent nothing"))
        assertTrue(ChakshuSummary.build(report(), hindi = true).contains("ऐप ने कुछ नहीं भेजा"))
    }

    @Test
    fun screenshotAndNotesAppearOnlyWhenPresent() {
        val withBoth = ChakshuSummary.build(
            report(hasScreenshot = true, notes = "called twice"),
            hindi = false
        )
        assertTrue(withBoth.contains("Screenshot: yes"))
        assertTrue(withBoth.contains("called twice"))

        val without = ChakshuSummary.build(report(), hindi = false)
        assertTrue(without.contains("Screenshot: no"))
        assertFalse(without.contains("Extra notes"))
    }

    @Test
    fun mediumIsRenderedPerLanguage() {
        assertTrue(ChakshuSummary.build(report(medium = ChakshuSummary.MEDIUM_CALL), hindi = true).contains("कॉल"))
        assertTrue(ChakshuSummary.build(report(medium = ChakshuSummary.MEDIUM_WHATSAPP), hindi = false).contains("WhatsApp"))
    }

    @Test
    fun completenessNeedsNumberDateAndCategory() {
        assertTrue(ChakshuSummary.isComplete(report()))
        assertFalse(ChakshuSummary.isComplete(report(number = "  ")))
        assertFalse(ChakshuSummary.isComplete(report(dateText = "")))
        assertFalse(ChakshuSummary.isComplete(report(category = "")))
        // time is part of completeness too
        assertFalse(ChakshuSummary.isComplete(report(timeText = "")))
    }
}
