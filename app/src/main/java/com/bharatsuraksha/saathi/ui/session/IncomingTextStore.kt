package com.bharatsuraksha.saathi.ui.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds text that arrived from another app via the Android share-sheet (or from
 * the notification listener) until the Home screen picks it up. In-memory only.
 */
object IncomingTextStore {
    private val _text = MutableStateFlow<String?>(null)
    val text: StateFlow<String?> = _text.asStateFlow()

    fun post(value: String) { _text.value = value }

    fun consume() { _text.value = null }
}
