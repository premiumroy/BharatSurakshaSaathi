package com.bharatsuraksha.saathi

import com.bharatsuraksha.saathi.domain.engine.RulesJson
import com.bharatsuraksha.saathi.domain.engine.ScamEngine
import com.bharatsuraksha.saathi.domain.model.RuleSet
import java.io.File

/**
 * Shared test fixture. Loads the SAME assets the app ships, so tests exercise the
 * real rules and the real official-domain list.
 * Gradle runs unit tests with the module directory as the working dir.
 */
object TestRules {

    val assetFile = File("src/main/assets/scam_rules.json")
    val domainsFile = File("src/main/assets/official_domains.json")

    val ruleSet: RuleSet by lazy {
        check(assetFile.exists()) {
            "Rules asset not found at ${assetFile.absolutePath} (cwd=${File(".").absolutePath})"
        }
        val base = RulesJson.parse(assetFile.readText())
        if (domainsFile.exists()) {
            RulesJson.mergeOfficialDomains(base, domainsFile.readText())
        } else {
            base
        }
    }

    val engine: ScamEngine by lazy { ScamEngine(ruleSet) }
}
