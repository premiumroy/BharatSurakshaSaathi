package com.bharatsuraksha.saathi.domain.learn

import com.bharatsuraksha.saathi.domain.model.Verdict

/**
 * "कॉल पर क्या हुआ?" triage. The five answers are scored with the same ideas
 * the scam engine uses: authority, threat, secrecy, a money or OTP request.
 * Reason keys are resource names so this stays pure and testable.
 */
object TriageScoring {

    data class Answers(
        val claimedAuthority: Boolean = false,
        val videoCall: Boolean = false,
        val askedMoneyOrOtp: Boolean = false,
        val askedSecrecy: Boolean = false,
        val createdPanic: Boolean = false
    )

    data class Outcome(
        val score: Int,
        val verdict: Verdict,
        val reasonKeys: List<String>
    )

    const val REASON_MONEY = "triage_reason_money"
    const val REASON_AUTHORITY = "triage_reason_authority"
    const val REASON_SECRECY = "triage_reason_secrecy"
    const val REASON_PANIC = "triage_reason_panic"
    const val REASON_VIDEO = "triage_reason_video"

    fun score(a: Answers): Outcome {
        var score = 0
        val keys = mutableListOf<String>()

        if (a.askedMoneyOrOtp) {
            score += 45
            keys += REASON_MONEY
        }
        if (a.claimedAuthority) {
            score += 25
            keys += REASON_AUTHORITY
        }
        if (a.askedSecrecy) {
            score += 20
            keys += REASON_SECRECY
        }
        if (a.createdPanic) {
            score += 15
            keys += REASON_PANIC
        }
        if (a.videoCall) {
            score += 15
            keys += REASON_VIDEO
        }

        // Authority plus secrecy plus pressure is the digital-arrest signature.
        val forced = a.claimedAuthority && a.askedSecrecy && (a.askedMoneyOrOtp || a.createdPanic)

        val verdict = when {
            forced || score >= 45 -> Verdict.DANGEROUS
            score >= 20 -> Verdict.CAREFUL
            else -> Verdict.SAFE
        }
        return Outcome(score.coerceAtMost(100), verdict, keys)
    }
}
