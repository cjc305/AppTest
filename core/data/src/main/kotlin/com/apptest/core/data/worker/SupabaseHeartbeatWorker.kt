package com.apptest.core.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.apptest.core.data.install.PackageInstallChecker
import com.apptest.core.data.install.UninstallEventStore
import com.apptest.core.network.notifications.SupabaseNotificationsApiService
import com.apptest.core.network.testing.AbandonBody
import com.apptest.core.network.testing.SupabaseTestingApiService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Periodic WorkManager job that runs every [REPEAT_DAYS] days on a network connection.
 *
 * **Responsibilities (R-041 + R-040):**
 * 1. Ping Supabase REST to keep the free-tier project alive (prevents 7-day auto-pause).
 * 2. Drain the [UninstallEventStore] queue; for any packages that were recently uninstalled
 *    and have an active match, call `PATCH matches → abandoned` on Supabase.
 * 3. Cross-check active matches via [PackageInstallChecker] to catch any missed events
 *    (device restart, deferred broadcast delivery, etc.).
 */
@HiltWorker
class SupabaseHeartbeatWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationsApiService: SupabaseNotificationsApiService,
    private val testingApiService: SupabaseTestingApiService,
    private val installChecker: PackageInstallChecker,
    private val uninstallEventStore: UninstallEventStore,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return runCatching {
            // 1. Keep-alive ping
            notificationsApiService.ping()
            Log.d(TAG, "Keep-alive ping OK")

            // 2. Process queued uninstall broadcast events
            val pendingUninstalls = uninstallEventStore.drainAll()

            // 3. Cross-check all active matches for install presence
            val activeMatches = runCatching { testingApiService.getActiveMatches() }
                .getOrDefault(emptyList())

            for (match in activeMatches) {
                val packageName = match.apps?.packageName ?: continue
                val uninstalledByEvent = packageName in pendingUninstalls
                val uninstalledByCheck = !installChecker.isInstalled(packageName)
                if (uninstalledByEvent || uninstalledByCheck) {
                    runCatching {
                        testingApiService.abandonMatch(
                            idFilter = "eq.${match.id}",
                            body = AbandonBody(),
                        ).close()
                        Log.d(TAG, "Abandoned match ${match.id} — $packageName not installed")
                    }
                }
            }
            Result.success()
        }.getOrElse { t ->
            Log.w(TAG, "Heartbeat failed: ${t.message}")
            if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
        }
    }

    companion object {
        private const val TAG = "SupabaseHeartbeat"
        private const val WORK_NAME = "supabase_heartbeat"
        private const val REPEAT_DAYS = 6L
        private const val MAX_RETRIES = 3

        fun scheduleIfNeeded(workManager: WorkManager) {
            val request = PeriodicWorkRequestBuilder<SupabaseHeartbeatWorker>(
                REPEAT_DAYS, TimeUnit.DAYS,
            ).setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            ).build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
