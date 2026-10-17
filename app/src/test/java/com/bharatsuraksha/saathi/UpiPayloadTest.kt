package com.bharatsuraksha.saathi

import com.bharatsuraksha.saathi.domain.model.Verdict
import com.bharatsuraksha.saathi.domain.upi.UpiAnalyzer
import com.bharatsuraksha.saathi.domain.upi.UpiKind
import com.bharatsuraksha.saathi.domain.upi.UpiParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Thirty-plus labelled UPI payloads: real shapes, scam shapes and malformed
 * input. Every case says what it is and what the app must decide.
 */
class UpiPayloadTest {

    // ---------------------------------------------------------------- parsing

    @Test
    fun parsesAFullPayPayload() {
        val p = UpiParser.parse("upi://pay?pa=rahul@ybl&pn=Rahul%20Sharma&am=250&cu=INR&tn=Dinner&mc=5411&tr=AB123")
        assertEquals(UpiKind.PAY, p.kind)
        assertEquals("rahul@ybl", p.payeeAddress)
        assertEquals("Rahul Sharma", p.payeeName)
        assertEquals("250", p.amount)
        assertEquals("INR", p.currency)
        assertEquals("Dinner", p.note)
        assertEquals("5411", p.merchantCode)
        assertEquals("AB123", p.txnRef)
        assertTrue(p.problems.isEmpty())
    }

    @Test
    fun parsesAMandatePayloadAsACollectRequest() {
        val p = UpiParser.parse("upi://mandate?pa=merchant@paytm&pn=Shop&am=1000&cu=INR&tr=M1")
        assertEquals(UpiKind.MANDATE, p.kind)
        assertTrue(p.isCollectRequest)
        assertTrue(p.sendsMoney)
    }

    @Test
    fun parsesAPlainUpiId() {
        val p = UpiParser.parse("someone@okaxis")
        assertEquals(UpiKind.PLAIN_ID, p.kind)
        assertEquals("someone@okaxis", p.payeeAddress)
        assertFalse(p.sendsMoney)
    }

    @Test
    fun schemeIsCaseInsensitive() {
        assertEquals(UpiKind.PAY, UpiParser.parse("UPI://PAY?pa=a@ybl&am=10").kind)
        assertEquals(UpiKind.MANDATE, UpiParser.parse("Upi://Mandate?pa=a@ybl").kind)
    }

    @Test
    fun unknownSchemeIsRejected() {
        val p = UpiParser.parse("https://example.com/pay?pa=a@ybl")
        assertEquals(UpiKind.UNKNOWN, p.kind)
        assertTrue(p.problems.contains("not_upi"))
    }

    @Test
    fun emptyInputIsRejected() {
        assertTrue(UpiParser.parse("   ").problems.contains("empty"))
    }

    @Test
    fun missingPayeeIsFlagged() {
        val p = UpiParser.parse("upi://pay?pn=Shop&am=10")
        assertNull(p.payeeAddress)
        assertTrue(p.problems.contains("missing_payee"))
    }

    @Test
    fun payeeThatIsNotAUpiIdIsFlagged() {
        val p = UpiParser.parse("upi://pay?pa=notanid&am=10")
        assertTrue(p.problems.contains("payee_not_a_upi_id"))
    }

    @Test
    fun amountThatIsNotANumberIsFlagged() {
        val p = UpiParser.parse("upi://pay?pa=a@ybl&am=abc")
        assertTrue(p.problems.contains("amount_not_a_number"))
        assertNull(p.amountValue)
    }

    @Test
    fun extraUnknownParametersAreIgnored() {
        val p = UpiParser.parse("upi://pay?pa=a@ybl&am=10&zz=whatever&sign=abc")
        assertEquals("a@ybl", p.payeeAddress)
        assertEquals("10", p.amount)
    }

    @Test
    fun urlEncodedValuesAreDecoded() {
        val p = UpiParser.parse("upi://pay?pa=a@ybl&pn=Krishna%20General%20Store&tn=Groceries%20%26%20milk")
        assertEquals("Krishna General Store", p.payeeName)
        assertEquals("Groceries & milk", p.note)
    }

