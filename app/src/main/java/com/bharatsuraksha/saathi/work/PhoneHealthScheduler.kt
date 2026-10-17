package com.bharatsuraksha.saathi.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Schedules the daily phone-health job. One job, once a day, only when the
 * battery is not low. No polling, no tight loops, nothing in between.
 */
object PhoneHealthScheduler {

    private const val WORK_NAME = "phone_health_daily"

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<PhoneHealthWorker>(24, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .setInitialDelay(1, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    /** For the "check now" button on the screen. */
    fun runNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<PhoneHealthWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME + "_now",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}
