package com.apptest.app

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.apptest.app.fcm.AppTestMessagingService
import com.apptest.app.fcm.FcmTopicManager
import com.apptest.core.data.worker.SupabaseHeartbeatWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application entry point. Initialises WorkManager with [HiltWorkerFactory] so that
 * Hilt-injected workers (e.g. [SupabaseHeartbeatWorker]) can receive their constructor deps.
 *
 * WorkManager auto-init is disabled in the Manifest (via the `WorkManagerInitializer` provider
 * removal); [Configuration.Provider] is the replacement per the official Hilt + WorkManager guide.
 *
 * HIGH-003 (audit 2026-05-23): [FcmTopicManager] is started here (ApplicationScope) instead of
 * inside MainActivity's lifecycleScope, so account-switch unsubscribe state survives process
 * death and Activity recreation.
 */
@HiltAndroidApp
class AppTestApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var fcmTopicManager: FcmTopicManager

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // MED-008: create FCM notification channel eagerly so first message doesn't pay the cost.
        AppTestMessagingService.ensureChannel(
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        )
        SupabaseHeartbeatWorker.scheduleIfNeeded(WorkManager.getInstance(this))
        fcmTopicManager.observe()
    }
}
