package com.bharatsuraksha.saathi.domain.engine

import com.bharatsuraksha.saathi.domain.model.LinkRules
import com.bharatsuraksha.saathi.domain.model.Reason

/**
 * Finds links in a message and flags risky ones: shortened links, look-alike
 * bank/UPI domains, raw IP addresses, suspicious TLDs, insecure http and
 * messages stuffed with many links.
 *
 * Pure Kotlin — no Android, no network.
 */
object LinkAnalyzer {

    private val urlRegex = Regex(
        "(?:https?://|www\\.)[^\\s<>\"')\\]]+",
        RegexOption.IGNORE_CASE
    )

    private val bareDomainRegex = Regex(
        "\\b(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+" +
            "(?:com|in|net|org|co|io|xyz|top|club|online|site|info|tk|ml|ga|cf|gq|buzz|icu|" +
            "link|live|work|click|download|review|loan|cyou|monster|quest|rest|cam|sbs|best|" +
            "kim|bid|win|date|faith|party|trade|webcam|stream|men|gdn|lol|wow|uno|pw|cc|su|ru|" +
            "shop|app|dev|me)\\b",
        RegexOption.IGNORE_CASE
    )

    /** Host of a link written without a scheme but followed by a path, e.g. bit.ly/xyz. */
    private val pathDomainRegex = Regex(
        "\\b[a-z0-9][a-z0-9-]{1,63}(?:\\.[a-z0-9-]{1,63})+(?=/)",
        RegexOption.IGNORE_CASE
    )

    private val trailing = charArrayOf('.', ',', ';', ')', ']', '}', '"', '\'', '!', '?')

    /** All distinct link-ish strings in the message, in order of appearance. */
    fun extractLinks(raw: String): List<String> {
        val found = LinkedHashSet<String>()
        urlRegex.findAll(raw).forEach { m ->
            found.add(m.value.trimEnd(*trailing))
        }
        bareDomainRegex.findAll(raw).forEach { m ->
            found.add(m.value.trimEnd(*trailing))
        }
        pathDomainRegex.findAll(raw).forEach { m ->
            found.add(m.value.trimEnd(*trailing))
        }
        return found.filter { it.isNotBlank() }
    }

    fun analyze(links: List<String>, rules: LinkRules): List<Reason> {
        if (links.isEmpty()) return emptyList()

        val reasons = mutableListOf<Reason>()
        val seen = mutableSetOf<String>()

        fun add(signalId: String) {
            val sig = rules.signal(signalId) ?: return
            if (seen.add(signalId)) {
                reasons.add(Reason("link_$signalId", "link", sig.reasonEn, sig.reasonHi, sig.weight))
            }
        }

        val hosts = links.map { hostOf(it) }.filter { it.isNotEmpty() }.toSet()

        for (link in links) {
            val host = hostOf(link)
            if (host.isEmpty()) continue
            if (isIpAddress(host)) add("ip_address")
            if (rules.shorteners.any { host == it || host.endsWith(".$it") }) add("shortener")
            val tld = host.substringAfterLast('.', "")
            if (tld.isNotEmpty() && rules.suspiciousTlds.contains(tld)) add("suspicious_tld")
            if (isLookalikeBrand(host, rules)) add("lookalike_brand")
            if (schemeOf(link) == "http") add("insecure_http")
        }

        if (hosts.size >= 3) add("many_links")
        return reasons
    }

    /** Lower-cased host part of a link (no scheme, path, port or userinfo). */
    fun hostOf(link: String): String {
        var s = link.trim()
        s = s.removePrefix("https://").removePrefix("http://")
        s = s.removePrefix("HTTPS://").removePrefix("HTTP://")
        s = s.substringBefore('/').substringBefore('?').substringBefore('#')
        s = s.substringAfter('@')       // strip userinfo
        s = s.substringBefore(':')      // strip port
        return s.lowercase()
    }

    private fun schemeOf(link: String): String = when {
        link.startsWith("https://", ignoreCase = true) -> "https"
        link.startsWith("http://", ignoreCase = true) -> "http"
        else -> ""
    }

    private fun isIpAddress(host: String): Boolean =
        Regex("^\\d{1,3}(\\.\\d{1,3}){3}$").matches(host)

    /**
     * True when the host name mentions a bank/UPI/government brand but is NOT one
     * of that brand's official domains — e.g. "sbi-kyc-update.xyz" or
     * "hdfcbank.verify-login.com".
     */
    private fun isLookalikeBrand(host: String, rules: LinkRules): Boolean {
        val hasBrand = rules.brandTokens.any { host.contains(it) }
        if (!hasBrand) return false
        val isOfficial = rules.officialDomains.any { host == it || host.endsWith(".$it") }
        return !isOfficial
    }
}
