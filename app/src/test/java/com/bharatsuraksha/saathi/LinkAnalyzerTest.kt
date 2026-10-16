package com.bharatsuraksha.saathi

import com.bharatsuraksha.saathi.domain.engine.LinkAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LinkAnalyzerTest {

    private val rules = TestRules.ruleSet.linkRules

    @Test
    fun extractsHttpAndBareDomains() {
        val links = LinkAnalyzer.extractLinks("Pay now at http://sbi-kyc.xyz or visit www.hdfc-verify.com today")
        assertTrue(links.any { it.contains("sbi-kyc.xyz") })
        assertTrue(links.any { it.contains("hdfc-verify.com") })
    }

    @Test
    fun hostOfStripsSchemePathAndPort() {
        assertEquals("www.sbi.co.in", LinkAnalyzer.hostOf("https://www.sbi.co.in/login?x=1"))
        assertEquals("example.com", LinkAnalyzer.hostOf("http://example.com:8080/a/b"))
    }

    @Test
    fun officialBankDomainIsNotLookalike() {
        val reasons = LinkAnalyzer.analyze(listOf("https://www.sbi.co.in/personal"), rules)
        assertTrue(reasons.none { it.id == "link_lookalike_brand" })
    }

    @Test
    fun lookalikeBankDomainIsFlagged() {
        val reasons = LinkAnalyzer.analyze(listOf("http://sbi-kyc-update.xyz/verify"), rules)
        val ids = reasons.map { it.id }
        assertTrue(ids.contains("link_lookalike_brand"))
        assertTrue(ids.contains("link_suspicious_tld"))
        assertTrue(ids.contains("link_insecure_http"))
    }

    @Test
    fun shortenedLinkIsFlagged() {
        val reasons = LinkAnalyzer.analyze(listOf("https://bit.ly/3xYz"), rules)
        assertTrue(reasons.any { it.id == "link_shortener" })
    }

    @Test
    fun rawIpAddressIsFlagged() {
        val reasons = LinkAnalyzer.analyze(listOf("http://192.168.0.10/login"), rules)
        assertTrue(reasons.any { it.id == "link_ip_address" })
    }

    @Test
    fun manyLinksAreFlagged() {
        val reasons = LinkAnalyzer.analyze(
            listOf("http://a.com/x", "http://b.com/y", "http://c.com/z"),
            rules
        )
        assertTrue(reasons.any { it.id == "link_many_links" })
    }

    @Test
    fun cleanMessageHasNoLinkReasons() {
        val reasons = LinkAnalyzer.analyze(LinkAnalyzer.extractLinks("Are we meeting at 5pm?"), rules)
        assertTrue(reasons.isEmpty())
    }
}
