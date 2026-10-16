package com.bharatsuraksha.saathi.data.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * On-device OCR for screenshots/photos using ML Kit's bundled models (Latin +
 * Devanagari). Fully offline — the models ship inside the app.
 */
@Singleton
class OcrTextExtractor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val latinRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val devanagariRecognizer =
        TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())

    /** Returns the recognised text (Latin + Devanagari runs combined), or "" if none. */
    suspend fun extract(uri: Uri): String {
        val image = InputImage.fromFilePath(context, uri)
        val latinText = runCatching { recognize(latinRecognizer, image) }.getOrDefault("")
        val devanagariText = runCatching { recognize(devanagariRecognizer, image) }.getOrDefault("")
        return listOf(latinText, devanagariText)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .joinToString("\n")
    }

    private suspend fun recognize(
        recognizer: com.google.mlkit.vision.text.TextRecognizer,
        image: InputImage
    ): String = suspendCancellableCoroutine { cont ->
        recognizer.process(image)
            .addOnSuccessListener { result -> if (cont.isActive) cont.resume(result.text) }
            .addOnFailureListener { error -> if (cont.isActive) cont.resumeWithException(error) }
    }
}
