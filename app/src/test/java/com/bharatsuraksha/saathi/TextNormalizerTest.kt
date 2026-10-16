package com.bharatsuraksha.saathi

import com.bharatsuraksha.saathi.domain.engine.TextNormalizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextNormalizerTest {

    @Test
    fun lowercasesLatin() {
        assertEquals("hello world", TextNormalizer.normalize("Hello WORLD"))
    }

    @Test
    fun collapsesWhitespaceAndTrims() {
        assertEquals("a b c", TextNormalizer.normalize("  a   b \n c  "))
    }

    @Test
    fun removesZeroWidthCharacters() {
        // "OTP" with zero-width spaces injected between letters
        val sneaky = "O\u200BTP"
        assertEquals("otp", TextNormalizer.normalize(sneaky))
    }

    @Test
    fun normalizesSmartQuotesAndDashes() {
        val out = TextNormalizer.normalize("It\u2019s a \u201Ctest\u201D \u2013 really")
        assertEquals("it's a \"test\" - really", out)
    }

    @Test
    fun keepsDevanagari() {
        val out = TextNormalizer.normalize("खाता बंद हो जाएगा")
        assertTrue(out.contains("खाता"))
        assertTrue(out.contains("बंद"))
    }

    @Test
    fun emptyStaysEmpty() {
        assertEquals("", TextNormalizer.normalize(""))
        assertFalse(TextNormalizer.normalize("   ").isNotEmpty())
    }
}
