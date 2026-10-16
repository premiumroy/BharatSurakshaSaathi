package com.bharatsuraksha.saathi.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Optional online explanation layer (Phase 4). Sends ONLY the message text the
 * user explicitly chose, and ONLY when the user has switched online mode on and
 * confirmed. The app talks to the user's own backend proxy — never to a model
 * provider directly, and never with an embedded API key.
 *
 * Default: never called.
 */
@Singleton
class ExplanationClient @Inject constructor() {

    /**
     * @return the plain-language explanation, or null on any failure.
     */
    suspend fun fetchExplanation(endpoint: String, message: String): String? =
        withContext(Dispatchers.IO) {
            if (endpoint.isBlank()) return@withContext null
            runCatching {
                val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 10_000
                    readTimeout = 20_000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    setRequestProperty("Accept", "application/json")
                }
                val body = JSONObject()
                    .put("message", message)
                    .put("language", "en")
                    .toString()
                connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

                if (connection.responseCode !in 200..299) {
                    connection.disconnect()
                    return@runCatching null
                }
                val text = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()
                JSONObject(text).optString("explanation").ifBlank { null }
            }.getOrNull()
        }
}
