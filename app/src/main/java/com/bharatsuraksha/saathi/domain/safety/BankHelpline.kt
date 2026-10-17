package com.bharatsuraksha.saathi.domain.safety

/**
 * Trusted bank helplines that the USER saved, read from the back of their own
 * card or passbook. We deliberately ship no bundled list of "official" numbers,
 * because a wrong number is worse than none. Pure logic, unit tested.
 */
object BankHelpline {

    data class Entry(val bank: String, val number: String)

    const val REASON_BANK = "helpline_reason_bank"
    const val REASON_NUMBER = "helpline_reason_number"

    /** Returns null when the entry is fine, otherwise a reason key. */
    fun validate(entry: Entry): String? {
        if (entry.bank.isBlank()) return REASON_BANK
        val digits = entry.number.filter { it.isDigit() }
        if (digits.length < 3 || digits.length > 15) return REASON_NUMBER
        return null
    }

    fun normalise(number: String): String = number.filter { it.isDigit() || it == '+' }
}
