package com.bharatsuraksha.saathi.domain.learn

import kotlinx.serialization.Serializable

/** One labelled card in the Spot the Scam game. */
@Serializable
data class GameCard(
    val id: String,
    val label: String,
    val family: String = "",
    val text: String,
    val whyHi: String,
    val whyEn: String
) {
    val isScam: Boolean get() = label == "scam"
}

/** One card in "इस हफ़्ते की ठगी". */
@Serializable
data class WeeklyScam(
    val id: String,
    val titleHi: String,
    val titleEn: String,
    val howHi: String,
    val howEn: String,
    val signsHi: String,
    val signsEn: String,
    val doHi: String,
    val doEn: String
)
