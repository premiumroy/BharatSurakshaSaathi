package com.bharatsuraksha.saathi.service

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallScreeningService
import androidx.core.content.ContextCompat
import com.bharatsuraksha.saathi.data.prefs.SettingsRepository
import com.bharatsuraksha.saathi.domain.agent.NumberHeuristics
import com.bharatsuraksha.saathi.domain.model.Verdict
import com.bharatsuraksha.saathi.ui.session.AgentActivityStore
import com.bharatsuraksha.saathi.ui.session.BankHelplineStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Call screening. Runs offline checks on the incoming number and, when the user
 * has turned it on, shows a quiet heads-up.
 *
 * It NEVER records, listens to or transcribes a call. It does not block a call
 * unless the user has explicitly switched on "silence unknown callers".
 * Requires the Call Screening role, granted manually by the user.
 */
@AndroidEntryPoint
class CallSafetyScreeningService : CallScreeningService() {

    @Inject lateinit var settingsRepository: SettingsRepository

    private val scope = CoroutineScope(Dispatchers.Default)

    override fun onScreenCall(callDetails: Call.Details) {
        val number = callDetails.handle?.schemeSpecificPart

        scope.launch {
            val enabled = settingsRepository.agentCallScreening.first()
            val paused = settingsRepository.isAgentPaused()
            val silenceUnknown = settingsRepository.silenceUnknownCallers.first()

            if (!enabled || paused) {
                respondToCall(callDetails, CallResponse.Builder().build())
                return@launch
            }

            val inContacts = number != null && isInContacts(number)
            val trustedBanks = BankHelplineStore.entries.value.map { it.number }.toSet()
            val blocklist = settingsRepository.blocklist.first()

            val analysis = NumberHeuristics.analyze(
                rawNumber = number,
                isInContacts = inContacts,
                trustedBankNumbers = trustedBanks,
                blocklist = blocklist
            )

            AgentActivityStore.recordCall(this@CallSafetyScreeningService)

            // Only ever silence when the user asked for it, and only for an
            // unknown number that looks dangerous.
            val shouldSilence = silenceUnknown && !inContacts && analysis.verdict == Verdict.DANGEROUS

            val response = CallResponse.Builder()
                .setDisallowCall(shouldSilence)
                .setRejectCall(shouldSilence)
                .setSkipCallLog(false)
                .setSkipNotification(false)
                .build()
            respondToCall(callDetails, response)

            if (!shouldSilence && analysis.verdict != Verdict.SAFE) {
                NotificationHelper.postCallTip(
                    this@CallSafetyScreeningService,
                    number,
                    analysis.verdict,
                    analysis.findings.map { it.reasonKey }
                )
            }
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
