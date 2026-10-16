package com.bharatsuraksha.saathi.domain.engine

import com.bharatsuraksha.saathi.domain.model.OfficialDomainsFile
import com.bharatsuraksha.saathi.domain.model.RuleSet
import kotlinx.serialization.json.Json

/** Supplies the rule set to the engine. Implemented by the data layer (asset-backed). */
interface RulesRepository {
    fun loadRules(): RuleSet
}

/** Parses the rules JSON. Kept here so both the app and unit tests share one code path. */
object RulesJson {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun parse(content: String): RuleSet = json.decodeFromString(RuleSet.serializer(), content)

    /**
     * Merges the bundled official-domain list (assets/official_domains.json) into
     * the rule set, so the look-alike check compares against a full, updatable list.
     */
    fun mergeOfficialDomains(ruleSet: RuleSet, domainsJson: String): RuleSet {
        val file = json.decodeFromString(OfficialDomainsFile.serializer(), domainsJson)
        if (file.domains.isEmpty()) return ruleSet
        val merged = (ruleSet.linkRules.officialDomains + file.domains)
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .distinct()
        return ruleSet.copy(linkRules = ruleSet.linkRules.copy(officialDomains = merged))
    }
}
