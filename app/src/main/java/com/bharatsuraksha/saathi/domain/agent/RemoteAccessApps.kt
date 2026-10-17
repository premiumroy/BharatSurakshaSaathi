package com.bharatsuraksha.saathi.domain.agent

/**
 * Remote-access tools that scammers use to take over a phone, and the banking /
 * UPI apps we compare them against.
 *
 * We ask Android about these exact package names only. QUERY_ALL_PACKAGES is
 * never requested, and nothing here reads any content from those apps.
 */
object RemoteAccessApps {

    /** The packages named in the phase brief. */
    val PACKAGES: List<String> = listOf(
        "com.anydesk.anydeskandroid",
        "com.teamviewer.teamviewer.market.mobile",
        "com.teamviewer.quicksupport.market",
        "com.teamviewer.remote",
        "com.rustdesk.rustdesk",
        "com.sand.airdroid",
        "com.zoho.assist",
        "com.google.chromeremotedesktop"
    )

    /** Display names, for the warning card. */
    val LABELS: Map<String, String> = mapOf(
        "com.anydesk.anydeskandroid" to "AnyDesk",
        "com.teamviewer.teamviewer.market.mobile" to "TeamViewer",
        "com.teamviewer.quicksupport.market" to "TeamViewer QuickSupport",
        "com.teamviewer.remote" to "TeamViewer Remote",
        "com.rustdesk.rustdesk" to "RustDesk",
        "com.sand.airdroid" to "AirDroid",
        "com.zoho.assist" to "Zoho Assist",
        "com.google.chromeremotedesktop" to "Chrome Remote Desktop"
    )

    /**
     * Common Indian banking and UPI apps. Used only to notice that a remote-access
     * tool and a money app were both used recently. We never read what happened in
     * either app.
     */
    val BANKING_UPI: List<String> = listOf(
        "com.sbi.lotusintouch",
        "com.sbi.SBIFreedomPlus",
        "com.csam.icici.bank.imobile",
        "com.hdfcbank.android.now",
        "com.msf.kbank.mobile",
        "net.one97.paytm",
        "com.phonepe.app",
        "com.google.android.apps.nbu.paisa.user",
        "in.amazon.mShop.android.shopping",
        "com.dreamplug.androidapp",
        "com.axis.mobile",
        "com.kotak.mahindra",
        "com.pnb.netbanking"
    )

    const val RECENT_WINDOW_MILLIS: Long = 30L * 60L * 1000L

    data class Overlap(
        val remoteAccessPackage: String?,
        val remoteUsedMillisAgo: Long?,
        val bankUsedMillisAgo: Long?,
        val urgent: Boolean
    )

    /**
     * True when a remote-access tool was used in the last 30 minutes and a banking
     * or UPI app was used in that same window. Pure, so it is unit tested.
     */
    fun isUrgentOverlap(
        remoteLastUsed: Long?,
        bankLastUsed: Long?,
        now: Long,
        windowMillis: Long = RECENT_WINDOW_MILLIS
    ): Boolean {
        if (remoteLastUsed == null || bankLastUsed == null) return false
        val remoteAgo = now - remoteLastUsed
        val bankAgo = now - bankLastUsed
        return remoteAgo in 0..windowMillis && bankAgo in 0..windowMillis
    }

    fun label(packageName: String): String = LABELS[packageName] ?: packageName
}
