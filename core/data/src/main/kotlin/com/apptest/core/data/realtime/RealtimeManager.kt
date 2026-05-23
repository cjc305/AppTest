package com.apptest.core.data.realtime

import android.util.Log
import com.apptest.core.data.di.ApplicationScope
import com.apptest.core.data.session.SessionStore
import com.apptest.core.network.AppJson
import com.apptest.core.network.di.SupabaseAnonKey
import com.apptest.core.network.di.SupabaseBaseUrl
import com.apptest.core.network.di.SupabaseRest
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

/**
 * Manages a single Supabase Realtime WebSocket connection per `_specs/api_contracts.md` §4.
 *
 * Lifecycle (post audit-2026-05-23 fixes):
 * - Opens on first valid [SessionStore.session]; reuses the [@SupabaseRest] OkHttpClient.
 * - On JWT rotation (refresh while signed in), sends Phoenix `access_token` message to the
 *   existing channel instead of dropping the connection (MED-1).
 * - Phoenix heartbeat every 30 s; send wrapped in runCatching to tolerate closed-WS races (HIGH-2-realtime).
 * - Reconnects after failure with exponential back-off + jitter (HIGH-8). Only ONE pending
 *   reconnect coroutine at a time — prior failures cancel any in-flight reconnect (HIGH-4 / CRIT-2).
 * - Closes on sign-out (null session); explicitly cancels the pending reconnect (CRIT-2).
 */
