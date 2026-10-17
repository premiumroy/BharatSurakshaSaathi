package com.bharatsuraksha.saathi.domain.safety

/**
 * Hides phone numbers, UPI IDs, account numbers and links in any text the user
 * is about to share. Pure and fully unit tested. Masking is on by default so a
 * warning card never leaks the sender's own details.
 */
object Masking {

    private const val DOT = "•"

    private val url = Regex("(?:https?://|www\\.)[^\\s]+", RegexOption.IGNORE_CASE)

    /** Something@something, the shape of every UPI ID and e-mail. */
    private val upi = Regex("[A-Za-z0-9._%+-]{2,}@[A-Za-z][A-Za-z0-9.-]{1,}")

    /** Indian mobile numbers, with or without +91 and separators. */
    private val phone = Regex("(?<!\\d)(?:\\+?91[\\s-]?)?[6-9](?:[\\s-]?\\d){9}(?!\\d)")

    /** Any other long digit run: account and card numbers. */
    private val account = Regex("(?<!\\d)(?:\\d[ -]?){9,18}\\d(?!\\d)")

    fun mask(text: String): String {
        if (text.isBlank()) return text
        var s = url.replace(text) { "[" + DOT.repeat(3) + "]" }
        s = upi.replace(s) { maskUpi(it.value) }
        s = phone.replace(s) { maskPhone(it.value) }
        s = account.replace(s) { maskAccount(it.value) }
        return s
    }

    fun maskPhone(value: String): String {
        val digits = value.filter { it.isDigit() }
        if (digits.length <= 2) return DOT.repeat(digits.length)
        return DOT.repeat(digits.length - 2) + digits.takeLast(2)
    }

    fun maskAccount(value: String): String {
        val digits = value.filter { it.isDigit() }
        if (digits.length <= 4) return DOT.repeat(digits.length)
        return DOT.repeat(digits.length - 4) + digits.takeLast(4)
    }

    fun maskUpi(value: String): String {
        val at = value.indexOf('@')
        if (at <= 0) return value
        val local = value.substring(0, at)
        val handle = value.substring(at + 1)
        val shown = if (local.length <= 2) local else local.take(2)
        val handleShown = if (handle.length <= 1) handle else handle.take(1)
        return shown + DOT.repeat(3) + "@" + handleShown + DOT.repeat(3)
    }

    /** True when anything in the text would be hidden. */
    fun containsSensitive(text: String): Boolean =
        url.containsMatchIn(text) || upi.containsMatchIn(text) ||
            phone.containsMatchIn(text) || account.containsMatchIn(text)
}
