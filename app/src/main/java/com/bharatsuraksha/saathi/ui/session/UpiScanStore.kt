package com.bharatsuraksha.saathi.ui.session

/** Carries the raw scanned QR payload from the scanner to the result screen. */
object UpiScanStore {
    var raw: String = ""
    fun clear() {
        raw = ""
    }
}