    @Test
    fun noQueryStringStillParsesTheKind() {
        val p = UpiParser.parse("upi://pay")
        assertEquals(UpiKind.PAY, p.kind)
        assertTrue(p.problems.contains("missing_payee"))
    }

    @Test
    fun isUpiIdAcceptsAndRejectsCorrectly() {
        assertTrue(UpiParser.isUpiId("a.b-c@ybl"))
        assertTrue(UpiParser.isUpiId("9876543210@paytm"))
        assertFalse(UpiParser.isUpiId("plainword"))
        assertFalse(UpiParser.isUpiId("@ybl"))
    }

    // --------------------------------------------------------------- analysis

    @Test
    fun aPlainIdWithNoAmountHasNoClearWarningSigns() {
        val a = UpiAnalyzer.analyze(UpiParser.parse("someone@okaxis"))
        assertEquals(Verdict.SAFE, a.verdict)
        assertEquals(UpiAnalyzer.Direction.UNKNOWN, a.direction)
    }

    @Test
    fun aPayQrWithAnAmountIsAtLeastCareful() {
        val a = UpiAnalyzer.analyze(UpiParser.parse("upi://pay?pa=shop@ybl&pn=Shop&am=250&cu=INR"))
        assertEquals(Verdict.CAREFUL, a.verdict)
        assertEquals(UpiAnalyzer.Direction.SENDS, a.direction)
        assertTrue(a.findings.any { it.ruleId == "upi_amount_present" })
    }

    @Test
    fun aPayQrWithNoAmountIsStillASend() {
        val a = UpiAnalyzer.analyze(UpiParser.parse("upi://pay?pa=shop@ybl&pn=Shop"))
        assertTrue(a.direction == UpiAnalyzer.Direction.SENDS)
    }

    @Test
    fun aCollectRequestIsDangerous() {
        val a = UpiAnalyzer.analyze(UpiParser.parse("upi://mandate?pa=merchant@paytm&pn=Shop&am=1000"))
        assertEquals(Verdict.DANGEROUS, a.verdict)
        assertTrue(a.findings.any { it.ruleId == "upi_collect_request" })
    }

    @Test
    fun aLargePreFilledAmountIsDangerous() {
        val a = UpiAnalyzer.analyze(UpiParser.parse("upi://pay?pa=x@ybl&pn=Someone&am=25000"))
        assertEquals(Verdict.DANGEROUS, a.verdict)
        assertTrue(a.findings.any { it.ruleId == "upi_amount_large" })
    }

    @Test
    fun exactlyFiveThousandCountsAsLarge() {
        val a = UpiAnalyzer.analyze(UpiParser.parse("upi://pay?pa=x@ybl&pn=Someone&am=5000"))
        assertTrue(a.findings.any { it.ruleId == "upi_amount_large" })
    }

    @Test
    fun fourThousandNineHundredIsNotLarge() {
        val a = UpiAnalyzer.analyze(UpiParser.parse("upi://pay?pa=x@ybl&pn=Someone&am=4900"))
        assertFalse(a.findings.any { it.ruleId == "upi_amount_large" })
    }

    @Test
    fun aPayeeNameImitatingThePoliceIsFlagged() {
        val a = UpiAnalyzer.analyze(UpiParser.parse("upi://pay?pa=x@ybl&pn=Delhi%20Police%20Fund&am=2000"))
        assertTrue(a.findings.any { it.ruleId == "upi_payee_imitates_authority" })
    }

    @Test
    fun aPayeeNameImitatingABankIsFlagged() {
        val a = UpiAnalyzer.analyze(UpiParser.parse("upi://pay?pa=x@ybl&pn=SBI%20KYC%20Desk&am=500"))
        assertTrue(a.findings.any { it.ruleId == "upi_payee_imitates_authority" })
    }

    @Test
    fun aGovernmentImitatingNameIsFlagged() {
        val a = UpiAnalyzer.analyze(UpiParser.parse("upi://pay?pa=x@ybl&pn=Income%20Tax%20Refund&am=100"))
        assertTrue(a.findings.any { it.ruleId == "upi_payee_imitates_authority" })
    }

