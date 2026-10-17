package com.bharatsuraksha.saathi.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bharatsuraksha.saathi.data.prefs.SettingsRepository
import com.bharatsuraksha.saathi.work.PhoneHealthScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * After a restart, puts back the two things the user had switched on: the daily
 * phone-health job, and nothing else. It deliberately starts NO listening — the
 * notification listener is restored by Android itself once the user has granted
 * notification access, and Call Guard is never started except by a tap.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var settings: SettingsRepository

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        val pending = goAsync()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        scope.launch {
            try {
                if (settings.agentPhoneHealth.first()) {
                    PhoneHealthScheduler.schedule(context)
                }
            } finally {
                pending.finish()
            }
        }
    }
}
