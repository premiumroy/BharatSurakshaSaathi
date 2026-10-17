package com.bharatsuraksha.saathi.domain.engine

import com.bharatsuraksha.saathi.domain.model.AnalysisResult
import com.bharatsuraksha.saathi.domain.model.Verdict

/**
 * Turns a result into plain "what to do now" steps.
 *
 * Returns string-resource KEYS (not localized text) so the UI can render them in
 * the user's chosen language. v2 adds family-specific steps, so a digital-arrest
 * message gets different advice from a fake-refund one.
 */
object AdviceProvider {

    /** Advice keys that belong to a scam family. */
    private val familyAdvice: Map<String, List<String>> = mapOf(
        "digital_arrest" to listOf("advice_police_no_arrest", "advice_dont_pay"),
        "investment" to listOf("advice_no_guaranteed_returns"),
        "task_job" to listOf("advice_job_never_pay"),
        "family_emergency" to listOf("advice_call_back_known_number"),
        "loan_app" to listOf("advice_loan_app_report"),
        "lottery_prize" to listOf("advice_lottery_never_pay"),
        "romance_sextortion" to listOf("advice_sextortion_dont_pay"),
        "courier_delivery" to listOf("advice_courier_never_pay"),
        "refund_tax_bill" to listOf("advice_dont_click")
    )

    fun adviceKeys(result: AnalysisResult): List<String> {
        val ids = result.matchedRuleIds.toSet()
        fun has(vararg keys: String) = keys.any { ids.contains(it) }

        // A safe message still gets gentle, universal advice.
        if (result.verdict == Verdict.SAFE) {
            return listOf("advice_dont_share_otp", "advice_verify_official")
        }

        val out = LinkedHashSet<String>()

        if (has("otp_request", "share_documents", "safe_account")) out += "advice_dont_share_otp"
        if (has("click_link") || result.links.isNotEmpty()) out += "advice_dont_click"
        if (has("pay_to_receive", "advance_payment", "refund_cashback", "money_transfer")) out += "advice_dont_pay"
        if (has("remote_access")) out += "advice_no_remote_app"
        if (has("digital_arrest", "legal_threat", "authority_impersonation")) {
            out += "advice_police_no_arrest"
        }

        // Family-specific steps, in the order the families were detected.
        result.familyIds.forEach { fid ->
            familyAdvice[fid]?.forEach { out += it }
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
