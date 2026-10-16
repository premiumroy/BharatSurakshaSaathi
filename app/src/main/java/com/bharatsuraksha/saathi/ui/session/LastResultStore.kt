package com.bharatsuraksha.saathi.ui.session

import com.bharatsuraksha.saathi.domain.model.AnalysisResult

/** Holds the most recent analysis so the Result screen can render it. */
object LastResultStore {
    var message: String = ""
    var result: AnalysisResult? = null
}
