package com.bharatsuraksha.saathi

import com.bharatsuraksha.saathi.domain.model.Verdict
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The main acceptance test: runs the whole corpus through the real engine.
 */
class ScamSamplesTest {

    private val engine = TestRules.engine

    @Test
    fun corpusHasAtLeast40Samples() {
        assertTrue("corpus too small: ${ScamSamples.all.size}", ScamSamples.all.size >= 40)
    }

    @Test
    fun safeSamplesAreSafe() {
        val flagged = ScamSamples.all
            .filter { it.kind == Kind.SAFE }
            .map { it to engine.analyze(it.text) }
            .filter { it.second.verdict != Verdict.SAFE }
            .map { "\"${it.first.text}\" -> ${it.second.verdict} (score ${it.second.score})" }
        assertTrue("Safe messages wrongly flagged:\n${flagged.joinToString("\n")}", flagged.isEmpty())
    }

    @Test
    fun scamSamplesAreNeverSafe() {
        val missed = ScamSamples.all
            .filter { it.kind == Kind.SCAM || it.kind == Kind.STRONG }
            .map { it to engine.analyze(it.text) }
            .filter { it.second.verdict == Verdict.SAFE }
            .map { "\"${it.first.text}\" (score ${it.second.score})" }
        assertTrue("Scams called SAFE:\n${missed.joinToString("\n")}", missed.isEmpty())
    }

    @Test
    fun strongScamsAreDangerous() {
        val weak = ScamSamples.all
            .filter { it.kind == Kind.STRONG }
            .map { it to engine.analyze(it.text) }
            .filter { it.second.verdict != Verdict.DANGEROUS }
            .map { "\"${it.first.text}\" -> ${it.second.verdict} (score ${it.second.score})" }
        assertTrue("Strong scams not DANGEROUS:\n${weak.joinToString("\n")}", weak.isEmpty())
    }
}
