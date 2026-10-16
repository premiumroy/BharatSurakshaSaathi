package com.bharatsuraksha.saathi.data.rules

import android.content.Context
import com.bharatsuraksha.saathi.domain.engine.RulesJson
import com.bharatsuraksha.saathi.domain.engine.RulesRepository
import com.bharatsuraksha.saathi.domain.model.RuleSet
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads the scam rules from app/src/main/assets/scam_rules.json and merges the
 * bundled official-domain list from assets/official_domains.json.
 * Parsed once and cached. Replacing either asset updates detection with no code change.
 */
@Singleton
class AssetRulesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : RulesRepository {

    private val cached: RuleSet by lazy {
        val base = RulesJson.parse(readAsset(RULES_ASSET))
        runCatching {
            RulesJson.mergeOfficialDomains(base, readAsset(DOMAINS_ASSET))
        }.getOrDefault(base)
    }

    override fun loadRules(): RuleSet = cached

    private fun readAsset(name: String): String =
        context.assets.open(name).bufferedReader().use { it.readText() }

    companion object {
        const val RULES_ASSET = "scam_rules.json"
        const val DOMAINS_ASSET = "official_domains.json"
    }
}
