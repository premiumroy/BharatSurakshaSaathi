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

    private val source = readStrings(File(resDir(), "values/strings.xml"))

    @Test
    fun everyLanguageFileHasExactlyTheSameKeys() {
        val sourceKeys = source.keys
        shippedLocaleFolders().forEach { folder ->
            val strings = readStrings(File(resDir(), "$folder/strings.xml"))
            val missing = sourceKeys - strings.keys
            val extra = strings.keys - sourceKeys
            assertTrue("$folder is missing ${missing.size} keys: ${missing.take(10)}", missing.isEmpty())
            assertTrue("$folder has ${extra.size} unknown keys: ${extra.take(10)}", extra.isEmpty())
        }
    }

    @Test
    fun everyPlaceholderIsPreserved() {
        shippedLocaleFolders().forEach { folder ->
            val strings = readStrings(File(resDir(), "$folder/strings.xml"))
            source.forEach { (key, value) ->
                val expected = placeholders(value)
                val actual = placeholders(strings[key].orEmpty())
                assertEquals("$folder / $key placeholders differ", expected, actual)
            }
        }
    }

    @Test
    fun safetyCriticalValuesAreNeverTranslated() {
        // These must survive translation exactly, in every language.
        val mustSurvive = listOf("1930", "cybercrime.gov.in", "sancharsaathi.gov.in")
        shippedLocaleFolders().forEach { folder ->
            val strings = readStrings(File(resDir(), "$folder/strings.xml"))
            source.forEach { (key, value) ->
                mustSurvive.forEach { token ->
                    if (value.contains(token)) {
                        assertTrue(
                            "$folder / $key lost the token '$token'",
                            strings[key].orEmpty().contains(token)
                        )
                    }
                }
            }
        }
    }

    @Test
    fun everyShippedFolderIsDeclaredInTheRegistry() {
        val declared = LanguageRegistry.ALL.map { it.tag }.toSet()
        shippedLocaleFolders().forEach { folder ->
            val tag = folder.removePrefix("values-b+").removePrefix("values-")
            assertTrue("$folder is not declared in LanguageRegistry", tag in declared)
        }
    }

    @Test
    fun everyLanguageClaimingStringsActuallyHasAFolder() {
        val shipped = shippedLocaleFolders().map { it.removePrefix("values-b+").removePrefix("values-") }.toSet()
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

    /** Every locale folder that ships a full strings.xml (hi, en live in values*). */
    private fun shippedLocaleFolders(): List<String> {
        val res = resDir()
        return res.listFiles()
            ?.filter { it.isDirectory }
            ?.map { it.name }
            ?.filter { it == "values-hi" || it.startsWith("values-b+") }
            ?.sorted()
            ?: emptyList()
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
