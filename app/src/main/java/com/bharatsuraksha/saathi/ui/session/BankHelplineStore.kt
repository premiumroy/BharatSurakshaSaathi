package com.bharatsuraksha.saathi.ui.session

import android.content.Context
import com.bharatsuraksha.saathi.domain.safety.BankHelpline
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Bank helplines the user typed in from their own card or passbook. We ship no
 * bundled list of "official" numbers, because a wrong number is dangerous.
 * Stored on this phone only.
 */
object BankHelplineStore {

    private const val PREFS = "saathi_helplines"
    private const val KEY = "entries"
    private const val SEP = "\u0001"

    private val _entries = MutableStateFlow<List<BankHelpline.Entry>>(emptyList())
    val entries: StateFlow<List<BankHelpline.Entry>> = _entries

    fun load(context: Context) {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "").orEmpty()
        _entries.value = raw.split("\n")
            .mapNotNull { line ->
                if (line.isBlank()) return@mapNotNull null
                val parts = line.split(SEP)
                if (parts.size != 2) return@mapNotNull null
                BankHelpline.Entry(parts[0], parts[1])
            }
    }

    fun add(context: Context, entry: BankHelpline.Entry) {
        val clean = BankHelpline.Entry(entry.bank.trim(), BankHelpline.normalise(entry.number))
        if (BankHelpline.validate(clean) != null) return
        _entries.value = (_entries.value + clean).distinctBy { it.bank.lowercase() + it.number }
        persist(context)
    }

    fun remove(context: Context, entry: BankHelpline.Entry) {
        _entries.value = _entries.value.filterNot { it == entry }
        persist(context)
    }

    fun clear(context: Context) {
        _entries.value = emptyList()
        persist(context)
    }

    private fun persist(context: Context) {
        val raw = _entries.value.joinToString("\n") { it.bank + SEP + it.number }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, raw).apply()
    }
}
