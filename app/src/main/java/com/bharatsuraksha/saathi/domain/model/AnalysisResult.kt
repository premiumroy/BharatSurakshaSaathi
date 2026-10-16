package com.bharatsuraksha.saathi.domain.model

/** One plain-language reason behind the verdict. */
data class Reason(
    val id: String,
    val category: String,
    val reasonEn: String,
    val reasonHi: String,
    val weight: Int
) {
    fun text(hindi: Boolean): String = if (hindi) reasonHi else reasonEn
}

/** How sure we are about the verdict — never "certain". */
enum class Confidence { LOW, MEDIUM, HIGH }

/** The full result of checking one message. */
data class AnalysisResult(
    val score: Int,
    val verdict: Verdict,
    val confidence: Confidence,
    val reasons: List<Reason>,
    val matchedRuleIds: List<String>,
    val links: List<String>
)
