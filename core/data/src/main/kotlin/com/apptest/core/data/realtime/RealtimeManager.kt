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
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

/**
 * Manages a single Supabase Realtime WebSocket connection per `_specs/api_contracts.md` §4.
 * Reconnects with exponential back-off + jitter; one pending reconnect at a time (HIGH-4/8).
 * Sends Phoenix `access_token` on JWT rotation instead of dropping the socket (MED-1).
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
        val base = supabaseBaseUrl.replace("https://", "wss://").replace("http://", "ws://")
        val url = "$base/realtime/v1/websocket?apikey=$anonKey&vsn=1.0.0"
        ws = okHttpClient.newWebSocket(Request.Builder().url(url).build(), PhoenixListener(jwt))
    }

    private suspend fun rotateAccessToken(newJwt: String) = connectionMutex.withLock { // MED-1
        val current = ws ?: return@withLock
        runCatching {
            current.send(
                """{"event":"access_token","topic":"realtime:public:notifications","payload":{"access_token":"$newJwt"},"ref":"${nextRef()}"}"""
            )
        }
    }

    private fun disconnect() {
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
            reconnectJob?.cancel()
            reconnectJob = scope.launch {
                connectionMutex.withLock { if (ws === webSocket) ws = null }
                val base = backoffMs
                backoffMs = min(backoffMs * 2, MAX_BACKOFF_MS)
                delay(base + Random.nextLong(0, base / 2 + 1)) // equal-jitter (HIGH-8)
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
                    runCatching { // MED-12: tolerate WS closed between check and send
                        webSocket.send("""{"event":"heartbeat","topic":"phoenix","payload":{},"ref":"${nextRef()}","join_ref":null}""")
                    }.onFailure { return@launch }
                }
            }
        }
    }

    // MED-006: buildJsonObject ensures JWT value is properly escaped.
    private fun joinPayload(ref: String, jwt: String): String =
        buildJsonObject {
            put("event", "phx_join")
            put("topic", "realtime:public:notifications")
            putJsonObject("payload") {
                putJsonObject("config") {
                    putJsonObject("broadcast") { put("self", false) }
                    putJsonObject("presence") { put("key", "") }
                    putJsonArray("postgres_changes") {
                        add(buildJsonObject {
                            put("event", "*")
                            put("schema", "public")
                            put("table", "notifications")
                        })
                    }
                }
                put("access_token", jwt)
            }
            put("ref", ref)
            put("join_ref", ref)
        }.toString()

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
        Log.w(TAG, "malformed payload (first 200 chars): ${text.take(200)}", it) // HIGH-1
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
