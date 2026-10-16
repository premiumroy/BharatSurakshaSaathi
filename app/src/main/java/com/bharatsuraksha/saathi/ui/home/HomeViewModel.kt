package com.bharatsuraksha.saathi.ui.home

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bharatsuraksha.saathi.R
import com.bharatsuraksha.saathi.data.prefs.SettingsRepository
import com.bharatsuraksha.saathi.data.repository.HistoryRepository
import com.bharatsuraksha.saathi.domain.engine.ScamEngine
import com.bharatsuraksha.saathi.ui.session.IncomingTextStore
import com.bharatsuraksha.saathi.ui.session.LastResultStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class HomeUiState(
    val text: String = "",
    @StringRes val errorRes: Int? = null,
    val checking: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val engine: ScamEngine,
    private val historyRepository: HistoryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val easyMode: StateFlow<Boolean> = settingsRepository.easyMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val kidsMode: StateFlow<Boolean> = settingsRepository.kidsMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** Text shared in from another app (or the notification listener). */
    val incomingText: StateFlow<String?> = IncomingTextStore.text

    private val _navigateToResult = MutableSharedFlow<Unit>()
    val navigateToResult: SharedFlow<Unit> = _navigateToResult.asSharedFlow()

    fun onTextChange(value: String) {
        _uiState.update { it.copy(text = value, errorRes = null) }
    }

    fun onIncomingConsumed() = IncomingTextStore.consume()

    fun onEasyModeChange(on: Boolean) {
        viewModelScope.launch { settingsRepository.setEasyMode(on) }
    }

    fun check() {
        val text = _uiState.value.text.trim()
        if (text.isEmpty()) {
            _uiState.update { it.copy(errorRes = R.string.home_empty_message) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(checking = true) }
            val result = withContext(Dispatchers.Default) { engine.analyze(text) }
            LastResultStore.message = text
            LastResultStore.result = result
            historyRepository.add(text, result)
            _uiState.update { it.copy(checking = false) }
            _navigateToResult.emit(Unit)
        }
    }
}
