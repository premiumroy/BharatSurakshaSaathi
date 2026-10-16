package com.bharatsuraksha.saathi.ui.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bharatsuraksha.saathi.data.prefs.SettingsRepository
import com.bharatsuraksha.saathi.data.remote.ExplanationClient
import com.bharatsuraksha.saathi.domain.engine.AdviceProvider
import com.bharatsuraksha.saathi.domain.model.AnalysisResult
import com.bharatsuraksha.saathi.ui.session.LastResultStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val explanationClient: ExplanationClient
) : ViewModel() {

    val message: String get() = LastResultStore.message

    val result: AnalysisResult? get() = LastResultStore.result

    val adviceKeys: List<String>
        get() = LastResultStore.result?.let { AdviceProvider.adviceKeys(it) } ?: emptyList()

    val onlineMode: StateFlow<Boolean> =
        settings.onlineMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val familyContacts: StateFlow<Set<String>> =
        settings.familyContacts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    private val _explanation = MutableStateFlow<String?>(null)
    val explanation: StateFlow<String?> = _explanation.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private var currentEndpoint: String = ""

    init {
        viewModelScope.launch {
            settings.onlineEndpoint.collect { currentEndpoint = it }
        }
    }

    /** Sends ONLY the message text, and only after the user confirmed. */
    fun fetchExplanation() {
        viewModelScope.launch {
            _loading.value = true
            _explanation.value = explanationClient.fetchExplanation(currentEndpoint, message)
            _loading.value = false
        }
    }
}
