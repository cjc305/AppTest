package com.apptest.core.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.apptest.core.network.auth.SupabaseAuthApiService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * LOW-005 — Retries the Supabase `/auth/v1/logout` call when [signOut] fails offline.
 *
 * The JWT is passed as [KEY_JWT] input data so it is available even after
 * [SessionStore] has been cleared. WorkManager persists input data in its own
 * Room DB (app-private), so the plaintext JWT is not accessible to other apps;
 * exposure window is bounded by the 3-attempt backoff (~8 min total).
 *
 * Enqueued with [WORK_NAME] + [ExistingWorkPolicy.REPLACE] so a second sign-out
 * attempt on the same device (e.g. user pressed Back then logged in again and
 * immediately signed out) replaces the queued request rather than stacking.
 */
@HiltWorker
class SignOutRevocationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val authApiService: SupabaseAuthApiService,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val jwt = inputData.getString(KEY_JWT)
        if (jwt.isNullOrBlank()) {
            Log.w(TAG, "Missing JWT — nothing to revoke")
            return Result.failure()
        }

        return runCatching {
            authApiService.signOut(bearer = "Bearer $jwt")
            Log.d(TAG, "Token revocation succeeded on attempt ${runAttemptCount + 1}")
            Result.success()
        }.getOrElse { t ->
            Log.w(TAG, "Revocation failed (attempt ${runAttemptCount + 1}): ${t.message}")
            if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
        }
    }

    companion object {
        private const val TAG = "SignOutRevocation"
        private const val WORK_NAME = "signout_revocation"
        private const val KEY_JWT = "jwt"
        private const val MAX_RETRIES = 3

        /**
         * Enqueue a one-time revocation attempt with exponential backoff.
         * Safe to call from a background coroutine; WorkManager persists across process death.
         */
        fun enqueue(workManager: WorkManager, jwt: String) {
            val request = OneTimeWorkRequestBuilder<SignOutRevocationWorker>()
                .setInputData(workDataOf(KEY_JWT to jwt))
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build(),
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1L, TimeUnit.MINUTES)
                .build()

            workManager.enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
            Log.d(TAG, "Revocation worker enqueued")
        }
    }
}
