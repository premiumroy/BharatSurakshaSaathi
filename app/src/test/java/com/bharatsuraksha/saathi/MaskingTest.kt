package com.bharatsuraksha.saathi

import com.bharatsuraksha.saathi.domain.safety.Masking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MaskingTest {

    @Test
    fun masksAPlainMobileNumber() {
        val out = Masking.mask("Call me on 9876543210 today")
        assertFalse(out.contains("9876543210"))
        assertTrue(out.contains("•"))
        assertTrue(out.endsWith("10 today"))
    }

    @Test
    fun masksANumberWithSpacesAndCountryCode() {
        val out = Masking.mask("+91 98765 43210")
        assertFalse(out.contains("98765"))
        assertFalse(out.contains("43210".substring(0, 3)))
        assertTrue(out.contains("•"))
    }

    @Test
    fun masksUpiIds() {
        val out = Masking.mask("Send to rahul.sharma@ybl now")
        assertFalse(out.contains("rahul.sharma@ybl"))
        assertTrue(out.contains("@"))
    }

    @Test
    fun masksLinks() {
        val out = Masking.mask("Open http://sbi-kyc.xyz/login now")
        assertFalse(out.contains("sbi-kyc.xyz"))
    }

    @Test
    fun masksAccountNumbers() {
        val out = Masking.mask("A/c 123456789012 debited")
        assertFalse(out.contains("123456789012"))
        assertTrue(out.contains("9012"))
    }

    @Test
    fun leavesOrdinaryTextAlone() {
        val text = "पापा, मैं कॉलेज से निकल गया हूँ।"
        assertEquals(text, Masking.mask(text))
    }

    @Test
    fun leavesShortNumbersAlone() {
        assertEquals("Order 1234 shipped", Masking.mask("Order 1234 shipped"))
    }

    @Test
    fun detectsSensitiveContent() {
        assertTrue(Masking.containsSensitive("call 9876543210"))
        assertTrue(Masking.containsSensitive("pay to abc@ybl"))
        assertFalse(Masking.containsSensitive("hello there, how are you"))
    }

    @Test
    fun maskingIsIdempotent() {
        val once = Masking.mask("call 9876543210 and pay a@ybl")
        assertEquals(once, Masking.mask(once))
    }
}
