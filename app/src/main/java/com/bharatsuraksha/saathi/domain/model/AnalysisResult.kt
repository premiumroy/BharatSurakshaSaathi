package com.bharatsuraksha.saathi.domain.model

/** One plain-language reason behind the verdict. */
data class Reason(
    val id: String,
    val category: String,
    val reasonEn: String,
    val reasonHi: String,
    val weight: Int,
    /** Which scam family this reason belongs to ("" when not family-specific). */
    val family: String = ""
) {
    fun text(hindi: Boolean): String = if (hindi) reasonHi else reasonEn
}

/** How sure we are about the verdict — never "certain". */
enum class Confidence { LOW, MEDIUM, HIGH }

/** A fixed warning tied to a scam family (e.g. the digital-arrest banner). */
data class Banner(
    val familyId: String,
    val textEn: String,
    val textHi: String
) {
    fun text(hindi: Boolean): String = if (hindi) textHi else textEn
}

/** The full result of checking one message. */
data class AnalysisResult(
    val score: Int,
    val verdict: Verdict,
    val confidence: Confidence,
    val reasons: List<Reason>,
    val matchedRuleIds: List<String>,
    val links: List<String>,
    /** Scam families the engine believes are involved, strongest first. */
    val familyIds: List<String> = emptyList(),
    /** Fixed warnings to show, one per family that declares one. */
    val banners: List<Banner> = emptyList()
)
