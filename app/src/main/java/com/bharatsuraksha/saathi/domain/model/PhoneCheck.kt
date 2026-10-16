package com.bharatsuraksha.saathi.domain.model

/** One reason behind a phone-number risk score. */
data class PhoneSignal(
    val id: String,
    val weight: Int,
    val reasonEn: String,
    val reasonHi: String
) {
    fun text(hindi: Boolean): String = if (hindi) reasonHi else reasonEn
}

data class PhoneCheckResult(
    val normalized: String,
    val score: Int,
    val verdict: Verdict,
    val signals: List<PhoneSignal>
)
