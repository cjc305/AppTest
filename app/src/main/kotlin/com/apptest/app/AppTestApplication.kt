package com.apptest.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.apptest.core.data.worker.SupabaseHeartbeatWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application entry point. Initialises WorkManager with [HiltWorkerFactory] so that
 * Hilt-injected workers (e.g. [SupabaseHeartbeatWorker]) can receive their constructor deps.
 *
 * WorkManager auto-init is disabled in the Manifest (via the `WorkManagerInitializer` provider
 * removal); [Configuration.Provider] is the replacement per the official Hilt + WorkManager guide.
 */
@HiltAndroidApp
class AppTestApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // Schedule the 6-day Supabase keep-alive heartbeat (idempotent; uses KEEP policy).
        SupabaseHeartbeatWorker.scheduleIfNeeded(WorkManager.getInstance(this))
    }
}
