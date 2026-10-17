package com.bharatsuraksha.saathi.domain.upi

import java.net.URLDecoder

enum class UpiKind { PAY, MANDATE, PLAIN_ID, UNKNOWN }

/**
 * One decoded UPI payload. A UPI QR can only ever SEND money, never receive it,
 * so [kind] and [payeeAddress] are the whole story.
 */
data class UpiPayload(
    val raw: String,
    val kind: UpiKind,
    val payeeAddress: String? = null,
    val payeeName: String? = null,
    val amount: String? = null,
    val currency: String? = null,
    val note: String? = null,
    val merchantCode: String? = null,
    val txnRef: String? = null,
    val problems: List<String> = emptyList()
) {
    val amountValue: Double? get() = amount?.trim()?.toDoubleOrNull()
    val hasAmount: Boolean get() = !amount.isNullOrBlank()
    val isCollectRequest: Boolean get() = kind == UpiKind.MANDATE

    /** True when scanning this would move money out of the user's account. */
    val sendsMoney: Boolean get() = kind == UpiKind.PAY || kind == UpiKind.MANDATE
}

/**
 * Parses upi://pay and upi://mandate payloads (pa, pn, am, tn, cu, mc, tr) and
 * plain UPI IDs. Pure Kotlin so every case can be unit tested.
 */
object UpiParser {

    const val P_AMOUNT = "am"
    const val P_PAYEE = "pa"
    const val P_NAME = "pn"
    const val P_NOTE = "tn"
    const val P_CURRENCY = "cu"
    const val P_MERCHANT = "mc"
    const val P_TXN_REF = "tr"

    private val upiId = Regex("^[A-Za-z0-9._%+-]{2,}@[A-Za-z][A-Za-z0-9.]{1,}$")

    fun isUpiId(text: String): Boolean = upiId.matches(text.trim())

    fun parse(rawInput: String): UpiPayload {
        val raw = rawInput.trim().replace("\n", "").replace("\r", "")
        if (raw.isEmpty()) return UpiPayload(raw, UpiKind.UNKNOWN, problems = listOf("empty"))

        val lower = raw.lowercase()
        val kind = when {
            lower.startsWith("upi://pay") -> UpiKind.PAY
            lower.startsWith("upi://mandate") -> UpiKind.MANDATE
            lower.startsWith("upi://") -> UpiKind.UNKNOWN
            isUpiId(raw) -> UpiKind.PLAIN_ID
            else -> UpiKind.UNKNOWN
        }

        if (kind == UpiKind.PLAIN_ID) {
            return UpiPayload(raw = raw, kind = kind, payeeAddress = raw)
        }
        if (kind == UpiKind.UNKNOWN) {
            return UpiPayload(raw = raw, kind = kind, problems = listOf("not_upi"))
        }

        val query = raw.substringAfter('?', "")
        val params = HashMap<String, String>()
        query.split('&').forEach { pair ->
            if (pair.isBlank()) return@forEach
            val key = pair.substringBefore('=', "").lowercase()
            val value = pair.substringAfter('=', "")
            if (key.isNotBlank()) params[key] = decode(value)
        }

        val problems = mutableListOf<String>()
        val payee = params[P_PAYEE]?.takeIf { it.isNotBlank() }
        if (payee == null) problems += "missing_payee"
        else if (!isUpiId(payee)) problems += "payee_not_a_upi_id"

        val amount = params[P_AMOUNT]?.takeIf { it.isNotBlank() }
        if (amount != null && amount.toDoubleOrNull() == null) problems += "amount_not_a_number"

        return UpiPayload(
            raw = raw,
            kind = kind,
            payeeAddress = payee,
            payeeName = params[P_NAME]?.takeIf { it.isNotBlank() },
            amount = amount,
            currency = params[P_CURRENCY]?.takeIf { it.isNotBlank() },
            note = params[P_NOTE]?.takeIf { it.isNotBlank() },
            merchantCode = params[P_MERCHANT]?.takeIf { it.isNotBlank() },
            txnRef = params[P_TXN_REF]?.takeIf { it.isNotBlank() },
            problems = problems
        )
    }

    private fun decode(value: String): String = try {
        URLDecoder.decode(value, "UTF-8")
    } catch (e: Exception) {
        value
    }
}
