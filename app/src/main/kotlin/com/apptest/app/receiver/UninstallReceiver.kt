package com.apptest.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.apptest.core.data.install.UninstallEventStore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Receives [Intent.ACTION_PACKAGE_REMOVED] broadcasts and enqueues the uninstalled package
 * name into [UninstallEventStore] for later processing by [SupabaseHeartbeatWorker].
 *
 * **Why this split?** The receiver is triggered immediately on uninstall and must complete
 * quickly (< 10 s or Android kills it). It just records the event; the heavy network call
 * (abandon match on Supabase) runs in the next WorkManager heartbeat.
 *
 * Manifest registration in `AndroidManifest.xml` with `android:exported="true"` is required
 * for the system to deliver this broadcast.
 */
@AndroidEntryPoint
class UninstallReceiver : BroadcastReceiver() {

    @Inject lateinit var uninstallEventStore: UninstallEventStore

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_PACKAGE_REMOVED) return
        val packageName = intent.data?.schemeSpecificPart ?: return

        // goAsync lets us use a coroutine without freezing the main thread.
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                uninstallEventStore.record(packageName)
                Log.d(TAG, "Recorded uninstall: $packageName")
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "UninstallReceiver"
    }
}
