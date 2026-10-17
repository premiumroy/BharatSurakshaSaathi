package com.bharatsuraksha.saathi.domain.model

import kotlinx.serialization.Serializable

/**
 * The whole rule set, loaded from app/src/main/assets/scam_rules.json.
 * Rules can be updated by replacing that asset — no code change needed.
 *
 * v2 adds [families], [comboRules] and [guards] so that detection can name a
 * scam family, reward signal *combinations* (which matter far more than single
 * words) and suppress false positives in genuine messages.
 */
@Serializable
data class RuleSet(
    val version: Int = 1,
    val updated: String = "",
    val changelog: List<ChangelogEntry> = emptyList(),
    val thresholds: Thresholds = Thresholds(),
    val families: List<Family> = emptyList(),
    val textRules: List<TextRule> = emptyList(),
    val comboRules: List<ComboRule> = emptyList(),
    val guards: List<GuardRule> = emptyList(),
    val linkRules: LinkRules = LinkRules()
) {
    fun family(id: String): Family? = families.firstOrNull { it.id == id }
}

/** One line of the rule-file changelog. */
@Serializable
data class ChangelogEntry(
    val version: Int = 0,
    val date: String = "",
    val note: String = ""
)

/**
 * A scam family (digital arrest, investment, task/job …). Families let the engine
 * name what it thinks the message is and give family-specific advice and banners.
 */
@Serializable
data class Family(
    val id: String,
    val nameEn: String = "",
    val nameHi: String = "",
    /** A fixed warning shown at the top of the result, if this family needs one. */
    val bannerEn: String = "",
    val bannerHi: String = "",
    /** Advice string-resource keys specific to this family. */
    val adviceKeys: List<String> = emptyList()
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
 *
 * v2 adds [family], [language] and [examples] so the rules file is self-documenting
 * and can be audited/updated without touching code.
 */
@Serializable
data class TextRule(
    val id: String,
    val family: String = "",
    val category: String = "",
    val weight: Int,
    /** "en", "hi", "hinglish" or "any" — documentation only. */
    val language: String = "any",
    val patterns: List<String> = emptyList(),
    val examples: List<String> = emptyList(),
    val reasonEn: String = "",
    val reasonHi: String = ""
)

/**
 * A rule that fires only when several other rules fire together. Combinations are
 * far stronger evidence than single words — e.g. authority impersonation + threat
 * + secrecy is a classic "digital arrest".
 *
 * [requiresAll] — every id must be present.
 * [requiresAny] — at least one id must be present (ignored when empty).
 * [requiresGroups] — every inner group must contribute at least one id.
 */
@Serializable
data class ComboRule(
    val id: String,
    val family: String = "",
    val weight: Int,
    val requiresAll: List<String> = emptyList(),
    val requiresAny: List<String> = emptyList(),
    val requiresGroups: List<List<String>> = emptyList(),
    val reasonEn: String = "",
    val reasonHi: String = ""
)

/**
 * A false-positive guard. When any of [patterns] matches, every rule id in
 * [suppress] is removed. Used so that, for example, a message saying
 * "do not share OTP" is never treated as *asking* for an OTP.
 */
@Serializable
data class GuardRule(
    val id: String,
    val family: String = "",
    val suppress: List<String> = emptyList(),
    val patterns: List<String> = emptyList()
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
