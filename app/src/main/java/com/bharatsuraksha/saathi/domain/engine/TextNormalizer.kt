package com.bharatsuraksha.saathi.domain.engine

import java.util.Locale

/**
 * Prepares raw text for rule matching. Pure and side-effect free so it is easy to test.
 *
 * v2 normalisation, in order:
 *  - removes zero-width / direction-control characters used to hide words
 *  - normalises smart quotes, unicode dashes and non-breaking spaces
 *  - collapses UPPERCASE letters split by spaces/dots/dashes ("K Y C", "K.Y.C", "O-T-P")
 *  - lowercases (Latin) and folds look-alike Unicode (Cyrillic / Greek / full-width Latin)
 *  - folds Devanagari digits to ASCII and normalises nukta / chandrabindu
 *  - collapses dotted/hyphenated lowercase runs ("k.y.c", "s-b-i") and Devanagari ("के.वाई.सी")
 *  - canonicalises a few Roman-Hindi spelling variants (batao/bataiye/btao -> bata)
 *  - collapses runs of whitespace
 *
 * Devanagari itself is preserved (lowercasing has no effect on it).
 */
object TextNormalizer {

    private val whitespace = Regex("\\s+")
    private val invisible = Regex("[\\u200B-\\u200F\\u202A-\\u202E\\u2060\\uFEFF]")

    /** UPPERCASE letters split apart by spaces/dots/dashes: "K Y C", "K.Y.C", "O-T-P". */
    private val spacedUpper = Regex("(?<![A-Za-z0-9])(?:[A-Z][\\s._\\-]+){2,}[A-Z](?![A-Za-z0-9])")

    /** Lowercase letters split by dots/dashes only: "k.y.c", "s-b-i". */
    private val dottedLower = Regex("(?<![a-z0-9])(?:[a-z][._\\-]+){2,}[a-z](?![a-z0-9])")

    /** Devanagari letters split by a dot/dash: "के.वाई.सी". */
    private val dottedDevanagari = Regex("([\\u0900-\\u097F])[.\\-](?=[\\u0900-\\u097F])")

    /** Common look-alike characters used to smuggle words past filters. */
    private val lookalike: Map<Char, Char> = buildMap {
        // Cyrillic
        put('\u0430', 'a'); put('\u0432', 'b'); put('\u0435', 'e'); put('\u0455', 's')
        put('\u0456', 'i'); put('\u0458', 'j'); put('\u043e', 'o'); put('\u0440', 'p')
        put('\u0441', 'c'); put('\u0443', 'y'); put('\u0445', 'x'); put('\u04c6', 'd')
        put('\u04bb', 'h'); put('\u04cf', 'l'); put('\u043a', 'k'); put('\u043c', 'm')
        put('\u043d', 'h'); put('\u0442', 't'); put('\u0433', 'r'); put('\u0437', '3')
        // Greek
        put('\u03b1', 'a'); put('\u03b2', 'b'); put('\u03b5', 'e'); put('\u03b9', 'i')
        put('\u03ba', 'k'); put('\u03bc', 'm'); put('\u03bd', 'v'); put('\u03bf', 'o')
        put('\u03c1', 'p'); put('\u03c4', 't'); put('\u03c5', 'u'); put('\u03c7', 'x')
        put('\u03b3', 'y')
    }

    /** Roman-Hindi spelling variants -> one canonical spelling. */
    private val romanHindi: List<Pair<Regex, String>> = listOf(
        Regex("\\b(batao|bataiye|bataao|bataye|btao|bta|bata\\s*do|bata\\s*dijiye|bata\\s*dena)\\b") to "bata",
        Regex("\\b(bhejo|bhejiye|bhejein|bhej\\s*do|bhejdo|bhejna|bhej\\s*dein|bhej\\s*dijiye)\\b") to "bhej",
        Regex("\\b(turant|turnt|turanth|foran)\\b") to "turant",
        Regex("\\b(jaldi|jldi|jaldi\\s*se)\\b") to "jaldi",
        Regex("\\b(paise|paisa|paisaa|paisey|rakam)\\b") to "paisa"
    )

    fun normalize(input: String): String {
        if (input.isEmpty()) return ""
        var s = invisible.replace(input, "")
        s = s.replace('\u2013', '-')   // en dash
            .replace('\u2014', '-')    // em dash
            .replace('\u2018', '\'')   // '
            .replace('\u2019', '\'')   // '
            .replace('\u201C', '"')    // "
            .replace('\u201D', '"')    // "
            .replace('\u00A0', ' ')    // nbsp
        s = collapseSpacedUpper(s)
        s = s.lowercase(Locale.ROOT)
        s = foldLookalikes(s)
        s = foldDevanagari(s)
        s = foldDigits(s)
        s = collapseDottedLower(s)
        s = dottedDevanagari.replace(s, "$1")
        for ((re, replacement) in romanHindi) s = re.replace(s, replacement)
        s = whitespace.replace(s, " ")
        return s.trim()
    }

    private fun collapseSpacedUpper(s: String): String =
        spacedUpper.replace(s) { m -> m.value.filter { it.isLetterOrDigit() } }

    private fun collapseDottedLower(s: String): String =
        dottedLower.replace(s) { m -> m.value.filter { it.isLetterOrDigit() } }

    private fun foldLookalikes(s: String): String {
        val sb = StringBuilder(s.length)
        for (ch in s) {
            val mapped = when {
                ch.code in 0xFF01..0xFF5E -> (ch.code - 0xFEE0).toChar()   // full-width
                ch == '\u3000' -> ' '
                else -> ch
            }
            sb.append(lookalike[mapped] ?: mapped)
        }
        return sb.toString()
    }

    /** Drop nukta and unify chandrabindu with anusvara. */
    private fun foldDevanagari(s: String): String {
        val sb = StringBuilder(s.length)
        for (ch in s) {
            when {
                ch == '\u093C' -> {}                                   // nukta
                ch == '\u0901' -> sb.append('\u0902')                  // chandrabindu -> anusvara
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    private fun foldDigits(s: String): String {
        val sb = StringBuilder(s.length)
        for (ch in s) {
            sb.append(if (ch.code in 0x0966..0x096F) ('0' + (ch.code - 0x0966)) else ch)
        }
        return sb.toString()
    }
}
