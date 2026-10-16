package com.bharatsuraksha.saathi.service

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallScreeningService
import androidx.core.content.ContextCompat
import com.bharatsuraksha.saathi.data.prefs.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * When an incoming call arrives from a number that is not in the user's contacts,
 * shows a small heads-up reminder ("Real police/CBI never ask for money on a call").
 *
 * It does NOT record, listen to, or transcribe calls, and it never blocks a call.
 * Requires the Call Screening role, which the user grants manually.
 */
@AndroidEntryPoint
class CallSafetyScreeningService : CallScreeningService() {

    @Inject lateinit var settingsRepository: SettingsRepository

    private val scope = CoroutineScope(Dispatchers.Default)

    override fun onScreenCall(callDetails: Call.Details) {
        // Always allow the call through — we only add a reminder.
        respondToCall(callDetails, CallResponse.Builder().build())

        val number = callDetails.handle?.schemeSpecificPart
        scope.launch {
            if (!settingsRepository.callTipEnabled.first()) return@launch
            if (number != null && isInContacts(number)) return@launch
            NotificationHelper.postCallTip(this@CallSafetyScreeningService, number)
        }
    }

    private fun isInContacts(number: String): Boolean {
        val granted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) return false

        return runCatching {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number)
            )
            contentResolver.query(uri, arrayOf(ContactsContract.PhoneLookup._ID), null, null, null)
                ?.use { cursor -> cursor.count > 0 } ?: false
        }.getOrDefault(false)
    }
}
