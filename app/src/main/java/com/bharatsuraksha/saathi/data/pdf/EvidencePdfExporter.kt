package com.bharatsuraksha.saathi.data.pdf

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.bharatsuraksha.saathi.data.db.EvidenceEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Writes the user's saved evidence to a simple, printable PDF in the app's own
 * files directory. Nothing leaves the device unless the user shares the file.
 */
@Singleton
class EvidencePdfExporter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    fun export(items: List<EvidenceEntity>): File {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842

        val title = Paint().apply {
            textSize = 20f
            color = Color.BLACK
            isFakeBoldText = true
        }
        val heading = Paint().apply {
            textSize = 14f
            color = Color.BLACK
            isFakeBoldText = true
        }
        val body = Paint().apply {
            textSize = 12f
            color = Color.DKGRAY
        }

        var pageNumber = 1
        var page = document.startPage(
            PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        )
        var canvas = page.canvas
        var y = 60f

        canvas.drawText("Bharat Suraksha Saathi — scam evidence report", 40f, y, title)
        y += 24f
        canvas.drawText("Generated: ${dateFormat.format(Date())}", 40f, y, body)
        y += 30f

        fun newPageIfNeeded(needed: Float) {
            if (y + needed > pageHeight - 50f) {
                document.finishPage(page)
                pageNumber += 1
                page = document.startPage(
                    PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                )
                canvas = page.canvas
                y = 60f
            }
        }

        items.forEachIndexed { index, item ->
            newPageIfNeeded(140f)
            canvas.drawText("${index + 1}. ${item.title}", 40f, y, heading)
            y += 20f
            listOf(
                "Amount involved: ${item.amount.ifBlank { "—" }}",
                "When: ${item.whenText.ifBlank { "—" }}",
                "Numbers involved: ${item.numbers.ifBlank { "—" }}",
                "Notes: ${item.notes.ifBlank { "—" }}",
                "Screenshot saved: ${if (item.screenshotPath.isNullOrBlank()) "no" else "yes"}",
                "Saved on: ${dateFormat.format(Date(item.createdAt))}"
            ).forEach { line ->
                wrap(line, body, (pageWidth - 80).toFloat()).forEach { wrapped ->
                    newPageIfNeeded(18f)
                    canvas.drawText(wrapped, 48f, y, body)
                    y += 16f
                }
            }
            y += 12f
        }

        document.finishPage(page)

        val dir = File(context.filesDir, "exports").apply { mkdirs() }
        val file = File(dir, "scam_evidence_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }

    private fun wrap(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(' ')
        val lines = mutableListOf<String>()
        val current = StringBuilder()
        for (word in words) {
            val candidate = if (current.isEmpty()) word else "$current $word"
            if (paint.measureText(candidate) <= maxWidth) {
                current.clear().append(candidate)
            } else {
                if (current.isNotEmpty()) lines.add(current.toString())
                current.clear().append(word)
            }
        }
        if (current.isNotEmpty()) lines.add(current.toString())
        return lines
    }
}
