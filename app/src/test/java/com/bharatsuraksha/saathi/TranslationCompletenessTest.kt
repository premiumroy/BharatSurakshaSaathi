package com.bharatsuraksha.saathi

import com.bharatsuraksha.saathi.ui.i18n.LanguageRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Guards the translations. Every shipped language file must carry exactly the
 * same keys as the English source, with exactly the same placeholders, and must
 * leave phone numbers and web addresses untouched.
 *
 * A language only ships once it passes this, so the app can never show a missing
 * key or a broken "%1$s" in the middle of a warning.
 */
class TranslationCompletenessTest {

    private val source = readStrings(sourceFile())

    @Test
    fun everyLanguageFileHasExactlyTheSameKeys() {
        val sourceKeys = source.keys
        shippedTags().forEach { tag ->
            val strings = readStrings(fileFor(tag))
            val missing = sourceKeys - strings.keys
            val extra = strings.keys - sourceKeys
            assertTrue("$tag is missing ${missing.size} keys: ${missing.take(10)}", missing.isEmpty())
            assertTrue("$tag has ${extra.size} unknown keys: ${extra.take(10)}", extra.isEmpty())
        }
    }

    @Test
    fun everyPlaceholderIsPreserved() {
        shippedTags().forEach { tag ->
            val strings = readStrings(fileFor(tag))
            source.forEach { (key, value) ->
                val expected = placeholders(value)
                val actual = placeholders(strings[key].orEmpty())
                assertEquals("$tag / $key placeholders differ", expected, actual)
            }
        }
    }

    @Test
    fun safetyCriticalValuesAreNeverTranslated() {
        // These must survive translation exactly, in every language.
        val mustSurvive = listOf("1930", "cybercrime.gov.in", "sancharsaathi.gov.in")
        shippedTags().forEach { tag ->
            val strings = readStrings(fileFor(tag))
            source.forEach { (key, value) ->
                mustSurvive.forEach { token ->
                    if (value.contains(token)) {
                        assertTrue(
                            "$tag / $key lost the token '$token'",
                            strings[key].orEmpty().contains(token)
                        )
                    }
                }
            }
        }
    }

    @Test
    fun everyShippedLanguageIsDeclaredInTheRegistry() {
        val declared = LanguageRegistry.ALL.map { it.tag }.toSet()
        shippedTags().forEach { tag ->
            assertTrue("$tag ships a strings.xml but is not declared in LanguageRegistry", tag in declared)
        }
    }

    @Test
    fun everyLanguageClaimingStringsActuallyHasAFolder() {
        val shipped = shippedTags().toSet()
        LanguageRegistry.ALL.filter { it.hasStrings }.forEach { language ->
            assertTrue(
                "${language.tag} claims hasStrings but no resource folder ships",
                language.tag in shipped
            )
        }
    }

    @Test
    fun theRegistryHasNoDuplicateTags() {
        val tags = LanguageRegistry.ALL.map { it.tag }
        assertEquals(tags.size, tags.toSet().size)
    }

    // ------------------------------------------------------------------ helpers

    /**
     * BCP-47 tags that ship a complete strings.xml. English lives in the default
     * `values/` folder, so it is added explicitly rather than found by name.
     */
    private fun shippedTags(): List<String> {
        val folders = resDir().listFiles()
            ?.filter { it.isDirectory }
            ?.map { it.name }
            ?: emptyList()
        val fromFolders = folders
            .filter { it == "values-hi" || it.startsWith("values-b+") }
            .map { it.removePrefix("values-b+").removePrefix("values-") }
        return (fromFolders + "en").distinct().sorted()
    }

    private fun sourceFile(): File = File(resDir(), "values/strings.xml")

    /** Hindi ships as values-hi; newer languages ship as values-b+<tag>. */
    private fun fileFor(tag: String): File {
        if (tag == "en") return sourceFile()
        val short = File(resDir(), "values-$tag/strings.xml")
        if (short.isFile) return short
        return File(resDir(), "values-b+$tag/strings.xml")
    }

    private fun resDir(): File {
        val candidates = listOf(
            File("src/main/res"),
            File("app/src/main/res"),
            File("../app/src/main/res")
        )
        return candidates.firstOrNull { it.isDirectory }
            ?: error("could not find the res directory from ${File(".").absolutePath}")
    }

    private fun readStrings(file: File): Map<String, String> {
        if (!file.isFile) return emptyMap()
        val text = file.readText()
        val regex = Regex("""<string name="([^"]+)">(.*?)</string>""", RegexOption.DOT_MATCHES_ALL)
        return regex.findAll(text).associate { it.groupValues[1] to it.groupValues[2] }
    }

    /** The set of format specifiers in a value, e.g. ["%1\$s", "%d"]. */
    private fun placeholders(value: String): List<String> =
        Regex("""%(\d+\$)?[sd]""").findAll(value).map { it.value }.sorted().toList()
}
