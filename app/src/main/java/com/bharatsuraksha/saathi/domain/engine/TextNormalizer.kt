package com.bharatsuraksha.saathi.domain.engine

import java.util.Locale

/**
 * Prepares raw text for rule matching. Pure and side-effect free so it is easy to test.
 *
 * - lowercases (Latin) so patterns can be written once
 * - removes zero-width / direction-control characters used to hide words
 * - normalizes smart quotes, unicode dashes and non-breaking spaces
 * - collapses runs of whitespace
 *
 * Devanagari is left untouched (lowercasing has no effect on it).
 */
object TextNormalizer {

    private val whitespace = Regex("\\s+")
    private val invisible = Regex("[\\u200B-\\u200F\\u202A-\\u202E\\u2060\\uFEFF]")

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
        s = s.lowercase(Locale.ROOT)
        s = whitespace.replace(s, " ")
        return s.trim()
    }
}
