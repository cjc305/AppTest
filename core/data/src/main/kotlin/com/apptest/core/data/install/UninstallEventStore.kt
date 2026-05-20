package com.apptest.core.data.install

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Lightweight queue for packages detected as uninstalled by [UninstallReceiver].
 *
 * Backed by the same DataStore used for auth session (file: `auth_session.preferences_pb`).
 * The set is consumed and cleared atomically by [SupabaseHeartbeatWorker] after it calls
 * the backend abandon endpoint for each entry.
 *
 * Concurrency: DataStore guarantees atomic edits; safe for BroadcastReceiver + Worker use.
 */
@Singleton
class UninstallEventStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    /** Record that [packageName] was just removed from the device. Idempotent. */
    suspend fun record(packageName: String) {
        dataStore.edit { prefs ->
            val current = prefs[KEY] ?: emptySet()
            prefs[KEY] = current + packageName
        }
    }

    /** Return all pending uninstalled packages, then atomically clear the queue. */
    suspend fun drainAll(): Set<String> {
        val pending = dataStore.data.map { it[KEY] ?: emptySet() }.first()
        if (pending.isNotEmpty()) {
            dataStore.edit { it.remove(KEY) }
        }
        return pending
    }

    private companion object {
        val KEY = stringSetPreferencesKey("pending_uninstalls")
    }
}
