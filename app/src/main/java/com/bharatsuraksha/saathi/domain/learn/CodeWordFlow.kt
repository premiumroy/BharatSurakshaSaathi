package com.bharatsuraksha.saathi.domain.learn

/**
 * Family code word and the "someone is asking for money in an emergency" flow.
 * Pure logic, unit tested. The code word itself is stored encrypted elsewhere
 * and is never sent anywhere.
 */
object CodeWordFlow {

    data class Situation(
        val hasCodeWord: Boolean = false,
        val codeWordMatches: Boolean = false,
        val asksMoney: Boolean = false,
        val urgent: Boolean = false,
        val fromNewNumber: Boolean = false,
        val onVideoCall: Boolean = false
    )

    enum class Level { LIKELY_REAL, VERIFY_FIRST, LIKELY_FRAUD }

    data class Advice(val level: Level, val reasonKeys: List<String>)

    const val REASON_NEW_NUMBER = "codeword_reason_new_number"
    const val REASON_MONEY = "codeword_reason_money"
    const val REASON_URGENT = "codeword_reason_urgent"
    const val REASON_VIDEO = "codeword_reason_video"
    const val REASON_MATCHED = "codeword_reason_matched"

    fun assess(s: Situation): Advice {
        var risk = 0
        val keys = mutableListOf<String>()

        if (s.fromNewNumber) {
            risk += 2
            keys += REASON_NEW_NUMBER
        }
        if (s.asksMoney) {
            risk += 3
            keys += REASON_MONEY
        }
        if (s.urgent) {
            risk += 1
            keys += REASON_URGENT
        }
        if (s.onVideoCall) {
            risk += 1
            keys += REASON_VIDEO
        }
        if (s.hasCodeWord && s.codeWordMatches) {
            risk -= 3
            keys += REASON_MATCHED
        }

        val level = when {
            risk >= 4 -> Level.LIKELY_FRAUD
            risk >= 2 -> Level.VERIFY_FIRST
            else -> Level.LIKELY_REAL
        }
        return Advice(level, keys)
    }

    /** Three questions to answer before sending any money. */
    val CHECKLIST_KEYS = listOf("codeword_check_1", "codeword_check_2", "codeword_check_3")

    /** Warning signs of a voice-clone or deepfake call. */
    val VOICE_CLONE_KEYS = listOf("codeword_clone_1", "codeword_clone_2", "codeword_clone_3")

    /** A code word is useful only if it is not guessable. */
    fun isCodeWordAcceptable(word: String): Boolean {
        val w = word.trim()
        if (w.length < 4) return false
        if (w.all { it.isDigit() }) return false
        val weak = setOf("password", "1234", "0000", "abcd", "test", "otp", "bank")
        return w.lowercase() !in weak
    }
}
