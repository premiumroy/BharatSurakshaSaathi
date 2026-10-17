package com.bharatsuraksha.saathi

import com.bharatsuraksha.saathi.domain.agent.BatteryGuidance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BatteryGuidanceTest {

    @Test
    fun xiaomiAndItsBrandsAreRecognised() {
        assertEquals(BatteryGuidance.XIAOMI, BatteryGuidance.vendorFor("Xiaomi", "Redmi").key)
        assertEquals(BatteryGuidance.XIAOMI, BatteryGuidance.vendorFor("Xiaomi", "POCO").key)
        assertEquals(BatteryGuidance.XIAOMI, BatteryGuidance.vendorFor("Xiaomi", "xiaomi").key)
    }

    @Test
    fun oppoRealmeAndOnePlusAreRecognised() {
        assertEquals(BatteryGuidance.OPPO, BatteryGuidance.vendorFor("OPPO", "OPPO").key)
        assertEquals(BatteryGuidance.OPPO, BatteryGuidance.vendorFor("realme", "realme").key)
        assertEquals(BatteryGuidance.OPPO, BatteryGuidance.vendorFor("OnePlus", "OnePlus").key)
    }

    @Test
    fun vivoIsRecognised() {
        assertEquals(BatteryGuidance.VIVO, BatteryGuidance.vendorFor("vivo", "vivo").key)
        assertEquals(BatteryGuidance.VIVO, BatteryGuidance.vendorFor("vivo", "iQOO").key)
    }

    @Test
    fun samsungIsRecognised() {
        assertEquals(BatteryGuidance.SAMSUNG, BatteryGuidance.vendorFor("samsung", "samsung").key)
    }

    @Test
    fun anythingElseFallsBackToTheGenericList() {
        assertEquals(BatteryGuidance.OTHER, BatteryGuidance.vendorFor("Google", "Pixel").key)
        assertEquals(BatteryGuidance.OTHER, BatteryGuidance.vendorFor(null, null).key)
    }

    @Test
    fun matchingIsCaseInsensitive() {
        assertEquals(BatteryGuidance.SAMSUNG, BatteryGuidance.vendorFor("SAMSUNG", null).key)
        assertEquals(BatteryGuidance.XIAOMI, BatteryGuidance.vendorFor(null, "ReDmI").key)
    }

    @Test
    fun everyVendorHasSteps() {
        BatteryGuidance.VENDORS.forEach { vendor ->
            assertTrue("no steps for ${vendor.key}", vendor.stepKeys.isNotEmpty())
            assertTrue("no label for ${vendor.key}", vendor.label.isNotBlank())
        }
    }

    @Test
    fun theGenericListIsLastSoItNeverWinsEarly() {
        assertEquals(BatteryGuidance.OTHER, BatteryGuidance.VENDORS.last().key)
        assertTrue(BatteryGuidance.VENDORS.last().tokens.isEmpty())
    }
}
