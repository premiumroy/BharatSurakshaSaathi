package com.bharatsuraksha.saathi

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * The app object. WorkManager is configured through Hilt so the daily phone-health
 * job can take injected dependencies. Nothing here starts any background work by
 * itself: the agent is off until the user switches it on.
 */
@HiltAndroidApp
class BharatSurakshaApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
