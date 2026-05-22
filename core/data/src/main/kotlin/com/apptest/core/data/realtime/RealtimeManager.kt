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
import kotlinx.coroutines.CoroutineScope
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
 * Lifecycle:
 * - Opens on first valid [SessionStore.session]; reuses the [@SupabaseRest][SupabaseRest] client.
 * - Subscribes to `realtime:public:notifications`; RLS ensures user-scoped delivery.
 * - Sends Phoenix heartbeats every 30 s to keep the connection alive.
 * - Reconnects after failure with exponential back-off (1s → 60s cap); reset on successful join.
 * - Closes on sign-out (null session).
 *
 * Consumers call [events] and filter by [RealtimeEvent.table].
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

    init {
        scope.launch {
            sessionStore.session.collect { session ->
                if (session != null && !session.isExpired()) {
                    currentJwt = session.jwt
                    connectIfNeeded(session.jwt)
                } else {
                    currentJwt = null
                    disconnect()
                }
            }
        }
    }

    /** Serializes connect/disconnect so concurrent session flips don't double-open the socket. */
    private suspend fun connectIfNeeded(jwt: String) = connectionMutex.withLock {
        if (ws != null) return@withLock
        val realtimeBase = supabaseBaseUrl.replace("https://", "wss://").replace("http://", "ws://")
        val url = "$realtimeBase/realtime/v1/websocket?apikey=$anonKey&vsn=1.0.0"
        val req = Request.Builder().url(url).build()
        ws = okHttpClient.newWebSocket(req, PhoenixListener(jwt))
        Log.d(TAG, "WebSocket connecting")
    }

    private fun disconnect() {
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
            webSocket.send(joinPayload(ref, jwt))
            scheduleHeartbeat(webSocket)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            if ("postgres_changes" !in text) return
            parseEvent(text)?.let { ev -> scope.launch { _events.emit(ev) } }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.w(TAG, "WebSocket failure: ${t.message} — retry in ${backoffMs}ms")
            scope.launch {
                connectionMutex.withLock { ws = null }
                val delayMs = backoffMs
                backoffMs = min(backoffMs * 2, MAX_BACKOFF_MS)
                delay(delayMs)
                currentJwt?.let { connectIfNeeded(it) }
            }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            scope.launch { connectionMutex.withLock { if (ws === webSocket) ws = null } }
        }

        private fun scheduleHeartbeat(webSocket: WebSocket) {
            scope.launch {
                while (ws === webSocket) {
                    delay(HEARTBEAT_MS)
                    webSocket.send("""{"event":"heartbeat","topic":"phoenix","payload":{},"ref":"${nextRef()}","join_ref":null}""")
                }
            }
        }
    }

    // ─── JSON helpers ─────────────────────────────────────────────────────────

    private fun joinPayload(ref: String, jwt: String): String =
        """{"event":"phx_join","topic":"realtime:public:notifications","payload":{"config":{"broadcast":{"self":false},"presence":{"key":""},"postgres_changes":[{"event":"*","schema":"public","table":"notifications"}]},"access_token":"$jwt"},"ref":"$ref","join_ref":"$ref"}"""

    /**
     * Phoenix realtime payload shape (Supabase):
     * `{ "payload": { "data": { "table": ..., "type": ..., "schema": ..., "record"|"new": { ... } } } }`
     *
     * We parse defensively (any field may be missing) and tolerate either `"new"` (older)
     * or `"record"` (current) keys for the row body.
     */
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
    }.onFailure { Log.v(TAG, "skipped malformed payload: ${it.message}") }.getOrNull()

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
