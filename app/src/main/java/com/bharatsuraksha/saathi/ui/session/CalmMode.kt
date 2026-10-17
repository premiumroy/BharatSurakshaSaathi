package com.bharatsuraksha.saathi.ui.session

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * UI-layer preference. Calm mode reduces motion app-wide for people who find
 * animation distracting. Stored locally; it is a display choice, not data.
 */
object CalmMode {
    private const val PREFS = "saathi_ui"
    private const val KEY = "calm_mode"

    private val _enabled = MutableStateFlow(false)
    val enabled: StateFlow<Boolean> = _enabled

    fun load(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        _enabled.value = prefs.getBoolean(KEY, false)
    }

    fun set(context: Context, value: Boolean) {
        _enabled.value = value
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY, value)
            .apply()
    }
}

/** Optional sound. Off by default. */
object SoundPref {
    private const val PREFS = "saathi_ui"
    private const val KEY = "sound_on"

    private val _enabled = MutableStateFlow(false)
    val enabled: StateFlow<Boolean> = _enabled

    fun load(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        _enabled.value = prefs.getBoolean(KEY, false)
    }

    fun set(context: Context, value: Boolean) {
        _enabled.value = value
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY, value)
            .apply()
    }
}
