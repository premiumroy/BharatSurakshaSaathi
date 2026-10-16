package com.bharatsuraksha.saathi

import com.bharatsuraksha.saathi.domain.model.Sensitivity
import com.bharatsuraksha.saathi.domain.model.Verdict
import org.junit.Assert.assertTrue
import org.junit.Test

class SensitivityTest {

    private val engine = TestRules.engine
    private val base = TestRules.ruleSet.thresholds

    @Test
    fun thresholdsAreOrderedPerSensitivity() {
        val low = Sensitivity.LOW.thresholds(base)
        val med = Sensitivity.MEDIUM.thresholds(base)
        val high = Sensitivity.HIGH.thresholds(base)
        assertTrue(low.careful > med.careful)
        assertTrue(high.careful < med.careful)
        assertTrue(high.dangerous < med.dangerous)
        assertTrue(low.dangerous > med.dangerous)
        // careful must stay below dangerous for every level
        listOf(low, med, high).forEach { assertTrue(it.careful < it.dangerous) }
    }

    @Test
    fun highSensitivityIsAtLeastAsStrictAsLow() {
        // A borderline message: flagged more readily at HIGH than at LOW.
        val text = "Please send your Aadhaar photo for verification."
        val low = engine.analyze(text, Sensitivity.LOW).verdict
        val high = engine.analyze(text, Sensitivity.HIGH).verdict
        assertTrue(high.ordinal >= low.ordinal)
    }

    @Test
    fun kidsModeStyleHighSensitivityFlagsMore() {
        val text = "Your account will be suspended. Update KYC now."
        val med = engine.analyze(text, Sensitivity.MEDIUM)
        val high = engine.analyze(text, Sensitivity.HIGH)
        assertTrue(high.verdict.ordinal >= med.verdict.ordinal)
        assertTrue(high.verdict != Verdict.SAFE)
    }

    @Test
    fun defaultSensitivityIsMedium() {
        val text = "Your KYC is expiring. Update at http://sbi-kyc.xyz"
        assertTrue(engine.analyze(text) == engine.analyze(text, Sensitivity.MEDIUM))
    }
}
