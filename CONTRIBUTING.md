# Contributing to Suraksha Saathi

Thanks for helping. The two most useful contributions are **new scam rules** and
**translations**, and both can be made without touching Kotlin.

## Ground rules

1. Everything must stay on the device. No analytics, no uploads, no new network calls.
2. Never say the app is 100% safe. Every user-facing warning keeps the line
   `यह गारंटी नहीं है।`
3. Do not add a dependency unless there is no reasonable alternative.
4. All tests must pass before a pull request: `./gradlew :app:testDebugUnitTest`

## Add or fix a scam rule

Rules live in `app/src/main/assets/scam_rules.json`. Each entry has:

| field | meaning |
|---|---|
| `id` | a stable name, never reused |
| `family` | the scam family it belongs to |
| `weight` | how much it contributes to the score |
| `language` | `en`, `hi`, or `both` |
| `examples` | messages that should match |
| `changelog` | what changed and when |

After editing, add a labelled message to `app/src/test/java/com/bharatsuraksha/saathi/ScamCorpusV2.kt`
and run the tests. The corpus reports precision and recall, and **a rule that
fires on a genuine message will fail the build**. That guard is deliberate: a
false warning teaches people to ignore real ones.

## Translate a language

1. Copy `app/src/main/res/values/strings.xml`.
2. Paste it into `app/src/main/res/values-b+<tag>/strings.xml`, for example
   `values-b+ta` for Tamil.
3. Change only the text between `>` and `</string>`. Leave every `name="..."` alone.
4. Keep every placeholder (`%1$s`, `%s`, `%d`) exactly where it was, and never
   change `1930`, `cybercrime.gov.in` or `sancharsaathi.gov.in`.
5. Write an apostrophe as `\'`. Android requires it.
6. Run the tests. `TranslationCompletenessTest` checks the key set, the
   placeholders and the protected phone numbers.
7. Set `hasStrings = true` for that language in `ui/i18n/LanguageRegistry.kt`.

For safety-critical strings (scam warnings and the "what to do now" steps), keep
the meaning of the Hindi and English source exact, prefer short everyday words,
and never invent a helpline number.

## Reporting a wrong result

Use the "यह नतीजा गलत लगा" button in the app, or open an issue and paste the
message that was misjudged. Please say what you expected instead.

## Code style

Kotlin, four-space indent, no wildcard imports, and comments that explain *why*
rather than *what*. Screens go in `ui/`, logic goes in `domain/`, and the domain
layer must stay free of Android imports so it stays unit-testable.
