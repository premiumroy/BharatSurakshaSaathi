package com.bharatsuraksha.saathi

import com.bharatsuraksha.saathi.domain.engine.LinkAnalyzer
import org.junit.Assert.assertTrue
import org.junit.Test

class OfficialDomainsTest {

    private val rules = TestRules.ruleSet.linkRules

    @Test
    fun bundledListIsLoaded() {
        assertTrue("official domains: ${rules.officialDomains.size}", rules.officialDomains.size >= 60)
    }

    @Test
    fun realBankDomainsAreNotLookalikes() {
        listOf(
            "https://www.hdfcbank.com/personal",
            "https://onlinesbi.sbi/",
            "https://www.icicibank.com/",
            "https://www.irctc.co.in/nget/train-search",
            "https://www.incometax.gov.in/iec/foportal/"
        ).forEach { url ->
            val reasons = LinkAnalyzer.analyze(listOf(url), rules)
            assertTrue("$url wrongly flagged", reasons.none { it.id == "link_lookalike_brand" })
        }
    }

    @Test
    fun lookalikesAreStillFlagged() {
        listOf(
            "http://hdfcbank.verify-login.com",
            "http://sbi-kyc-update.xyz",
            "https://paytm-offers.online/win"
        ).forEach { url ->
            val reasons = LinkAnalyzer.analyze(listOf(url), rules)
            assertTrue("$url not flagged", reasons.any { it.id == "link_lookalike_brand" })
        }
    }
}
