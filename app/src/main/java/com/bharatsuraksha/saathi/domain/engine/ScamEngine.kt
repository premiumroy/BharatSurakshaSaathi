package com.bharatsuraksha.saathi.domain.engine

import com.bharatsuraksha.saathi.domain.model.AnalysisResult
import com.bharatsuraksha.saathi.domain.model.Banner
import com.bharatsuraksha.saathi.domain.model.ComboRule
import com.bharatsuraksha.saathi.domain.model.Confidence
import com.bharatsuraksha.saathi.domain.model.GuardRule
import com.bharatsuraksha.saathi.domain.model.Reason
import com.bharatsuraksha.saathi.domain.model.RuleSet
import com.bharatsuraksha.saathi.domain.model.Sensitivity
import com.bharatsuraksha.saathi.domain.model.TextRule
import com.bharatsuraksha.saathi.domain.model.Verdict

/**
 * The offline scam-detection engine (v2).
 *
 * Everything runs on the device: rule patterns + combinations + link analysis and
 * a transparent additive score. No network, no ML black box — every verdict can be
 * explained in plain words.
 *
 * Pipeline:
 *  1. normalise the text (see [TextNormalizer])
 *  2. match text rules
 *  3. apply guards — a guard removes rules it contradicts (e.g. "do not share OTP"
 *     is not an OTP *request*)
 *  4. analyse links (a missing link never lowers the score)
 *  5. fire combination rules over the surviving text rules AND link signals
 *  6. sum weights (cap 100) and map to a [Verdict] using [Sensitivity]-adjusted thresholds
 */
class ScamEngine(ruleSet: RuleSet) {

    private val baseThresholds = ruleSet.thresholds
    private val linkRules = ruleSet.linkRules
    private val families = ruleSet.families
    private val comboRules = ruleSet.comboRules

    /** Patterns are compiled once, at construction, for speed. */
    private val compiled: List<Pair<TextRule, List<Regex>>> = ruleSet.textRules.map { rule ->
        rule to rule.patterns.mapNotNull { pattern ->
            runCatching { Regex(pattern, RegexOption.IGNORE_CASE) }.getOrNull()
        }
    }

    private val compiledGuards: List<Pair<GuardRule, List<Regex>>> = ruleSet.guards.map { guard ->
        guard to guard.patterns.mapNotNull { pattern ->
            runCatching { Regex(pattern, RegexOption.IGNORE_CASE) }.getOrNull()
        }
    }

    fun analyze(rawInput: String, sensitivity: Sensitivity = Sensitivity.MEDIUM): AnalysisResult {
        val thresholds = sensitivity.thresholds(baseThresholds)
        val text = TextNormalizer.normalize(rawInput)

        // 1. Text rules that match.
        val fired = LinkedHashSet<String>()
        for ((rule, regexes) in compiled) {
            if (regexes.any { it.containsMatchIn(text) }) fired.add(rule.id)
        }

        // 2. Guards suppress rules they contradict.
        val suppressed = LinkedHashSet<String>()
        for ((guard, regexes) in compiledGuards) {
            if (regexes.any { it.containsMatchIn(text) }) suppressed.addAll(guard.suppress)
        }
        val active: Set<String> = fired.filterNot { it in suppressed }.toSet()

        // 3. Link analysis — links can only add risk, never remove it.
        val links = LinkAnalyzer.extractLinks(rawInput)
        val linkReasons = LinkAnalyzer.analyze(links, linkRules)
        val linkIds: Set<String> = linkReasons.map { it.id }.toSet()

        // 4. Everything that is present, for combination matching.
        val present: Set<String> = active + linkIds

        val reasons = mutableListOf<Reason>()
        val matched = LinkedHashSet<String>()
        var score = 0

        for ((rule, _) in compiled) {
            if (rule.id in active) {
                score += rule.weight
                matched.add(rule.id)
                reasons += Reason(rule.id, rule.category, rule.reasonEn, rule.reasonHi, rule.weight, rule.family)
            }
        }

        for (r in linkReasons) {
            score += r.weight
            matched.add(r.id)
            reasons += r
        }

        for (combo in comboRules) {
            if (comboMatches(combo, present)) {
                score += combo.weight
                matched.add(combo.id)
                reasons += Reason(combo.id, combo.family, combo.reasonEn, combo.reasonHi, combo.weight, combo.family)
            }
        }

        score = score.coerceIn(0, 100)

        val verdict = when {
            score >= thresholds.dangerous -> Verdict.DANGEROUS
            score >= thresholds.careful -> Verdict.CAREFUL
            else -> Verdict.SAFE
        }

        val confidence = when (verdict) {
            Verdict.SAFE -> if (score <= 8) Confidence.HIGH else Confidence.MEDIUM
            Verdict.CAREFUL -> Confidence.MEDIUM
            Verdict.DANGEROUS -> if (score >= thresholds.dangerous + 15) Confidence.HIGH else Confidence.MEDIUM
        }

        val familyIds = reasons.map { it.family }.filter { it.isNotBlank() }.distinct()

        val banners = familyIds.mapNotNull { fid ->
            families.firstOrNull { it.id == fid }
                ?.takeIf { it.bannerEn.isNotBlank() || it.bannerHi.isNotBlank() }
                ?.let { Banner(fid, it.bannerEn, it.bannerHi) }
        }

        // Top 3 reasons, strongest first. Ties broken by id for stable output.
        val topReasons = reasons
            .distinctBy { it.id }
            .sortedWith(compareByDescending<Reason> { it.weight }.thenBy { it.id })
            .take(3)

        return AnalysisResult(
            score = score,
            verdict = verdict,
            confidence = confidence,
            reasons = topReasons,
            matchedRuleIds = matched.toList(),
            links = links,
            familyIds = familyIds,
            banners = banners
        )
    }

    private fun comboMatches(combo: ComboRule, present: Set<String>): Boolean {
        if (combo.requiresAll.any { it !in present }) return false
        if (combo.requiresAny.isNotEmpty() && combo.requiresAny.none { it in present }) return false
        if (combo.requiresGroups.any { group -> group.none { it in present } }) return false
        return true
    }
}
