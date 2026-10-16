package com.bharatsuraksha.saathi.domain.model

/**
 * How strict detection is. Lower thresholds = more sensitive (flags more).
 * Kids Safe Mode uses [HIGH].
 */
enum class Sensitivity {
    LOW,
    MEDIUM,
    HIGH;

    /** Adjusts the base thresholds from the rule set. */
    fun thresholds(base: Thresholds): Thresholds = when (this) {
        LOW -> Thresholds(
            careful = base.careful + 10,
            dangerous = base.dangerous + 10
        )
        MEDIUM -> base
        HIGH -> Thresholds(
            careful = (base.careful - 10).coerceAtLeast(5),
            dangerous = (base.dangerous - 15).coerceAtLeast(base.careful - 5).coerceAtLeast(10)
        )
    }
}
