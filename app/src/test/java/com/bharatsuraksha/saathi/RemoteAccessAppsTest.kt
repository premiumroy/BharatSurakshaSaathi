package com.bharatsuraksha.saathi

import com.bharatsuraksha.saathi.domain.agent.RemoteAccessApps
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteAccessAppsTest {

    private val now = 1_700_000_000_000L
    private val minute = 60_000L

    @Test
    fun theNamedRemoteAccessToolsAreCovered() {
        assertTrue(RemoteAccessApps.PACKAGES.contains("com.anydesk.anydeskandroid"))
        assertTrue(RemoteAccessApps.PACKAGES.contains("com.teamviewer.teamviewer.market.mobile"))
        assertTrue(RemoteAccessApps.PACKAGES.contains("com.teamviewer.quicksupport.market"))
        assertTrue(RemoteAccessApps.PACKAGES.contains("com.rustdesk.rustdesk"))
        assertTrue(RemoteAccessApps.PACKAGES.contains("com.sand.airdroid"))
        assertTrue(RemoteAccessApps.PACKAGES.contains("com.zoho.assist"))
        assertTrue(RemoteAccessApps.PACKAGES.contains("com.google.chromeremotedesktop"))
    }

    @Test
    fun everyPackageHasAReadableLabel() {
        RemoteAccessApps.PACKAGES.forEach { pkg ->
            assertTrue("missing label for $pkg", RemoteAccessApps.label(pkg) != pkg)
        }
    }

    @Test
    fun anUnknownPackageFallsBackToItsOwnName() {
        assertEquals("com.example.thing", RemoteAccessApps.label("com.example.thing"))
    }

    @Test
    fun bothUsedJustNowIsUrgent() {
        assertTrue(
            RemoteAccessApps.isUrgentOverlap(
                remoteLastUsed = now - 5 * minute,
                bankLastUsed = now - 2 * minute,
                now = now
            )
        )
    }

    @Test
    fun anOldRemoteUseIsNotUrgent() {
        assertFalse(
            RemoteAccessApps.isUrgentOverlap(
                remoteLastUsed = now - 45 * minute,
                bankLastUsed = now - 2 * minute,
                now = now
            )
        )
    }

    @Test
    fun anOldBankUseIsNotUrgent() {
        assertFalse(
            RemoteAccessApps.isUrgentOverlap(
                remoteLastUsed = now - 5 * minute,
                bankLastUsed = now - 45 * minute,
                now = now
            )
        )
    }

    @Test
    fun exactlyThirtyMinutesStillCounts() {
        assertTrue(
            RemoteAccessApps.isUrgentOverlap(
                remoteLastUsed = now - 30 * minute,
                bankLastUsed = now - 30 * minute,
                now = now
            )
        )
    }

    @Test
    fun withoutUsageAccessThereIsNoOverlap() {
        assertFalse(RemoteAccessApps.isUrgentOverlap(null, null, now))
        assertFalse(RemoteAccessApps.isUrgentOverlap(now, null, now))
        assertFalse(RemoteAccessApps.isUrgentOverlap(null, now, now))
    }

    @Test
    fun aFutureTimestampIsIgnored() {
        assertFalse(
            RemoteAccessApps.isUrgentOverlap(
                remoteLastUsed = now + minute,
                bankLastUsed = now,
                now = now
            )
        )
    }

    @Test
    fun theBankingListCoversTheCommonIndianApps() {
        assertTrue(RemoteAccessApps.BANKING_UPI.contains("com.phonepe.app"))
        assertTrue(RemoteAccessApps.BANKING_UPI.contains("net.one97.paytm"))
        assertTrue(RemoteAccessApps.BANKING_UPI.contains("com.sbi.lotusintouch"))
    }
}
