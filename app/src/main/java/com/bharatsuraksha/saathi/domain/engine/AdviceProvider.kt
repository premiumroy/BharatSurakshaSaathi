package com.bharatsuraksha.saathi.domain.engine

import com.bharatsuraksha.saathi.domain.model.AnalysisResult
import com.bharatsuraksha.saathi.domain.model.Verdict

/**
 * Turns a result into plain "what to do now" steps.
 *
 * Returns string-resource KEYS (not localized text) so the UI can render them
 * in the user's chosen language. Keeping this in the domain layer means it is
 * unit-testable too.
 */
object AdviceProvider {

    fun adviceKeys(result: AnalysisResult): List<String> {
        val ids = result.matchedRuleIds.toSet()
        fun has(vararg keys: String) = keys.any { ids.contains(it) }

        // A safe message still gets gentle, universal advice.
        if (result.verdict == Verdict.SAFE) {
            return listOf("advice_dont_share_otp", "advice_verify_official")
        }

        val out = LinkedHashSet<String>()

        if (has("otp_request", "share_documents")) out += "advice_dont_share_otp"
        if (has("click_link") || result.links.isNotEmpty()) out += "advice_dont_click"
        if (has("pay_to_receive", "advance_payment", "refund_cashback")) out += "advice_dont_pay"
        if (has("remote_access")) out += "advice_no_remote_app"
        if (has("digital_arrest", "legal_threat", "authority_impersonation")) {
            out += "advice_police_no_arrest"
        }
        out += "advice_verify_official"
        out += "advice_block_number"

        if (result.verdict == Verdict.DANGEROUS) {
            out += "advice_report_1930"
            out += "advice_report_cybercrime"
            out += "advice_keep_message"
        }
        return out.toList()
    }
}
