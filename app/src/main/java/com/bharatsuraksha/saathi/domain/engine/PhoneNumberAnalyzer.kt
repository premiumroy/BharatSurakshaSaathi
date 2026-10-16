package com.bharatsuraksha.saathi.domain.engine

import com.bharatsuraksha.saathi.domain.model.PhoneCheckResult
import com.bharatsuraksha.saathi.domain.model.PhoneSignal
import com.bharatsuraksha.saathi.domain.model.Verdict

/**
 * Offline heuristics for a phone number the user types in. No network, no
 * carrier lookup — just format analysis plus the user's own blocklist.
 *
 * This is a caution tool, not a "spam database": a low score only means the
 * number *looks* like a normal Indian mobile.
 */
object PhoneNumberAnalyzer {

    private const val INDIA = "91"

    /** Common international calling codes (longest match wins). */
    private val COUNTRY_CODES = setOf(
        "1", "7", "20", "27", "30", "31", "33", "34", "39", "40", "41", "43", "44", "45",
        "46", "47", "48", "49", "51", "52", "55", "56", "57", "58", "60", "61", "62", "63",
        "64", "65", "66", "81", "82", "84", "86", "90", "91", "92", "93", "94", "95", "98",
        "212", "213", "216", "218", "220", "233", "234", "249", "251", "254", "255", "256",
        "260", "263", "265", "880", "855", "856", "960", "961", "962", "963", "964", "965",
        "966", "967", "968", "971", "972", "973", "974", "975", "976", "977", "992", "993",
        "994", "995", "996", "998"
    )

    fun analyze(rawInput: String, blocklist: Set<String> = emptySet()): PhoneCheckResult {
        val trimmed = rawInput.trim()
        val digits = trimmed.filter { it.isDigit() }
        val hasPlus = trimmed.startsWith("+")
        val normalized = normalize(hasPlus, digits)

        val signals = mutableListOf<PhoneSignal>()

        // Withheld / private caller (works even when there are no digits at all)
        val lower = trimmed.lowercase()
        if (lower.contains("private") || lower.contains("unknown") || lower.contains("withheld")) {
            signals += PhoneSignal(
                "withheld", 30,
                "The caller is hidden (private/unknown number).",
                "कॉल करने वाला छिपा हुआ है (प्राइवेट/अज्ञात नंबर)।"
            )
        }

        if (digits.isEmpty()) {
            val s = signals.sumOf { it.weight }.coerceIn(0, 100)
            val v = when {
                s >= 60 -> Verdict.DANGEROUS
                s >= 25 -> Verdict.CAREFUL
                else -> Verdict.SAFE
            }
            return PhoneCheckResult(normalized, s, v, signals)
        }

        // User blocklist
        val blockDigits = blocklist.map { it.filter(Char::isDigit) }.filter { it.isNotEmpty() }
        if (blockDigits.any { digits.endsWith(it) || it.endsWith(digits) }) {
            signals += PhoneSignal(
                "blocklisted", 80,
                "This number is in your blocklist.",
                "यह नंबर आपकी ब्लॉकलिस्ट में है।"
            )
        }

        val cc = if (hasPlus) matchCountryCode(digits) else null
        val national = if (cc != null) digits.drop(cc.length) else digits

        if (cc != null && cc != INDIA) {
            signals += PhoneSignal(
                "non_india", 35,
                "This is an international number (+$cc), not an Indian one. Scam calls often come from abroad.",
                "यह एक विदेशी नंबर (+$cc) है, भारतीय नहीं। ठगी की कॉल अक्सर विदेश से आती हैं।"
            )
        }

        val isIndianContext = cc == null || cc == INDIA
        val isIndianMobile = isIndianContext && national.length == 10 && national.first() in '6'..'9'
        if (isIndianContext && !isIndianMobile) {
            if (national.length != 10) {
                signals += PhoneSignal(
                    "odd_length", 30,
                    "Indian mobile numbers have 10 digits; this one has ${national.length}.",
                    "भारतीय मोबाइल नंबर में 10 अंक होते हैं; इस नंबर में ${national.length} हैं।"
                )
            } else {
                signals += PhoneSignal(
                    "odd_format", 25,
                    "Indian mobile numbers usually start with 6, 7, 8 or 9.",
                    "भारतीय मोबाइल नंबर आमतौर पर 6, 7, 8 या 9 से शुरू होते हैं।"
                )
            }
        }

        if (isRepetitive(national)) {
            signals += PhoneSignal(
                "repeated_digits", 25,
                "The number repeats the same digits, which is unusual for a real line.",
                "नंबर में एक ही अंक बार-बार दोहराया गया है, जो असली नंबर के लिए असामान्य है।"
            )
        }
        if (isSequential(national)) {
            signals += PhoneSignal(
                "sequential_digits", 25,
                "The digits run in a simple sequence.",
                "अंक सीधे क्रम में चलते हैं।"
            )
        }
        if (national.startsWith("140")) {
            signals += PhoneSignal(
                "telemarketing", 10,
                "This looks like a telemarketing number (140 series).",
                "यह टेलीमार्केटिंग नंबर (140 श्रृंखला) लगता है।"
            )
        }
        if (!hasPlus && digits.length in 4..6) {
            signals += PhoneSignal(
                "short_code", 20,
                "This is a short code, often used for premium or promotional services.",
                "यह एक शॉर्ट कोड है, जो अक्सर प्रीमियम या प्रचार सेवाओं के लिए होता है।"
            )
        }

        val score = signals.sumOf { it.weight }.coerceIn(0, 100)
        val verdict = when {
            score >= 60 -> Verdict.DANGEROUS
            score >= 25 -> Verdict.CAREFUL
            else -> Verdict.SAFE
        }
        return PhoneCheckResult(normalized, score, verdict, signals.sortedByDescending { it.weight })
    }

    private fun matchCountryCode(digits: String): String? {
        // Prefer the longest matching known code (3, then 2, then 1).
        for (len in intArrayOf(3, 2, 1)) {
            if (digits.length > len) {
                val candidate = digits.take(len)
                if (COUNTRY_CODES.contains(candidate)) return candidate
            }
        }
        return if (digits.length > 2) digits.take(2) else null
    }

    private fun normalize(hasPlus: Boolean, digits: String): String =
        if (digits.isEmpty()) "" else if (hasPlus) "+$digits" else digits

    private fun isRepetitive(national: String): Boolean {
        if (national.length < 6) return false
        val distinct = national.toSet().size
        if (distinct <= 2) return true
        val maxRun = national.groupBy { it }.values.maxOf { it.size }
        return maxRun >= 7
    }

    private fun isSequential(national: String): Boolean {
        if (national.length < 6) return false
        val ascending = "01234567890123456789"
        val descending = "98765432109876543210"
        return ascending.contains(national) || descending.contains(national)
    }
}
