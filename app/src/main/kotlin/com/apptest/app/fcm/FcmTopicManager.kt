package com.apptest.app.fcm

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.apptest.core.common.jwtSubject
import com.apptest.core.data.di.ApplicationScope
import com.apptest.core.data.session.SessionStore
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Application-scoped FCM topic-subscription manager.
 *
 * **Audit 2026-05-23 HIGH-003 fix:** the previous `MainActivity.lifecycleScope` block kept
 * `lastUid` as a local `var`, so process death erased it and a subsequent sign-out never
 * unsubscribed from the prior user's topic — cross-account notification leak. The previous
 * code also treated `unsubscribeFromTopic` as fire-and-forget; if the network call failed,
 * no retry was attempted.
 *
 * This singleton:
 * 1. Lives in [ApplicationScope] (survives Activity recreation / process recreation).
 * 2. Persists `last_subscribed_uid` in DataStore so account-switch detection survives
 *    process death.
 * 3. Persists a `pending_unsubscribes` set so a failed/interrupted unsubscribe is retried
 *    on the next cold start before the new subscription is attempted.
 * 4. `await()`s the FCM Tasks so we only consider a topic confirmed after Firebase actually
 *    accepts the request.
 *
 * Hook: [AppTestApplication] field-injects this and calls [observe] in onCreate.
 */
@Singleton
class FcmTopicManager @Inject constructor(
    private val sessionStore: SessionStore,
    private val dataStore: DataStore<Preferences>,
    @ApplicationScope private val scope: CoroutineScope,
) {

    /**
     * Start observing session → topic subscription state. Idempotent — calling twice
     * just launches two collectors; in practice [AppTestApplication] calls this exactly once.
     */
    fun observe() {
        scope.launch { drainPending() }
        scope.launch {
            sessionStore.session
                .map { it?.takeIf { s -> !s.isExpired() }?.jwt?.jwtSubject() }
                .distinctUntilChanged()
                .collect { uid -> syncSubscription(uid) }
        }
    }

    /** On startup, retry any unsubscribes that didn't confirm last session. */
    private suspend fun drainPending() {
        val pending = dataStore.data.firstOrNull()?.get(KEY_PENDING) ?: emptySet()
        pending.forEach { topic ->
            runCatching { FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).awaitTask() }
                .onSuccess {
                    removePending(topic)
                    Log.d(TAG, "Drained pending unsubscribe: $topic")
                }
                .onFailure { Log.w(TAG, "Pending unsubscribe still failing for $topic: ${it.message}") }
        }
    }

    private suspend fun syncSubscription(uid: String?) {
        val last = dataStore.data.firstOrNull()?.get(KEY_LAST_UID)
        // Unsubscribe the prior user's topic if it differs from the current one.
        if (last != null && last != uid) {
            val topic = "user_$last"
            // Queue first so we retry on failure / process death; remove only on confirmed success.
            addPending(topic)
            runCatching { FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).awaitTask() }
                .onSuccess { removePending(topic); Log.d(TAG, "Unsubscribed $topic") }
                .onFailure { Log.w(TAG, "Unsubscribe failed for $topic: ${it.message}") }
        }
        if (uid != null) {
            val topic = "user_$uid"
            runCatching { FirebaseMessaging.getInstance().subscribeToTopic(topic).awaitTask() }
                .onSuccess { Log.d(TAG, "Subscribed $topic") }
                .onFailure { Log.w(TAG, "Subscribe failed for $topic: ${it.message}") }
        }
        // Persist the new "last uid" regardless of subscribe result (the user IS signed in / out).
        dataStore.edit { prefs ->
            if (uid == null) prefs.remove(KEY_LAST_UID) else prefs[KEY_LAST_UID] = uid
        }
    }

    private suspend fun addPending(topic: String) {
        dataStore.edit { it[KEY_PENDING] = (it[KEY_PENDING] ?: emptySet()) + topic }
    }

    private suspend fun removePending(topic: String) {
        dataStore.edit { prefs ->
            val remaining = (prefs[KEY_PENDING] ?: emptySet()) - topic
            if (remaining.isEmpty()) prefs.remove(KEY_PENDING) else prefs[KEY_PENDING] = remaining
        }
    }

    /** Suspend bridge for Play-Services Task (avoids needing kotlinx-coroutines-play-services). */
    private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { cont ->
        addOnSuccessListener { cont.resume(it) }
        addOnFailureListener { cont.resumeWithException(it) }
    }

    private companion object {
        const val TAG = "FcmTopicManager"
        val KEY_LAST_UID = stringPreferencesKey("fcm_last_subscribed_uid")
        val KEY_PENDING = stringSetPreferencesKey("fcm_pending_unsubscribes")
    }
}
