package com.bharatsuraksha.saathi.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.bharatsuraksha.saathi.domain.model.Sensitivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "saathi_settings")

/**
 * All app preferences in one place. Everything here stays on the device.
 */
@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // --- Preferences -----------------------------------------------------------

    val easyMode: Flow<Boolean> = bool(EASY_MODE, false)
    val kidsMode: Flow<Boolean> = bool(KIDS_MODE, false)
    val highContrast: Flow<Boolean> = bool(HIGH_CONTRAST, false)
    val onboardingDone: Flow<Boolean> = bool(ONBOARDING_DONE, false)
    val autoCheckEnabled: Flow<Boolean> = bool(AUTO_CHECK, false)
    val callTipEnabled: Flow<Boolean> = bool(CALL_TIP, false)
    val onlineMode: Flow<Boolean> = bool(ONLINE_MODE, false)

    val onlineEndpoint: Flow<String> = string(ONLINE_ENDPOINT, "")
    val parentPinHash: Flow<String> = string(PARENT_PIN_HASH, "")

    /** Package names the notification listener should watch. Empty = watch the defaults. */
    val watchedApps: Flow<Set<String>> = stringSet(WATCHED_APPS, emptySet())

    /** Numbers the user has blocked (any format). */
    val blocklist: Flow<Set<String>> = stringSet(BLOCKLIST, emptySet())

    /** Trusted family contacts (phone numbers) for the one-tap family alert. */
    val familyContacts: Flow<Set<String>> = stringSet(FAMILY_CONTACTS, emptySet())

    val sensitivity: Flow<Sensitivity> = context.dataStore.data.map {
        runCatching { Sensitivity.valueOf(it[SENSITIVITY] ?: Sensitivity.MEDIUM.name) }
            .getOrDefault(Sensitivity.MEDIUM)
    }

    // --- Setters ---------------------------------------------------------------

    suspend fun setEasyMode(on: Boolean) = putBoolean(EASY_MODE, on)
    suspend fun setKidsMode(on: Boolean) = putBoolean(KIDS_MODE, on)
    suspend fun setHighContrast(on: Boolean) = putBoolean(HIGH_CONTRAST, on)
    suspend fun setOnboardingDone(on: Boolean) = putBoolean(ONBOARDING_DONE, on)
    suspend fun setAutoCheckEnabled(on: Boolean) = putBoolean(AUTO_CHECK, on)
    suspend fun setCallTipEnabled(on: Boolean) = putBoolean(CALL_TIP, on)
    suspend fun setOnlineMode(on: Boolean) = putBoolean(ONLINE_MODE, on)

    suspend fun setOnlineEndpoint(value: String) {
        context.dataStore.edit { it[ONLINE_ENDPOINT] = value.trim() }
    }

    suspend fun setParentPinHash(value: String) {
        context.dataStore.edit { it[PARENT_PIN_HASH] = value }
    }

    suspend fun setSensitivity(value: Sensitivity) {
        context.dataStore.edit { it[SENSITIVITY] = value.name }
    }

    suspend fun setWatchedApps(value: Set<String>) {
        context.dataStore.edit { it[WATCHED_APPS] = value }
    }

    suspend fun setBlocklist(value: Set<String>) {
        context.dataStore.edit { it[BLOCKLIST] = value }
    }

    suspend fun setFamilyContacts(value: Set<String>) {
        context.dataStore.edit { it[FAMILY_CONTACTS] = value }
    }

    /** Wipes every preference (used by "Clear all data"). */
    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }

    /** Synchronous-ish read for background services. */
    suspend fun currentSensitivity(): Sensitivity = sensitivity.first()

    suspend fun currentWatchedApps(): Set<String> = watchedApps.first()

    suspend fun currentAutoCheck(): Boolean = autoCheckEnabled.first()

    // --- Helpers ---------------------------------------------------------------

    private fun bool(key: Preferences.Key<Boolean>, default: Boolean): Flow<Boolean> =
        context.dataStore.data.map { it[key] ?: default }

    private fun string(key: Preferences.Key<String>, default: String): Flow<String> =
        context.dataStore.data.map { it[key] ?: default }

    private fun stringSet(key: Preferences.Key<Set<String>>, default: Set<String>): Flow<Set<String>> =
        context.dataStore.data.map { it[key] ?: default }

    private suspend fun putBoolean(key: Preferences.Key<Boolean>, value: Boolean) {
        context.dataStore.edit { it[key] = value }
    }

    companion object {
        private val EASY_MODE = booleanPreferencesKey("easy_mode")
        private val KIDS_MODE = booleanPreferencesKey("kids_mode")
        private val HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
        private val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        private val AUTO_CHECK = booleanPreferencesKey("auto_check")
        private val CALL_TIP = booleanPreferencesKey("call_tip")
        private val ONLINE_MODE = booleanPreferencesKey("online_mode")
        private val ONLINE_ENDPOINT = stringPreferencesKey("online_endpoint")
        private val PARENT_PIN_HASH = stringPreferencesKey("parent_pin_hash")
        private val SENSITIVITY = stringPreferencesKey("sensitivity")
        private val WATCHED_APPS = stringSetPreferencesKey("watched_apps")
        private val BLOCKLIST = stringSetPreferencesKey("blocklist")
        private val FAMILY_CONTACTS = stringSetPreferencesKey("family_contacts")
    }
}
