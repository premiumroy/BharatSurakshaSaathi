package com.bharatsuraksha.saathi

import com.bharatsuraksha.saathi.domain.agent.PhraseMatcher
import com.bharatsuraksha.saathi.domain.model.Verdict
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Thirty labelled call transcripts in Hindi, English and Hinglish. Each case
 * says what it is and what the live matcher must decide.
 */
class PhraseMatcherTest {

    private fun expect(text: String, verdict: Verdict) {
        val m = PhraseMatcher.match(text)
        assertEquals("for: $text", verdict, m.verdict)
    }

    // ------------------------------------------------------------- scams (20)

    @Test fun scam01() = expect("मैं CBI से बोल रहा हूँ, आपके नाम पर केस है।", Verdict.DANGEROUS)
    @Test fun scam02() = expect("This is a digital arrest. Do not disconnect the video call.", Verdict.DANGEROUS)
    @Test fun scam03() = expect("आप डिजिटल अरेस्ट में हैं, कॉल मत काटो।", Verdict.DANGEROUS)
    @Test fun scam04() = expect("Sir, aapke parcel mein drugs mila hai, customs se bol raha hoon.", Verdict.DANGEROUS)
    @Test fun scam05() = expect("अगर आपने किसी को बताया तो गिरफ्तारी हो जाएगी।", Verdict.DANGEROUS)
    @Test fun scam06() = expect("Please share the OTP for verification.", Verdict.DANGEROUS)
    @Test fun scam07() = expect("अपना UPI PIN बताइए, वरना पैसे कट जाएँगे।", Verdict.DANGEROUS)
    @Test fun scam08() = expect("Transfer the money to the safe account for fund regularisation.", Verdict.DANGEROUS)
    @Test fun scam09() = expect("आपके पैसे सुरक्षित खाते में डालने होंगे।", Verdict.DANGEROUS)
    @Test fun scam10() = expect("मैं पुलिस इंस्पेक्टर बोल रहा हूँ, आपका नाम एक केस में आया है।", Verdict.DANGEROUS)
    @Test fun scam11() = expect("Immediately send money or you will be arrested.", Verdict.DANGEROUS)
    @Test fun scam12() = expect("Aapke naam se parcel aaya hai, courier department se.", Verdict.CAREFUL)
    @Test fun scam13() = expect("Sir please stay on the line, do not hang up.", Verdict.DANGEROUS)
    @Test fun scam14() = expect("Scan the QR code and send the money right now.", Verdict.DANGEROUS)
    @Test fun scam15() = expect("This is a final notice from customs, pay immediately.", Verdict.DANGEROUS)
    @Test fun scam16() = expect("मुझे आपका नेट बैंकिंग पासवर्ड और ओटीपी बताइए।", Verdict.DANGEROUS)
    @Test fun scam17() = expect("Enforcement Directorate se bol raha hoon, aapka case hai.", Verdict.DANGEROUS)
    @Test fun scam18() = expect("डिजिटल अरेस्ट में हैं, किसी को मत बताओ।", Verdict.DANGEROUS)
    @Test fun scam19() = expect("गिरफ्तारी वारंट जारी हो चुका है, तुरंत संपर्क करें।", Verdict.DANGEROUS)
    @Test fun scam20() = expect("Tell no one about this call, keep it confidential.", Verdict.CAREFUL)

    // ---------------------------------------------------------- genuine (10)

    @Test fun real01() = expect("पापा, मैं शाम तक घर पहुँच जाऊँगा।", Verdict.SAFE)
    @Test fun real02() = expect("Your order has been delivered. Thank you for shopping.", Verdict.SAFE)
    @Test fun real03() = expect("भाई, कल मिलते हैं।", Verdict.SAFE)
    @Test fun real04() = expect("The meeting is at 4 pm, please join on time.", Verdict.SAFE)
    @Test fun real05() = expect("आपका बिल 25 तारीख को देय है।", Verdict.SAFE)
    @Test fun real06() = expect("Hello, I am calling about your insurance renewal.", Verdict.SAFE)
    @Test fun real07() = expect("ट्रेन 25 मिनट देरी से चल रही है।", Verdict.SAFE)
    @Test fun real08() = expect("The doctor said to take the medicine after food.", Verdict.SAFE)
    @Test fun real09() = expect("आपकी सोसाइटी की मीटिंग रविवार को है।", Verdict.SAFE)
    @Test fun real10() = expect(
        "Your OTP for login is 4821. Do not share this OTP with anyone.",
        Verdict.SAFE
    )

    // ------------------------------------------------------------- structure

    @Test
    fun aWarningAboutOtpIsNotAnOtpRequest() {
        val m = PhraseMatcher.match("Never share your OTP or PIN with anyone.")
        assertTrue(m.hits.none { it.category == PhraseMatcher.CAT_CREDENTIAL })
        assertEquals(Verdict.SAFE, m.verdict)
    }

    @Test
    fun authorityPlusMoneyIsBoostedEvenWithoutSecrecy() {
        val m = PhraseMatcher.match("CBI officer here, transfer the money to the safe account.")
        assertEquals(Verdict.DANGEROUS, m.verdict)
        assertTrue(m.hits.any { it.category == PhraseMatcher.CAT_AUTHORITY })
    }

    @Test
    fun blankInputIsSafe() {
        assertEquals(Verdict.SAFE, PhraseMatcher.match("   ").verdict)
    }

    @Test
    fun scoreNeverExceedsOneHundred() {
        val m = PhraseMatcher.match(
            "digital arrest cbi police customs safe account otp share the otp upi pin immediately"
        )
        assertTrue(m.score <= 100)
        assertEquals(Verdict.DANGEROUS, m.verdict)
    }
}
