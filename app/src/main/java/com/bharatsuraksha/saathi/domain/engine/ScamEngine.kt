package com.bharatsuraksha.saathi.domain.engine

import com.bharatsuraksha.saathi.domain.model.AnalysisResult
import com.bharatsuraksha.saathi.domain.model.Confidence
import com.bharatsuraksha.saathi.domain.model.Reason
import com.bharatsuraksha.saathi.domain.model.RuleSet
import com.bharatsuraksha.saathi.domain.model.Sensitivity
import com.bharatsuraksha.saathi.domain.model.TextRule
import com.bharatsuraksha.saathi.domain.model.Verdict

/**
 * The offline scam-detection engine.
 *
 * Everything runs on the device: rule patterns + link analysis + a simple,
 * transparent additive score. No network, no ML black box — so every verdict
 * can be explained in plain words.
 *
 * Score is the sum of the weights of every rule that matched, capped at 100,
 * then mapped to a [Verdict] using thresholds adjusted by [Sensitivity].
 */
class ScamEngine(ruleSet: RuleSet) {

    private val baseThresholds = ruleSet.thresholds
    private val linkRules = ruleSet.linkRules

    /** Patterns are compiled once, at construction, for speed. */
    private val compiled: List<Pair<TextRule, List<Regex>>> = ruleSet.textRules.map { rule ->
        rule to rule.patterns.mapNotNull { pattern ->
            runCatching { Regex(pattern, RegexOption.IGNORE_CASE) }.getOrNull()
        }
    }

    fun analyze(rawInput: String, sensitivity: Sensitivity = Sensitivity.MEDIUM): AnalysisResult {
        val thresholds = sensitivity.thresholds(baseThresholds)
        val text = TextNormalizer.normalize(rawInput)

        val reasons = mutableListOf<Reason>()
        val matched = mutableListOf<String>()
        var score = 0

        // 1. Text rules.
        for ((rule, regexes) in compiled) {
            if (regexes.any { it.containsMatchIn(text) }) {
                score += rule.weight
                matched += rule.id
                reasons += Reason(rule.id, rule.category, rule.reasonEn, rule.reasonHi, rule.weight)
            }
        }

        // 2. Link analysis.
        val links = LinkAnalyzer.extractLinks(rawInput)
        for (r in LinkAnalyzer.analyze(links, linkRules)) {
            score += r.weight
            reasons += r
            matched += r.id
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
            matchedRuleIds = matched,
            links = links
        )
    }
}
