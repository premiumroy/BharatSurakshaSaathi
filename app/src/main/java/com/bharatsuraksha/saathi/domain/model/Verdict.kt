package com.bharatsuraksha.saathi.domain.model

/**
 * Overall verdict shown to the user. We never claim 100% accuracy — see [Confidence].
 */
enum class Verdict {
    SAFE,
    CAREFUL,
    DANGEROUS
}
