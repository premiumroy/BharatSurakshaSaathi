package com.bharatsuraksha.saathi.domain.agent

import com.bharatsuraksha.saathi.domain.model.Verdict

/**
 * Offline checks on an incoming call number. Pure logic, unit tested. It never
 * blocks a call by itself: it only decides whether to show a quiet heads-up.
 */
object NumberHeuristics {

    data class Finding(val ruleId: String, val weight: Int, val reasonKey: String)

    data class Analysis(
        val score: Int,
        val verdict: Verdict,
        val findings: List<Finding>,
        val knownTrustedBank: Boolean
    )

    const val RULE_BLOCKED = "num_blocked"
    const val RULE_INTL = "num_intl_prefix"
    const val RULE_ROBO = "num_robo_pattern"
    const val RULE_NOT_IN_CONTACTS = "num_not_in_contacts"
    const val RULE_TRUSTED_BANK = "num_trusted_bank"
    const val RULE_BANK_LOOKALIKE = "num_bank_lookalike"
    const val RULE_PRIVATE = "num_private"

    const val REASON_BLOCKED = "agent_reason_blocked"
    const val REASON_INTL = "agent_reason_intl"
    const val REASON_ROBO = "agent_reason_robo"
    const val REASON_NOT_IN_CONTACTS = "agent_reason_not_in_contacts"
    const val REASON_TRUSTED_BANK = "agent_reason_trusted_bank"
    const val REASON_BANK_LOOKALIKE = "agent_reason_bank_lookalike"
    const val REASON_PRIVATE = "agent_reason_private"

    /** Country codes that carry a lot of scam traffic into India. */
    val HIGH_RISK_PREFIXES = listOf(
        "+92", "+880", "+234", "+254", "+255", "+375", "+62", "+84", "+27", "+263", "+251"
    )

    /** Short codes banks and telemarketers use. */
    private val serviceCode = Regex("^(1800|1860|140|1600)\\d*$")

    fun analyze(
        rawNumber: String?,
        isInContacts: Boolean,
        trustedBankNumbers: Set<String> = emptySet(),
        blocklist: Set<String> = emptySet()
    ): Analysis {
        var score = 0
        val findings = mutableListOf<Finding>()

        val number = rawNumber?.trim().orEmpty()
        val digits = number.filter { it.isDigit() }

        if (number.isBlank() || number.equals("private", true) || digits.length < 4) {
            score += 10
            findings += Finding(RULE_PRIVATE, 10, REASON_PRIVATE)
            return Analysis(score, Verdict.SAFE, findings, knownTrustedBank = false)
        }

        val normalised = normalise(number)

        if (blocklist.any { normalise(it) == normalised }) {
            score += 60
            findings += Finding(RULE_BLOCKED, 60, REASON_BLOCKED)
        }

        val trusted = trustedBankNumbers.any { normalise(it) == normalised }
        if (trusted) {
            score -= 40
            findings += Finding(RULE_TRUSTED_BANK, -40, REASON_TRUSTED_BANK)
        } else if (serviceCode.matches(digits)) {
            // Looks like a bank or telemarketer short code, but it is not one the
            // user saved. Nudge them to check, do not accuse.
            score += 15
            findings += Finding(RULE_BANK_LOOKALIKE, 15, REASON_BANK_LOOKALIKE)
        }

        // A saved contact is trusted: prefix and pattern checks do not apply.
        if (!trusted && !isInContacts && HIGH_RISK_PREFIXES.any { number.startsWith(it) }) {
            score += 25
            findings += Finding(RULE_INTL, 25, REASON_INTL)
        }

        if (!trusted && !isInContacts && isRoboPattern(digits)) {
            score += 20
            findings += Finding(RULE_ROBO, 20, REASON_ROBO)
        }

        if (!trusted && !isInContacts) {
            score += 10
            findings += Finding(RULE_NOT_IN_CONTACTS, 10, REASON_NOT_IN_CONTACTS)
        }

        val verdict = when {
            score >= 45 -> Verdict.DANGEROUS
            score >= 15 -> Verdict.CAREFUL
            else -> Verdict.SAFE
        }
        return Analysis(score.coerceAtMost(100), verdict, findings, trusted)
    }

    /** Repeated digits, or long runs of the same digit: typical of robocalls. */
    fun isRoboPattern(digits: String): Boolean {
        if (digits.length < 6) return false
        var run = 1
        for (i in 1 until digits.length) {
            run = if (digits[i] == digits[i - 1]) run + 1 else 1
            if (run >= 6) return true
        }
        // 123456 / 654321 style runs
        var ascending = 1
        var descending = 1
        for (i in 1 until digits.length) {
            val diff = digits[i] - digits[i - 1]
            ascending = if (diff == 1) ascending + 1 else 1
            descending = if (diff == -1) descending + 1 else 1
            if (ascending >= 6 || descending >= 6) return true
        }
        return false
    }

    fun normalise(number: String): String = number.filter { it.isDigit() }.takeLast(10)
}