@Singleton
class RealtimeManager @Inject constructor(
    @SupabaseRest private val okHttpClient: OkHttpClient,
    @SupabaseAnonKey private val anonKey: String,
    @SupabaseBaseUrl private val supabaseBaseUrl: String,
    private val sessionStore: SessionStore,
    @ApplicationScope private val scope: CoroutineScope,
) {

    private val _events = MutableSharedFlow<RealtimeEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<RealtimeEvent> = _events.asSharedFlow()

    private val connectionMutex = Mutex()
    @Volatile private var ws: WebSocket? = null
    @Volatile private var currentJwt: String? = null
    @Volatile private var refCounter = 0
    @Volatile private var backoffMs: Long = INITIAL_BACKOFF_MS
    @Volatile private var reconnectJob: Job? = null

    init {
        scope.launch {
            sessionStore.session.collect { session ->
                val newJwt = session?.takeIf { !it.isExpired() }?.jwt
                val prev = currentJwt
                currentJwt = newJwt
                when {
                    newJwt == null -> disconnect()
                    prev == null -> connectIfNeeded(newJwt)
                    prev != newJwt -> rotateAccessToken(newJwt)  // MED-1: refresh without drop
                    else -> Unit
                }
            }
        }
    }

    private suspend fun connectIfNeeded(jwt: String) = connectionMutex.withLock {
        if (ws != null) return@withLock
        val realtimeBase = supabaseBaseUrl.replace("https://", "wss://").replace("http://", "ws://")
        val url = "$realtimeBase/realtime/v1/websocket?apikey=$anonKey&vsn=1.0.0"
        val req = Request.Builder().url(url).build()
        ws = okHttpClient.newWebSocket(req, PhoenixListener(jwt))
        Log.d(TAG, "WebSocket connecting")
    }

    /** MED-1: forward the new JWT to the existing channel without recreating the socket. */
    private suspend fun rotateAccessToken(newJwt: String) = connectionMutex.withLock {
        val current = ws ?: return@withLock
        runCatching {
            current.send(
                """{"event":"access_token","topic":"realtime:public:notifications","payload":{"access_token":"$newJwt"},"ref":"${nextRef()}"}"""
            )
        }
    }

    private fun disconnect() {
        // CRIT-2: cancel any pending reconnect so the user's sign-out actually closes the channel.
        reconnectJob?.cancel()
        reconnectJob = null
        scope.launch {
            connectionMutex.withLock {
                ws?.close(CLOSE_NORMAL, "signed out")
                ws = null
                backoffMs = INITIAL_BACKOFF_MS
            }
        }
    }

    private fun nextRef() = (++refCounter).toString()

    private inner class PhoenixListener(private val jwt: String) : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(TAG, "WebSocket open — joining notifications channel")
            backoffMs = INITIAL_BACKOFF_MS
            val ref = nextRef()
            runCatching { webSocket.send(joinPayload(ref, jwt)) }
            scheduleHeartbeat(webSocket)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            if ("postgres_changes" !in text) return
            parseEvent(text)?.let { ev -> scope.launch { _events.emit(ev) } }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.w(TAG, "WebSocket failure: ${t.message} — retry in ${backoffMs}ms")
            // HIGH-4: cancel any prior in-flight reconnect; only one pending reconnect ever.
            reconnectJob?.cancel()
            reconnectJob = scope.launch {
                connectionMutex.withLock { if (ws === webSocket) ws = null }
                val base = backoffMs
                backoffMs = min(backoffMs * 2, MAX_BACKOFF_MS)
                // HIGH-8 fix: equal-jitter so N devices don't reconnect in lockstep.
                val jitter = Random.nextLong(0, base / 2 + 1)
                delay(base + jitter)
                // CRIT-2: re-check current session under the mutex — sign-out during backoff aborts here.
                val jwtNow = currentJwt ?: return@launch
                connectIfNeeded(jwtNow)
            }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            scope.launch { connectionMutex.withLock { if (ws === webSocket) ws = null } }
        }

        private fun scheduleHeartbeat(webSocket: WebSocket) {
            scope.launch {
                while (ws === webSocket) {
                    delay(HEARTBEAT_MS)
                    // MED-12: WS can close between the `===` check and the send — wrap to tolerate.
                    runCatching {
                        webSocket.send("""{"event":"heartbeat","topic":"phoenix","payload":{},"ref":"${nextRef()}","join_ref":null}""")
                    }.onFailure { return@launch }
                }
            }
        }
    }

    private fun joinPayload(ref: String, jwt: String): String =
        """{"event":"phx_join","topic":"realtime:public:notifications","payload":{"config":{"broadcast":{"self":false},"presence":{"key":""},"postgres_changes":[{"event":"*","schema":"public","table":"notifications"}]},"access_token":"$jwt"},"ref":"$ref","join_ref":"$ref"}"""

    private fun parseEvent(text: String): RealtimeEvent? = runCatching {
        val root = AppJson.parseToJsonElement(text).jsonObject
        val data = root["payload"]?.jsonObject?.get("data")?.jsonObject ?: return@runCatching null
        val table = data["table"]?.jsonPrimitive?.contentOrNull ?: return@runCatching null
        val eventType = (data["type"] ?: data["eventType"])?.jsonPrimitive?.contentOrNull
            ?: return@runCatching null
        val schema = data["schema"]?.jsonPrimitive?.contentOrNull ?: "public"
        val row = (data["record"] ?: data["new"])?.jsonObject
        val fields: Map<String, String> = row.orEmpty().mapValues { (_, v) -> v.asString() }
        RealtimeEvent(table = table, eventType = eventType, schema = schema, fields = fields)
    }.onFailure {
        // HIGH-1: previous Log.v was filtered in release. Use Log.w with truncated payload so prod
        // surfaces malformed frames (still bounded — log only first 200 chars).
        Log.w(TAG, "malformed payload (first 200 chars): ${text.take(200)}", it)
    }.getOrNull()

    private fun JsonElement.asString(): String =
        (this as? kotlinx.serialization.json.JsonPrimitive)?.contentOrNull ?: toString()

    private fun JsonObject?.orEmpty(): JsonObject = this ?: JsonObject(emptyMap())

    private companion object {
        const val TAG = "RealtimeManager"
        const val HEARTBEAT_MS = 30_000L
        const val INITIAL_BACKOFF_MS = 1_000L
        const val MAX_BACKOFF_MS = 60_000L
        const val CLOSE_NORMAL = 1000
    }
}
