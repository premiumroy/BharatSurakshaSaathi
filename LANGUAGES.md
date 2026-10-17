# Languages

Saathi is written in Hindi first, with English alongside. Everything else is a
translation, and every translation that a native speaker has not yet checked
carries a **बीटा / Beta** badge in the picker.

## Status legend

| Status | Meaning |
|---|---|
| **VERIFIED** | Checked by a native speaker. Hindi and English only. |
| **BETA** | Our best attempt. Shown with a beta badge and a report link. |

## Detection legend

| Level | Meaning |
|---|---|
| **STRONG** | Full offline rule set, tuned and tested. |
| **BASIC** | A starter detection pack exists for the common scams. |
| **UI ONLY** | Interface translated; warnings fall back to Hindi, then English. |

## The languages

| Language | Tag | Script | Status | Detection | Text shipped |
|---|---|---|---|---|---|
| Hindi | hi | Devanagari | VERIFIED | STRONG | yes |
| English | en | Latin | VERIFIED | STRONG | yes |
| Marathi | mr | Devanagari | BETA | BASIC | see below |
| Bengali | bn | Bengali | BETA | BASIC | see below |
| Tamil | ta | Tamil | BETA | BASIC | see below |
| Telugu | te | Telugu | BETA | BASIC | see below |
| Gujarati | gu | Gujarati | BETA | BASIC | see below |
| Punjabi | pa | Gurmukhi | BETA | BASIC | see below |
| Kannada | kn | Kannada | BETA | BASIC | see below |
| Malayalam | ml | Malayalam | BETA | BASIC | see below |
| Odia | or | Odia | BETA | UI ONLY | pending |
| Assamese | as | Bengali | BETA | UI ONLY | pending |
| Urdu | ur | Arabic (RTL) | BETA | UI ONLY | pending |
| Maithili | mai | Devanagari | BETA | UI ONLY | pending |
| Konkani | kok | Devanagari | BETA | UI ONLY | pending |
| Nepali | ne | Devanagari | BETA | UI ONLY | pending |
| Sindhi | sd | Arabic (RTL) | BETA | UI ONLY | pending |
| Dogri | doi | Devanagari | BETA | UI ONLY | pending |
| Kashmiri | ks | Arabic (RTL) | BETA | UI ONLY | pending |
| Santali | sat | Ol Chiki | BETA | UI ONLY | pending |
| Manipuri | mni | Meitei Mayek | BETA | UI ONLY | pending |
| Bodo | brx | Devanagari | BETA | UI ONLY | pending |
| Garhwali | gbm | Devanagari | BETA | UI ONLY | pending |
| Kumaoni | kfy | Devanagari | BETA | UI ONLY | pending |
| Bhojpuri | bho | Devanagari | BETA | UI ONLY | pending |
| Haryanvi | bgc | Devanagari | BETA | UI ONLY | pending |
| Rajasthani | raj | Devanagari | BETA | UI ONLY | pending |
| Marwari | mwr | Devanagari | BETA | UI ONLY | pending |
| Chhattisgarhi | hne | Devanagari | BETA | UI ONLY | pending |
| Awadhi | awa | Devanagari | BETA | UI ONLY | pending |
| Magahi | mag | Devanagari | BETA | UI ONLY | pending |
| Tulu | tcy | Kannada | BETA | UI ONLY | pending |

A language appears under **Fully translated** in the picker only when its
`strings.xml` is complete. The rest are declared, shown in their own script, and
fall back to Hindi until someone finishes them.

## How to fix a translation, by hand

You do not need to touch any Kotlin. Every word lives in one plain file.

1. Copy the English source: `app/src/main/res/values/strings.xml`.
2. Paste it into a new folder named `values-b+<tag>`, for example
   `app/src/main/res/values-b+ta/strings.xml` for Tamil.
3. Change only the text between `>` and `</string>`. Leave the
   `name="..."` part exactly as it is.
4. Keep every `%1$s`, `%s` and `%d` exactly where it was. The tests fail if one
   is missing or added.
5. Never change `1930`, `cybercrime.gov.in` or `sancharsaathi.gov.in`. A test
   fails if they are altered.
6. If the string has an apostrophe, write it as `\'`. Android needs that.
7. Run `./gradlew :app:testDebugUnitTest`. `TranslationCompletenessTest` checks
   that your file has every key, the same placeholders, and the untouched phone
   numbers. When it passes, the language appears in the picker.

To move a language from **UI ONLY** to **BASIC**, add a detection pack at
`app/src/main/assets/rules/<tag>.json` and change its `detection` in
`LanguageRegistry.kt`.

## Safety of translations

Scam warnings and the "what to do now" steps are safety-critical. Keep the
meaning of the Hindi and English source exact, keep sentences short and plain,
and never invent a helpline number. When a language is missing a key, the app
shows Hindi, then English, rather than an empty line.
