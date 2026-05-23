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
 * Per-user queue for packages detected as uninstalled by [UninstallReceiver].
 *
 * Backed by the same DataStore used for auth session (file: `auth_session.preferences_pb`).
 *
 * **CRIT-006 (audit 2026-05-23):** keyed by `pending_uninstalls_<uid>` so events queued
 * under user A cannot be drained under user B after an account switch. Without the
 * user-scoping, B's heartbeat worker would abandon B's matches because A had uninstalled
 * a package with the same name.
 *
 * **CRIT-005 (audit 2026-05-23):** the previous `drainAll()` cleared the queue BEFORE any
 * backend call — so if the abandon REST call failed (or worker ran while signed-out),
 * pending events were lost forever. Replaced with [peek] + [ack] so callers only remove
 * events that were successfully processed.
 *
 * Concurrency: DataStore guarantees atomic edits; safe for BroadcastReceiver + Worker use.
 */
@Singleton
class UninstallEventStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    /** Record that [packageName] was just removed from the device, under [userId]. Idempotent. */
    suspend fun record(userId: String, packageName: String) {
        val key = keyFor(userId)
        dataStore.edit { prefs ->
            prefs[key] = (prefs[key] ?: emptySet()) + packageName
        }
    }

    /** Return all pending uninstalled packages for [userId] WITHOUT clearing. Callers must [ack]. */
    suspend fun peek(userId: String): Set<String> =
        dataStore.data.map { it[keyFor(userId)] ?: emptySet() }.first()

    /** Remove [packages] from [userId]'s queue (called after successful backend abandon). */
    suspend fun ack(userId: String, packages: Set<String>) {
        if (packages.isEmpty()) return
        val key = keyFor(userId)
        dataStore.edit { prefs ->
            val remaining = (prefs[key] ?: emptySet()) - packages
            if (remaining.isEmpty()) prefs.remove(key) else prefs[key] = remaining
        }
    }

    /** Drop all pending events for [userId] (called on sign-out cleanup). */
    suspend fun clear(userId: String) {
        dataStore.edit { it.remove(keyFor(userId)) }
    }

    private fun keyFor(userId: String) = stringSetPreferencesKey("pending_uninstalls_$userId")
}