    @Test
    fun aNormalShopNameIsNotFlaggedAsAuthority() {
        val a = UpiAnalyzer.analyze(UpiParser.parse("upi://pay?pa=shop@ybl&pn=Krishna%20General%20Store&am=100"))
        assertFalse(a.findings.any { it.ruleId == "upi_payee_imitates_authority" })
    }

    @Test
    fun anUnrecognisedHandleIsFlaggedMildly() {
        val a = UpiAnalyzer.analyze(UpiParser.parse("upi://pay?pa=someone@strangebank&am=100"))
        assertTrue(a.findings.any { it.ruleId == "upi_odd_handle" })
    }

    @Test
    fun aKnownHandleIsNotFlagged() {
        val a = UpiAnalyzer.analyze(UpiParser.parse("upi://pay?pa=someone@okhdfcbank&am=100"))
        assertFalse(a.findings.any { it.ruleId == "upi_odd_handle" })
    }

    @Test
    fun aMissingPayeeRaisesTheRisk() {
        val a = UpiAnalyzer.analyze(UpiParser.parse("upi://pay?pn=Nobody&am=10"))
        assertTrue(a.findings.any { it.ruleId == "upi_missing_payee" })
    }

    @Test
    fun aPlainIdCarriesThePlainIdNote() {
        val a = UpiAnalyzer.analyze(UpiParser.parse("someone@ybl"))
        assertTrue(a.findings.any { it.ruleId == "upi_plain_id" })
    }

    @Test
    fun scoreNeverExceedsOneHundred() {
        val a = UpiAnalyzer.analyze(
            UpiParser.parse("upi://mandate?pa=x@strange&pn=Police%20Fund&am=99999")
        )
        assertTrue(a.score <= 100)
        assertEquals(Verdict.DANGEROUS, a.verdict)
    }

    @Test
    fun theWorstCaseCollectRequestIsDangerous() {
        // The classic "scan this to receive your money" story arrives as a collect.
        val a = UpiAnalyzer.analyze(
            UpiParser.parse("upi://mandate?pa=customer@paytm&pn=Refund%20Desk&am=4999&cu=INR&tr=R1")
        )
        assertEquals(Verdict.DANGEROUS, a.verdict)
        assertTrue(a.direction == UpiAnalyzer.Direction.SENDS)
    }

    @Test
    fun aNormalMerchantQrIsOnlyCareful() {
        val a = UpiAnalyzer.analyze(
            UpiParser.parse("upi://pay?pa=store@okicici&pn=Local%20Kirana&am=340&cu=INR&mc=5411")
        )
        assertEquals(Verdict.CAREFUL, a.verdict)
    }

    @Test
    fun analysisAlwaysProducesAReasonForKeyItRaises() {
        val a = UpiAnalyzer.analyze(UpiParser.parse("upi://mandate?pa=x@strange&pn=Police&am=99999"))
        assertTrue(a.findings.isNotEmpty())
        a.findings.forEach { assertTrue(it.reasonKey.startsWith("upi_reason_")) }
    }

    @Test
    fun directionIsUnknownForANonUpiString() {
        val a = UpiAnalyzer.analyze(UpiParser.parse("just some text"))
        assertEquals(UpiAnalyzer.Direction.UNKNOWN, a.direction)
        assertEquals(Verdict.SAFE, a.verdict)
    }

    @Test
    fun aVeryLongNoteDoesNotBreakParsing() {
        val note = "x".repeat(600)
        val p = UpiParser.parse("upi://pay?pa=a@ybl&am=10&tn=$note")
        assertEquals(600, p.note?.length)
        assertEquals("a@ybl", p.payeeAddress)
    }

    @Test
    fun multipleParamsWithTheSameKeyKeepTheLast() {
        val p = UpiParser.parse("upi://pay?pa=first@ybl&pa=second@ybl&am=10")
        assertEquals("second@ybl", p.payeeAddress)
    }
}
