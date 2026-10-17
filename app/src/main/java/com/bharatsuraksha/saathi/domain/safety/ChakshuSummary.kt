package com.bharatsuraksha.saathi.domain.safety

/**
 * Builds a copy-ready summary for a Sanchar Saathi (Chakshu) report. The app
 * only prepares the text and opens the portal: the user submits it themselves,
 * and nothing is ever sent from here. Pure logic, unit tested.
 */
object ChakshuSummary {

    const val MEDIUM_CALL = "call"
    const val MEDIUM_SMS = "sms"
    const val MEDIUM_WHATSAPP = "whatsapp"
    const val MEDIUM_OTHER = "other"

    data class Report(
        val number: String,
        val dateText: String,
        val timeText: String,
        val medium: String,
        val category: String,
        val hasScreenshot: Boolean = false,
        val notes: String = ""
    )

    fun isComplete(report: Report): Boolean =
        report.number.isNotBlank() &&
            report.dateText.isNotBlank() &&
            report.timeText.isNotBlank() &&
            report.category.isNotBlank()

    fun build(report: Report, hindi: Boolean): String {
        val mediumText = when (report.medium) {
            MEDIUM_CALL -> if (hindi) "कॉल" else "Call"
            MEDIUM_SMS -> if (hindi) "SMS" else "SMS"
            MEDIUM_WHATSAPP -> "WhatsApp"
            else -> if (hindi) "अन्य" else "Other"
        }
        return if (hindi) {
            buildString {
                appendLine("संचार साथी (चक्षु) शिकायत के लिए जानकारी")
                appendLine()
                appendLine("संदिग्ध नंबर: ${report.number}")
                appendLine("तारीख: ${report.dateText}")
                appendLine("समय: ${report.timeText}")
                appendLine("माध्यम: $mediumText")
                appendLine("ठगी का प्रकार: ${report.category}")
                appendLine("स्क्रीनशॉट: ${if (report.hasScreenshot) "है" else "नहीं"}")
                if (report.notes.isNotBlank()) appendLine("अतिरिक्त जानकारी: ${report.notes}")
                appendLine()
                appendLine("नोट: यह जानकारी मैंने स्वयं संचार साथी पोर्टल पर भरी है। ऐप ने कुछ नहीं भेजा।")
            }
        } else {
            buildString {
                appendLine("Details for a Sanchar Saathi (Chakshu) report")
                appendLine()
                appendLine("Suspicious number: ${report.number}")
                appendLine("Date: ${report.dateText}")
                appendLine("Time: ${report.timeText}")
                appendLine("Medium: $mediumText")
                appendLine("Fraud type: ${report.category}")
                appendLine("Screenshot: ${if (report.hasScreenshot) "yes" else "no"}")
                if (report.notes.isNotBlank()) appendLine("Extra notes: ${report.notes}")
                appendLine()
                appendLine("Note: I filled this in myself on the Sanchar Saathi portal. The app sent nothing.")
            }
        }.trim()
    }
}
