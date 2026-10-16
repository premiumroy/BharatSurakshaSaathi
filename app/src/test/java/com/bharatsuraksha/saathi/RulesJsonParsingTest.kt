package com.bharatsuraksha.saathi

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the JSON asset: it must parse, contain enough rules, and — crucially —
 * every regex must compile. A bad pattern should fail the build, not crash users.
 */
class RulesJsonParsingTest {

    private val ruleSet = TestRules.ruleSet

    @Test
    fun parsesAndHasRules() {
        assertTrue("expected many text rules", ruleSet.textRules.size >= 15)
        assertTrue("expected link signals", ruleSet.linkRules.signals.isNotEmpty())
    }

    @Test
    fun thresholdsAreOrdered() {
        assertTrue(ruleSet.thresholds.careful < ruleSet.thresholds.dangerous)
        assertTrue(ruleSet.thresholds.dangerous <= 100)
    }

    @Test
    fun everyPatternCompiles() {
        val broken = mutableListOf<String>()
        for (rule in ruleSet.textRules) {
            for (p in rule.patterns) {
                runCatching { Regex(p, RegexOption.IGNORE_CASE) }
                    .onFailure { broken += "${rule.id}: '$p' -> ${it.message}" }
            }
        }
        assertTrue("Broken regex patterns:\n${broken.joinToString("\n")}", broken.isEmpty())
    }

    @Test
    fun ruleIdsAreUnique() {
        val ids = ruleSet.textRules.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun everyRuleHasBothLanguageReasons() {
        val missing = ruleSet.textRules.filter { it.reasonEn.isBlank() || it.reasonHi.isBlank() }.map { it.id }
        assertTrue("Rules missing a reason: $missing", missing.isEmpty())
    }

    @Test
    fun everyLinkSignalHasReasons() {
        val missing = ruleSet.linkRules.signals.filter { it.reasonEn.isBlank() || it.reasonHi.isBlank() }.map { it.id }
        assertTrue("Signals missing a reason: $missing", missing.isEmpty())
    }
}
