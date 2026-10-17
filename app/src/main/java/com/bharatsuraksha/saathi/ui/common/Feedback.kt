package com.bharatsuraksha.saathi.ui.common

import android.content.Context
import android.content.Intent
import com.bharatsuraksha.saathi.R

/**
 * "यह नतीजा गलत लगा / Report a wrong result".
 *
 * Nothing happens until the user taps: this only opens the share sheet with the
 * message text already in it. The app never uploads anything by itself, and the
 * user can edit or delete the text before sending.
 */
fun shareWrongResult(context: Context, message: String) {
    val body = context.getString(R.string.report_wrong_result_body, message.take(2000))
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.report_wrong_result_subject))
        putExtra(Intent.EXTRA_TEXT, body)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching {
        context.startActivity(
            Intent.createChooser(intent, context.getString(R.string.report_wrong_result))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
