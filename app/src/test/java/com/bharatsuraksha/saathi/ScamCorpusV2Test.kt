package com.bharatsuraksha.saathi

import com.bharatsuraksha.saathi.domain.model.Verdict
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v2 regression suite: runs the 130+ labelled corpus through the real engine and
 * asserts the promised quality bar — zero false positives on genuine messages,
 * every scam flagged, every "strong" scam Dangerous — and reports precision/recall.
 */
class ScamCorpusV2Test {

    private val engine = TestRules.engine

    private data class Tally(val tp: Int, val fp: Int, val tn: Int, val fn: Int) {
        val precision: Double get() = if (tp + fp == 0) 1.0 else tp.toDouble() / (tp + fp)
        val recall: Double get() = if (tp + fn == 0) 1.0 else tp.toDouble() / (tp + fn)
    }

    private fun tally(): Tally {
        var tp = 0; var fp = 0; var tn = 0; var fn = 0
        for (item in ScamCorpusV2.all) {
            val flagged = engine.analyze(item.text).verdict != Verdict.SAFE
            val isScam = item.expect != Expect.SAFE
            when {
                isScam && flagged -> tp++
                isScam -> fn++
                flagged -> fp++
                else -> tn++
            }
        }
        return Tally(tp, fp, tn, fn)
    }

    @Test
    fun corpusIsBigEnough() {
        assertTrue("corpus too small: ${ScamCorpusV2.all.size}", ScamCorpusV2.all.size >= 120)
    }

    @Test
    fun corpusCoversBothClasses() {
        val genuine = ScamCorpusV2.all.count { it.expect == Expect.SAFE }
        val scam = ScamCorpusV2.all.count { it.expect != Expect.SAFE }
        assertTrue("genuine=$genuine", genuine >= 60)
        assertTrue("scam=$scam", scam >= 60)
    }

    @Test
    fun zeroFalsePositivesOnGenuineMessages() {
        val flagged = ScamCorpusV2.all
            .filter { it.expect == Expect.SAFE }
            .map { it to engine.analyze(it.text) }
            .filter { it.second.verdict != Verdict.SAFE }
            .map { "\"${it.first.text}\" -> ${it.second.verdict} ${it.second.score} ${it.second.matchedRuleIds}" }
        assertTrue("Genuine messages wrongly flagged:\n${flagged.joinToString("\n")}", flagged.isEmpty())
    }

    @Test
    fun everyScamIsFlagged() {
        val missed = ScamCorpusV2.all
            .filter { it.expect != Expect.SAFE }
            .map { it to engine.analyze(it.text) }
            .filter { it.second.verdict == Verdict.SAFE }
            .map { "\"${it.first.text}\" (score ${it.second.score})" }
        assertTrue("Scams called SAFE:\n${missed.joinToString("\n")}", missed.isEmpty())
    }

    @Test
    fun strongScamsAreDangerous() {
        val weak = ScamCorpusV2.all
            .filter { it.expect == Expect.DANGER }
            .map { it to engine.analyze(it.text) }
            .filter { it.second.verdict != Verdict.DANGEROUS }
            .map { "\"${it.first.text}\" -> ${it.second.verdict} (score ${it.second.score})" }
        assertTrue("Strong scams not DANGEROUS:\n${weak.joinToString("\n")}", weak.isEmpty())
    }

    @Test
    fun precisionAndRecallMeetTheBar() {
        val t = tally()
        println("ScamCorpusV2 — TP=${t.tp} FP=${t.fp} TN=${t.tn} FN=${t.fn}")
        println("ScamCorpusV2 — precision=${t.precision} recall=${t.recall}")
        assertTrue("precision=${t.precision}", t.precision >= 0.95)
        assertTrue("recall=${t.recall}", t.recall >= 0.95)
    }
}
