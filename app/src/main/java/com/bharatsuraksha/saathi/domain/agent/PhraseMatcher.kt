package com.bharatsuraksha.saathi.domain.agent

import com.bharatsuraksha.saathi.domain.model.Verdict

/**
 * Matches live call text (or a transcript in a test) against the phrases that
 * mark a phone scam, in Hindi, English and Hinglish. Pure logic, unit tested.
 * Nothing here stores or transmits anything.
 */
object PhraseMatcher {

    data class Phrase(val key: String, val category: String, val weight: Int, val needles: List<String>)

    data class Hit(val phraseKey: String, val category: String, val weight: Int)

    data class Match(val score: Int, val verdict: Verdict, val hits: List<Hit>)

    const val CAT_AUTHORITY = "authority"
    const val CAT_DIGITAL_ARREST = "digital_arrest"
    const val CAT_NO_DISCONNECT = "no_disconnect"
    const val CAT_SECRECY = "secrecy"
    const val CAT_CREDENTIAL = "credential"
    const val CAT_SAFE_ACCOUNT = "safe_account"
    const val CAT_UPI = "upi"
    const val CAT_URGENCY = "urgency"

    val PHRASES = listOf(
        Phrase("cbi", CAT_AUTHORITY, 40, listOf("cbi", "सीबीआई", "सी बी आई")),
        Phrase("police", CAT_AUTHORITY, 35, listOf("police", "पुलिस", "थाना", "inspector", "इंस्पेक्टर")),
        Phrase("customs", CAT_AUTHORITY, 35, listOf("customs", "कस्टम", "custom officer")),
        Phrase("ed", CAT_AUTHORITY, 35, listOf("enforcement directorate", "ईडी", "ed officer")),
        Phrase("narcotics", CAT_AUTHORITY, 30, listOf("narcotics", "नारकोटिक्स", "ndps")),
        Phrase("digital_arrest", CAT_DIGITAL_ARREST, 45, listOf("digital arrest", "डिजिटल अरेस्ट", "डिजिटल arrest")),
        Phrase("arrest", CAT_DIGITAL_ARREST, 30, listOf("arrest", "गिरफ्तार", "गिरफ़्तार", "warrant", "वारंट")),
        Phrase("court_case", CAT_DIGITAL_ARREST, 20, listOf("court", "कोर्ट", "अदालत", "case filed", "एफआईआर", "केस", "case")),
        Phrase("do_not_disconnect", CAT_NO_DISCONNECT, 30, listOf(
            "do not disconnect", "don't disconnect", "do not hang up", "don't hang up",
            "कॉल मत काटो", "कॉल मत काटना", "फोन मत काटो", "मत काटो"
        )),
        Phrase("stay_on_line", CAT_NO_DISCONNECT, 20, listOf("stay on the line", "stay connected", "लाइन पर रहो", "लाइन पर रहिए")),
        Phrase("secrecy", CAT_SECRECY, 30, listOf(
            "tell no one", "do not tell anyone", "don't tell anyone", "keep it secret", "keep this confidential",
            "किसी को मत बताओ", "किसी को नहीं बताना", "किसी को मत बताना", "गुप्त रखो", "किसी को न बताएँ",
            "किसी को बताया", "किसी को बता दिया", "किसी को बताना मत"
        )),
        Phrase("otp", CAT_CREDENTIAL, 35, listOf("otp", "ओटीपी", "ओ टी पी", "one time password")),
        Phrase("otp_request", CAT_CREDENTIAL, 45, listOf(
            "share the otp", "share otp", "otp share", "send the otp", "give me the otp", "tell me the otp",
            "otp batao", "otp bataye", "ओटीपी बताओ", "ओटीपी बताइए", "ओटीपी शेयर", "otp bolo"
        )),
        Phrase("pin", CAT_CREDENTIAL, 30, listOf("upi pin", "atm pin", "पिन", "पिन नंबर", "mpin")),
        Phrase("cvv", CAT_CREDENTIAL, 30, listOf("cvv", "सीवीवी", "card number", "कार्ड नंबर")),
        Phrase("password", CAT_CREDENTIAL, 25, listOf("password", "पासवर्ड", "net banking login")),
        Phrase("safe_account", CAT_SAFE_ACCOUNT, 45, listOf(
            "safe account", "सुरक्षित खाता", "सुरक्षित खाते", "government account", "सरकारी खाता",
            "fund regularisation", "फंड रेगुलराइजेशन", "verification account", "वेरिफिकेशन खाता"
        )),
        Phrase("transfer_money", CAT_SAFE_ACCOUNT, 30, listOf(
            "transfer the money", "transfer your money", "पैसे ट्रांसफर", "पैसा ट्रांसफर", "amount transfer"
        )),
        Phrase("upi_request", CAT_UPI, 30, listOf(
            "upi", "यूपीआई", "gpay", "google pay", "phonepe", "फोनपे", "paytm", "पेटीएम",
            "scan the qr", "qr code", "क्यूआर"
        )),
        Phrase("send_money", CAT_UPI, 25, listOf(
            "send the money", "send money", "पैसे भेजो", "पैसे भेजिए", "पैसा भेजो", "amount bhej"
        )),
        Phrase("urgency", CAT_URGENCY, 15, listOf(
            "immediately", "right now", "within an hour", "last warning", "final notice",
            "तुरंत", "अभी", "आखिरी चेतावनी", "जल्दी करो", "जल्दी कीजिए"
        )),
        Phrase("parcel", CAT_AUTHORITY, 25, listOf("parcel", "पार्सल", "courier", "कूरियर", "consignment"))
    )

    /**
     * "Do not share this OTP" is a warning, not a request. When the speaker is
     * telling the listener to keep a credential safe, credential phrases are
     * dropped. This mirrors the engine's false-positive guard.
     */
    private val neverShare = listOf(
        "do not share", "don't share", "never share", "do not disclose",
        "शेयर न करें", "साझा न करें", "मत बताइए", "मत बताओ"
    )

    fun match(transcript: String): Match {
        val text = normalise(transcript)
        if (text.isBlank()) return Match(0, Verdict.SAFE, emptyList())

        val suppressCredentials = neverShare.any { text.contains(normalise(it)) }

        val hits = mutableListOf<Hit>()
        var score = 0
        PHRASES.forEach { phrase ->
            val mentioned = phrase.needles.any { text.contains(normalise(it)) }
            if (!mentioned) return@forEach
            if (suppressCredentials && phrase.category == CAT_CREDENTIAL) return@forEach
            hits += Hit(phrase.key, phrase.category, phrase.weight)
            score += phrase.weight
        }

        val categories = hits.map { it.category }.toSet()
        // The digital-arrest signature: authority plus secrecy plus pressure.
        if (CAT_AUTHORITY in categories && CAT_SECRECY in categories &&
            (CAT_SAFE_ACCOUNT in categories || CAT_CREDENTIAL in categories || CAT_URGENCY in categories)
        ) {
            score += 30
        }
        // Authority plus a money demand is the same trick without the secrecy.
        if (CAT_AUTHORITY in categories && (CAT_SAFE_ACCOUNT in categories || CAT_UPI in categories)) {
            score += 20
        }

        val verdict = when {
            score >= 45 -> Verdict.DANGEROUS
            score >= 20 -> Verdict.CAREFUL
            else -> Verdict.SAFE
        }
        return Match(score.coerceAtMost(100), verdict, hits)
    }

    /** Lowercase and collapse whitespace, so matching is stable. */
    fun normalise(text: String): String =
        text.lowercase().replace(Regex("\\s+"), " ").trim()
}
