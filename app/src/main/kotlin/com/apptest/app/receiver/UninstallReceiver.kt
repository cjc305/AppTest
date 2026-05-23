package com.apptest.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.apptest.core.common.jwtSubject
import com.apptest.core.data.di.ApplicationScope
import com.apptest.core.data.install.UninstallEventStore
import com.apptest.core.data.session.SessionStore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * Receives [Intent.ACTION_PACKAGE_REMOVED] broadcasts and enqueues the uninstalled package
 * name into [UninstallEventStore] for later processing by [SupabaseHeartbeatWorker].
 *
 * **CRIT-003 / CRIT-006 (audit 2026-05-23):** uses the application-scoped [appScope] instead
 * of `CoroutineScope(Dispatchers.IO)` (which had no SupervisorJob, no cancellation tie-in,
 * leaking Jobs on receiver tear-down). The event is recorded against the current user's id —
 * extracted from the active session JWT — so an account switch can't cross-contaminate
 * queues.
 *
 * If no signed-in user exists at broadcast time (rare: uninstall fires after sign-out), the
 * event is dropped with a warning. Tracking a "last-known uid" for late events is a follow-up.
 *
 * **Why this split?** The receiver is triggered immediately on uninstall and must complete
 * quickly (< 10 s or Android kills it). It just records the event; the heavy network call
 * (abandon match on Supabase) runs in the next WorkManager heartbeat.
 */
@AndroidEntryPoint
class UninstallReceiver : BroadcastReceiver() {

    @Inject lateinit var uninstallEventStore: UninstallEventStore
    @Inject lateinit var sessionStore: SessionStore
    @Inject @ApplicationScope lateinit var appScope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_PACKAGE_REMOVED) return
        // Ignore upgrade events (the package is being replaced, not truly uninstalled).
        if (intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) return
        val packageName = intent.data?.schemeSpecificPart ?: return

        val pendingResult = goAsync()
        appScope.launch {
            try {
                val uid = sessionStore.session.firstOrNull()
                    ?.takeIf { !it.isExpired() }
                    ?.jwt?.jwtSubject()
                if (uid == null) {
                    Log.w(TAG, "Ignored uninstall of $packageName: no signed-in user")
                    return@launch
                }
                uninstallEventStore.record(userId = uid, packageName = packageName)
                Log.d(TAG, "Recorded uninstall under uid=$uid: $packageName")
            } catch (t: Throwable) {
                Log.w(TAG, "Recording uninstall failed: ${t.message}")
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "UninstallReceiver"
    }
}
