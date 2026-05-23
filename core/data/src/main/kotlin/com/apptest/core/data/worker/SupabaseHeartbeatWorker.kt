package com.apptest.core.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.apptest.core.common.jwtSubject
import com.apptest.core.data.install.PackageInstallChecker
import com.apptest.core.data.install.UninstallEventStore
import com.apptest.core.data.session.SessionStore
import com.apptest.core.network.notifications.SupabaseNotificationsApiService
import com.apptest.core.network.testing.AbandonBody
import com.apptest.core.network.testing.SupabaseTestingApiService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.firstOrNull

/**
 * Periodic WorkManager job per R-041 + R-040.
 *
 * **Audit 2026-05-23 fixes:**
 * - **CRIT-005:** gate the entire worker on having a current signed-in user. Previous code
 *   ran while signed-out, hit `runCatching` 401s silently, and crucially called
 *   `UninstallEventStore.drainAll()` BEFORE any backend interaction — wiping the queue
 *   without ever telling the server.
 * - **CRIT-005:** use `peek(uid) → abandon → ack(uid, succeeded)` so events that failed to
 *   reach the server stay queued for the next run.
 * - **CRIT-006:** queue + ack scoped by user id (cross-account isolation).
 * - **MED-009:** REPEAT_DAYS lowered 6→3 + flexInterval so WorkManager defer can't push the
 *   actual fire interval past Supabase's 7-day auto-pause window. Backoff also explicit.
 *
 * **Responsibilities:**
 * 1. Ping Supabase REST to keep the free-tier project alive.
 * 2. Drain this user's queued uninstall events; abandon matches on Supabase.
 * 3. Cross-check active matches via [PackageInstallChecker] to catch any missed events.
 */
@HiltWorker
class SupabaseHeartbeatWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val sessionStore: SessionStore,
    private val notificationsApiService: SupabaseNotificationsApiService,
    private val testingApiService: SupabaseTestingApiService,
    private val installChecker: PackageInstallChecker,
    private val uninstallEventStore: UninstallEventStore,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // CRIT-005 gate: signed-out → no-op (queue retained for next signed-in run).
        val uid = sessionStore.session.firstOrNull()
            ?.takeIf { !it.isExpired() }
            ?.jwt?.jwtSubject()
        if (uid == null) {
            Log.d(TAG, "Skipped: no signed-in user")
            return Result.success()
        }

        return runCatching {
            // 1. Keep-alive ping (best-effort).
            runCatching { notificationsApiService.ping() }
                .onSuccess { Log.d(TAG, "Keep-alive ping OK") }
                .onFailure { Log.w(TAG, "Ping failed: ${it.message}") }

            // 2. Read queued events for THIS user (peek, don't clear yet).
            val pendingUninstalls = uninstallEventStore.peek(uid)

            // 3. Fetch active matches; bail if Supabase is unreachable so we don't
            //    accidentally abandon matches we can't see.
            val activeMatches = runCatching { testingApiService.getActiveMatches() }
                .getOrElse {
                    Log.w(TAG, "getActiveMatches failed: ${it.message}")
                    return@runCatching Result.retry()
                }

            val acked = mutableSetOf<String>()
            for (match in activeMatches) {
                val packageName = match.apps?.packageName ?: continue
                val uninstalledByEvent = packageName in pendingUninstalls
                val uninstalledByCheck = !installChecker.isInstalled(packageName)
                if (!uninstalledByEvent && !uninstalledByCheck) continue
                val abandoned = runCatching {
                    testingApiService.abandonMatch(
                        idFilter = "eq.${match.id}",
                        body = AbandonBody(),
                    ).close()
                    Log.d(TAG, "Abandoned match ${match.id} — $packageName not installed")
                    true
                }.getOrElse {
                    Log.w(TAG, "Abandon failed for ${match.id}: ${it.message}")
                    false
                }
                if (abandoned && uninstalledByEvent) acked += packageName
            }

            // 4. Only ack packages whose abandon call returned successfully — failed ones
            //    stay in the queue for the next run.
            if (acked.isNotEmpty()) {
                uninstallEventStore.ack(userId = uid, packages = acked)
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
        private const val REPEAT_DAYS = 3L              // MED-009: 6 → 3 (margin vs Supabase 7d)
        private const val FLEX_HOURS = 12L
        private const val MAX_RETRIES = 3

        fun scheduleIfNeeded(workManager: WorkManager) {
            val request = PeriodicWorkRequestBuilder<SupabaseHeartbeatWorker>(
                REPEAT_DAYS, TimeUnit.DAYS,
                FLEX_HOURS, TimeUnit.HOURS,
            ).setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            ).setBackoffCriteria(
                BackoffPolicy.LINEAR,
                10L,
                TimeUnit.MINUTES,
            ).build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
