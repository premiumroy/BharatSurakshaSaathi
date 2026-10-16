package com.bharatsuraksha.saathi.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bharatsuraksha.saathi.data.prefs.PinManager
import com.bharatsuraksha.saathi.data.prefs.SettingsRepository
import com.bharatsuraksha.saathi.data.repository.EvidenceRepository
import com.bharatsuraksha.saathi.data.repository.HistoryRepository
import com.bharatsuraksha.saathi.domain.model.Sensitivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val historyRepository: HistoryRepository,
    private val evidenceRepository: EvidenceRepository
) : ViewModel() {

    private fun <T> state(flow: kotlinx.coroutines.flow.Flow<T>, default: T): StateFlow<T> =
        flow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), default)

    val easyMode = state(settings.easyMode, false)
    val highContrast = state(settings.highContrast, false)
    val kidsMode = state(settings.kidsMode, false)
    val sensitivity = state(settings.sensitivity, Sensitivity.MEDIUM)
    val watchedApps = state(settings.watchedApps, emptySet<String>())
    val autoCheck = state(settings.autoCheckEnabled, false)
    val callTip = state(settings.callTipEnabled, false)
    val onlineMode = state(settings.onlineMode, false)
    val onlineEndpoint = state(settings.onlineEndpoint, "")
    val familyContacts = state(settings.familyContacts, emptySet<String>())
    val pinHash = state(settings.parentPinHash, "")

    /** null = still loading (so we can decide whether to show onboarding). */
    val onboardingDone: StateFlow<Boolean?> = settings.onboardingDone
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val hasPin: StateFlow<Boolean> = settings.parentPinHash
        .map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun setEasyMode(on: Boolean) = viewModelScope.launch { settings.setEasyMode(on) }
    fun setHighContrast(on: Boolean) = viewModelScope.launch { settings.setHighContrast(on) }
    fun setKidsMode(on: Boolean) = viewModelScope.launch { settings.setKidsMode(on) }
    fun setAutoCheck(on: Boolean) = viewModelScope.launch { settings.setAutoCheckEnabled(on) }
    fun setCallTip(on: Boolean) = viewModelScope.launch { settings.setCallTipEnabled(on) }
    fun setOnlineMode(on: Boolean) = viewModelScope.launch { settings.setOnlineMode(on) }
    fun setSensitivity(value: Sensitivity) = viewModelScope.launch { settings.setSensitivity(value) }
    fun setWatchedApps(value: Set<String>) = viewModelScope.launch { settings.setWatchedApps(value) }
    fun setEndpoint(value: String) = viewModelScope.launch { settings.setOnlineEndpoint(value) }
    fun setFamilyContacts(value: Set<String>) = viewModelScope.launch { settings.setFamilyContacts(value) }
    fun setPin(pin: String) = viewModelScope.launch { settings.setParentPinHash(PinManager.hash(pin)) }

    fun checkPin(pin: String, currentHash: String): Boolean =
        currentHash.isNotEmpty() && PinManager.hash(pin) == currentHash

    fun clearAllData() {
        viewModelScope.launch {
            historyRepository.deleteAll()
            evidenceRepository.deleteAll()
            settings.clearAll()
        }
    }
}
