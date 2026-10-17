package com.bharatsuraksha.saathi.domain.upi

import com.bharatsuraksha.saathi.domain.model.Verdict

/**
 * Reads a decoded UPI payload and says whether scanning it would SEND money,
 * and how risky it looks. Reason keys are resource names, so this stays pure
 * and unit tested. Nothing here ever calls a UPI ID "verified".
 */
object UpiAnalyzer {

    enum class Direction { SENDS, UNKNOWN }

    data class Finding(val ruleId: String, val weight: Int, val reasonKey: String)

    data class Analysis(
        val verdict: Verdict,
        val score: Int,
        val direction: Direction,
        val findings: List<Finding>
    )

    const val REASON_COLLECT = "upi_reason_collect"
    const val REASON_AMOUNT = "upi_reason_amount"
    const val REASON_LARGE = "upi_reason_large"
    const val REASON_AUTHORITY = "upi_reason_authority"
    const val REASON_HANDLE = "upi_reason_handle"
    const val REASON_MISSING_PAYEE = "upi_reason_missing"
    const val REASON_PLAIN_ID = "upi_reason_plain"

    /** Anything at or above this is treated as a large pre-filled amount. */
    const val LARGE_AMOUNT = 5_000.0

    private val authorityWords = listOf(
        "police", "cbi", "customs", "custom", "income tax", "incometax", "govt", "government",
        "rbi", "court", "narcotic", "challan", "traffic", "electricity", "bijli", "board",
        "sbi", "hdfc", "icici", "axis", "kotak", "pnb", "canara", "union bank", "baroda",
        "idfc", "yes bank", "paytm", "phonepe", "gpay", "bharat pay"
    )

    /** Handles we recognise. An unknown one is a mild flag, never proof. */
    private val knownHandles = setOf(
        "ybl", "ibl", "axl", "apl", "upi", "paytm", "okaxis", "oksbi", "okhdfcbank",
        "okicici", "sbi", "hdfcbank", "icici", "axisbank", "kotak", "idfcbank",
        "barodampay", "airtel", "freecharge", "jupiteraxis", "naviaxis", "yapl",
        "abfspay", "timecosmos", "waaxis", "wahdfcbank", "waicici", "wasbi",
        "rmhdfcbank", "rml", "pnb", "cnrb", "uco", "idbi", "federal", "indus"
    )

    fun analyze(payload: UpiPayload): Analysis {
        var score = 0
        val findings = mutableListOf<Finding>()

        if (payload.isCollectRequest) {
            score += 50
            findings += Finding("upi_collect_request", 50, REASON_COLLECT)
        }

        if (payload.hasAmount) {
            score += 15
            findings += Finding("upi_amount_present", 15, REASON_AMOUNT)
            val value = payload.amountValue
            if (value != null && value >= LARGE_AMOUNT) {
                score += 30
                findings += Finding("upi_amount_large", 30, REASON_LARGE)
            }
        }

        val name = payload.payeeName?.lowercase().orEmpty()
        if (name.isNotBlank() && authorityWords.any { name.contains(it) }) {
            score += 25
            findings += Finding("upi_payee_imitates_authority", 25, REASON_AUTHORITY)
        }

        val handle = payload.payeeAddress?.substringAfter('@', "")?.lowercase().orEmpty()
        if (handle.isNotBlank() && handle !in knownHandles) {
            score += 10
            findings += Finding("upi_odd_handle", 10, REASON_HANDLE)
        }

        if (payload.payeeAddress == null) {
            score += 15
            findings += Finding("upi_missing_payee", 15, REASON_MISSING_PAYEE)
        }

        if (payload.kind == UpiKind.PLAIN_ID) {
            findings += Finding("upi_plain_id", 0, REASON_PLAIN_ID)
        }

        val verdict = when {
            score >= 45 -> Verdict.DANGEROUS
            score >= 15 -> Verdict.CAREFUL
            else -> Verdict.SAFE
        }

        val direction = if (payload.sendsMoney) Direction.SENDS else Direction.UNKNOWN
        return Analysis(verdict, score.coerceAtMost(100), direction, findings)
    }
}
