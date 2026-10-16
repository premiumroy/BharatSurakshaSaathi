package com.bharatsuraksha.saathi.domain.model

import kotlinx.serialization.Serializable

/**
 * The whole rule set, loaded from app/src/main/assets/scam_rules.json.
 * Rules can be updated by replacing that asset — no code change needed.
 */
@Serializable
data class RuleSet(
    val version: Int = 1,
    val updated: String = "",
    val thresholds: Thresholds = Thresholds(),
    val textRules: List<TextRule> = emptyList(),
    val linkRules: LinkRules = LinkRules()
)

@Serializable
data class Thresholds(
    /** Score at which we move from SAFE to CAREFUL. */
    val careful: Int = 25,
    /** Score at which we move from CAREFUL to DANGEROUS. */
    val dangerous: Int = 60
)

/**
 * One detection rule. [patterns] are Java/Kotlin regular expressions, matched
 * case-insensitively against the normalized message text.
 */
@Serializable
data class TextRule(
    val id: String,
    val category: String = "",
    val weight: Int,
    val patterns: List<String> = emptyList(),
    val reasonEn: String = "",
    val reasonHi: String = ""
)

@Serializable
data class LinkRules(
    val shorteners: List<String> = emptyList(),
    val suspiciousTlds: List<String> = emptyList(),
    val brandTokens: List<String> = emptyList(),
    val officialDomains: List<String> = emptyList(),
    val signals: List<LinkSignal> = emptyList()
) {
    fun signal(id: String): LinkSignal? = signals.firstOrNull { it.id == id }
}

@Serializable
data class LinkSignal(
    val id: String,
    val weight: Int,
    val reasonEn: String = "",
    val reasonHi: String = ""
)

/** Bundled list of genuine bank / government / payment domains (assets/official_domains.json). */
@Serializable
data class OfficialDomainsFile(
    val version: Int = 1,
    val updated: String = "",
    val domains: List<String> = emptyList()
)
