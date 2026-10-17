package com.bharatsuraksha.saathi.domain.agent

/**
 * Plain-language guidance for the phone makers whose battery savers stop
 * background apps. Pure data plus a lookup, so it is unit tested.
 */
object BatteryGuidance {

    data class Vendor(
        val key: String,
        val label: String,
        /** Tokens matched against Build.MANUFACTURER and Build.BRAND. */
        val tokens: List<String>,
        /** String-resource keys, one per step. */
        val stepKeys: List<String>
    )

    const val XIAOMI = "xiaomi"
    const val OPPO = "oppo"
    const val VIVO = "vivo"
    const val SAMSUNG = "samsung"
    const val OTHER = "other"

    val VENDORS: List<Vendor> = listOf(
        Vendor(
            key = XIAOMI,
            label = "Xiaomi / Redmi / POCO",
            tokens = listOf("xiaomi", "redmi", "poco"),
            stepKeys = listOf(
                "battery_xiaomi_1", "battery_xiaomi_2", "battery_xiaomi_3", "battery_xiaomi_4"
            )
        ),
        Vendor(
            key = OPPO,
            label = "Oppo / Realme / OnePlus",
            tokens = listOf("oppo", "realme", "oneplus"),
            stepKeys = listOf(
                "battery_oppo_1", "battery_oppo_2", "battery_oppo_3"
            )
        ),
        Vendor(
            key = VIVO,
            label = "Vivo / iQOO",
            tokens = listOf("vivo", "iqoo"),
            stepKeys = listOf(
                "battery_vivo_1", "battery_vivo_2", "battery_vivo_3"
            )
        ),
        Vendor(
            key = SAMSUNG,
            label = "Samsung",
            tokens = listOf("samsung"),
            stepKeys = listOf(
                "battery_samsung_1", "battery_samsung_2", "battery_samsung_3"
            )
        ),
        Vendor(
            key = OTHER,
            label = "Other phones",
            tokens = emptyList(),
            stepKeys = listOf(
                "battery_other_1", "battery_other_2", "battery_other_3"
            )
        )
    )

    /** Which guidance to show for this phone. Falls back to the generic list. */
    fun vendorFor(manufacturer: String?, brand: String?): Vendor {
        val haystack = ((manufacturer ?: "") + " " + (brand ?: "")).lowercase()
        return VENDORS.firstOrNull { vendor ->
            vendor.tokens.isNotEmpty() && vendor.tokens.any { haystack.contains(it) }
        } ?: VENDORS.first { it.key == OTHER }
    }
}
