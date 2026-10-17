package com.bharatsuraksha.saathi.ui.i18n

/** How far a language has been checked by a human. */
enum class LanguageStatus { VERIFIED, BETA }

/** How well the offline detection packs cover a language. */
enum class Detection { STRONG, BASIC, UI_ONLY }

/**
 * One language the app can show. [hasStrings] is true only when a complete
 * strings.xml actually ships for it, so the picker never promises a language it
 * cannot draw.
 */
data class AppLanguage(
    val tag: String,
    val nativeName: String,
    val englishName: String,
    val status: LanguageStatus,
    val detection: Detection,
    val script: String,
    val rtl: Boolean = false,
    val hasStrings: Boolean = false
)

/**
 * Every language Saathi knows about.
 *
 * Hindi and English are the verified source. The other Indian languages are
 * BETA: the wording is our best attempt, and every beta row carries a badge and
 * a way to report a bad translation. Regional and dialect languages are BETA by
 * definition, and they fall back to Hindi, then English, for anything missing.
 */
object LanguageRegistry {

    val ALL: List<AppLanguage> = listOf(
        // ---- verified source ----
        AppLanguage("hi", "हिन्दी", "Hindi", LanguageStatus.VERIFIED, Detection.STRONG, "Devanagari", hasStrings = true),
        AppLanguage("en", "English", "English", LanguageStatus.VERIFIED, Detection.STRONG, "Latin", hasStrings = true),

        // ---- Indian languages with detection packs (BASIC) ----
        AppLanguage("mr", "मराठी", "Marathi", LanguageStatus.BETA, Detection.BASIC, "Devanagari", hasStrings = true),
        AppLanguage("bn", "বাংলা", "Bengali", LanguageStatus.BETA, Detection.BASIC, "Bengali"),
        AppLanguage("ta", "தமிழ்", "Tamil", LanguageStatus.BETA, Detection.BASIC, "Tamil"),
        AppLanguage("te", "తెలుగు", "Telugu", LanguageStatus.BETA, Detection.BASIC, "Telugu"),
        AppLanguage("gu", "ગુજરાતી", "Gujarati", LanguageStatus.BETA, Detection.BASIC, "Gujarati"),
        AppLanguage("pa", "ਪੰਜਾਬੀ", "Punjabi", LanguageStatus.BETA, Detection.BASIC, "Gurmukhi"),
        AppLanguage("kn", "ಕನ್ನಡ", "Kannada", LanguageStatus.BETA, Detection.BASIC, "Kannada"),
        AppLanguage("ml", "മലയാളം", "Malayalam", LanguageStatus.BETA, Detection.BASIC, "Malayalam"),

        // ---- Indian languages, UI only for now ----
        AppLanguage("or", "ଓଡ଼ିଆ", "Odia", LanguageStatus.BETA, Detection.UI_ONLY, "Odia"),
        AppLanguage("as", "অসমীয়া", "Assamese", LanguageStatus.BETA, Detection.UI_ONLY, "Bengali"),
        AppLanguage("ur", "اردو", "Urdu", LanguageStatus.BETA, Detection.UI_ONLY, "Arabic", rtl = true),
        AppLanguage("mai", "मैथिली", "Maithili", LanguageStatus.BETA, Detection.UI_ONLY, "Devanagari"),
        AppLanguage("kok", "कोंकणी", "Konkani", LanguageStatus.BETA, Detection.UI_ONLY, "Devanagari"),
        AppLanguage("ne", "नेपाली", "Nepali", LanguageStatus.BETA, Detection.UI_ONLY, "Devanagari"),
        AppLanguage("sd", "سنڌي", "Sindhi", LanguageStatus.BETA, Detection.UI_ONLY, "Arabic", rtl = true),
        AppLanguage("doi", "डोगरी", "Dogri", LanguageStatus.BETA, Detection.UI_ONLY, "Devanagari"),
        AppLanguage("ks", "کٲشُر", "Kashmiri", LanguageStatus.BETA, Detection.UI_ONLY, "Arabic", rtl = true),
        AppLanguage("sat", "ᱥᱟᱱᱛᱟᱲᱤ", "Santali", LanguageStatus.BETA, Detection.UI_ONLY, "Ol Chiki"),
        AppLanguage("mni", "ꯃꯤꯇꯩ ꯂꯣꯟ", "Manipuri", LanguageStatus.BETA, Detection.UI_ONLY, "Meitei Mayek"),
        AppLanguage("brx", "बड़ो", "Bodo", LanguageStatus.BETA, Detection.UI_ONLY, "Devanagari"),

        // ---- regional and dialect languages, always BETA ----
        AppLanguage("gbm", "गढ़वळि", "Garhwali", LanguageStatus.BETA, Detection.UI_ONLY, "Devanagari"),
        AppLanguage("kfy", "कुमाऊँनी", "Kumaoni", LanguageStatus.BETA, Detection.UI_ONLY, "Devanagari"),
        AppLanguage("bho", "भोजपुरी", "Bhojpuri", LanguageStatus.BETA, Detection.UI_ONLY, "Devanagari"),
        AppLanguage("bgc", "हरियाणवी", "Haryanvi", LanguageStatus.BETA, Detection.UI_ONLY, "Devanagari"),
        AppLanguage("raj", "राजस्थानी", "Rajasthani", LanguageStatus.BETA, Detection.UI_ONLY, "Devanagari"),
        AppLanguage("mwr", "मारवाड़ी", "Marwari", LanguageStatus.BETA, Detection.UI_ONLY, "Devanagari"),
        AppLanguage("hne", "छत्तीसगढ़ी", "Chhattisgarhi", LanguageStatus.BETA, Detection.UI_ONLY, "Devanagari"),
        AppLanguage("awa", "अवधी", "Awadhi", LanguageStatus.BETA, Detection.UI_ONLY, "Devanagari"),
        AppLanguage("mag", "मगही", "Magahi", LanguageStatus.BETA, Detection.UI_ONLY, "Devanagari"),
        AppLanguage("tcy", "ತುಳು", "Tulu", LanguageStatus.BETA, Detection.UI_ONLY, "Kannada")
    )

    fun byTag(tag: String): AppLanguage? =
        ALL.firstOrNull { it.tag.equals(tag, ignoreCase = true) }

    /** Languages with a complete strings.xml on board. */
    val translated: List<AppLanguage> get() = ALL.filter { it.hasStrings }

    /** Languages declared but not translated yet. */
    val pending: List<AppLanguage> get() = ALL.filterNot { it.hasStrings }

    fun search(query: String): List<AppLanguage> {
        val q = query.trim()
        if (q.isEmpty()) return ALL
        return ALL.filter {
            it.nativeName.contains(q, ignoreCase = true) ||
                it.englishName.contains(q, ignoreCase = true) ||
                it.tag.contains(q, ignoreCase = true) ||
                it.script.contains(q, ignoreCase = true)
        }
    }
}
