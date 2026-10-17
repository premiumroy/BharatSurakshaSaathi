package com.bharatsuraksha.saathi

import com.bharatsuraksha.saathi.domain.agent.PhoneHealth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneHealthTest {

    @Test
    fun aCleanPhoneScoresFull() {
        val report = PhoneHealth.score(PhoneHealth.Flags())
        assertEquals(100, report.score)
        assertEquals(PhoneHealth.Level.GOOD, report.level)
        assertTrue(report.actionKeys.contains(PhoneHealth.ACTION_ALL_CLEAR))
    }

    @Test
    fun oneUnknownAccessibilityServiceIsAWarning() {
        val report = PhoneHealth.score(PhoneHealth.Flags(unknownAccessibilityServices = 1))
        assertEquals(75, report.score)
        assertEquals(PhoneHealth.Level.WATCH, report.level)
        assertTrue(report.actionKeys.contains(PhoneHealth.ACTION_ACCESSIBILITY))
    }

    @Test
    fun accessibilityDeductionIsCapped() {
        val report = PhoneHealth.score(PhoneHealth.Flags(unknownAccessibilityServices = 9))
        assertEquals(50, report.score)
    }

    @Test
    fun riskyPermissionAppsDeduct() {
        val report = PhoneHealth.score(PhoneHealth.Flags(riskyPermissionApps = 2))
        assertEquals(70, report.score)
        assertTrue(report.actionKeys.contains(PhoneHealth.ACTION_PERMISSIONS))
    }

    @Test
    fun recentApksDeduct() {
        val report = PhoneHealth.score(PhoneHealth.Flags(recentApkDownloads = 2))
        assertEquals(80, report.score)
        assertTrue(report.actionKeys.contains(PhoneHealth.ACTION_APK))
    }

    @Test
    fun everythingWrongIsRisky() {
        val report = PhoneHealth.score(
            PhoneHealth.Flags(
                unknownAccessibilityServices = 2,
                riskyPermissionApps = 2,
                recentApkDownloads = 2
            )
        )
        assertEquals(0, report.score)
        assertEquals(PhoneHealth.Level.RISKY, report.level)
    }

    @Test
    fun scoreNeverGoesBelowZero() {
        val report = PhoneHealth.score(
            PhoneHealth.Flags(unknownAccessibilityServices = 20, riskyPermissionApps = 20, recentApkDownloads = 20)
        )
        assertTrue(report.score >= 0)
    }

    @Test
    fun atMostThreeActionsAreReturned() {
        val report = PhoneHealth.score(
            PhoneHealth.Flags(unknownAccessibilityServices = 1, riskyPermissionApps = 1, recentApkDownloads = 1)
        )
        assertTrue(report.actionKeys.size <= 3)
        assertEquals(3, report.actionKeys.size)
    }

    @Test
    fun theAllClearActionIsOnlyUsedWhenNothingIsFlagged() {
        val flagged = PhoneHealth.score(PhoneHealth.Flags(recentApkDownloads = 1))
        assertTrue(!flagged.actionKeys.contains(PhoneHealth.ACTION_ALL_CLEAR))
    }
}
