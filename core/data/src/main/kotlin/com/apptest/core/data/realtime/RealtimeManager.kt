package com.apptest.core.data.realtime

import android.util.Log
import com.apptest.core.data.di.ApplicationScope
import com.apptest.core.data.session.SessionStore
import com.apptest.core.network.di.SupabaseAnonKey
import com.apptest.core.network.di.SupabaseBaseUrl
import com.apptest.core.network.di.SupabaseRest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
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
 * - Reconnects after failure with a 5-second back-off.
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

    @Volatile private var ws: WebSocket? = null
    @Volatile private var currentJwt: String? = null
    @Volatile private var refCounter = 0

    init {
        scope.launch {
            sessionStore.session.collect { session ->
                if (session != null && !session.isExpired()) {
                    currentJwt = session.jwt
                    if (ws == null) connect(session.jwt)
                } else {
                    currentJwt = null
                    disconnect()
                }
            }
        }
    }

    private fun connect(jwt: String) {
        val realtimeBase = supabaseBaseUrl.replace("https://", "wss://").replace("http://", "ws://")
        val url = "$realtimeBase/realtime/v1/websocket?apikey=$anonKey&vsn=1.0.0"
        val req = Request.Builder().url(url).build()
        ws = okHttpClient.newWebSocket(req, PhoenixListener(jwt))
        Log.d(TAG, "WebSocket connecting")
    }

    private fun disconnect() {
        ws?.close(CLOSE_NORMAL, "signed out")
        ws = null
    }

    private fun nextRef() = (++refCounter).toString()

    private inner class PhoenixListener(private val jwt: String) : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(TAG, "WebSocket open — joining notifications channel")
            val ref = nextRef()
            webSocket.send(joinPayload(ref, jwt))
            scheduleHeartbeat(webSocket)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            if ("postgres_changes" !in text) return
            parseEvent(text)?.let { ev -> scope.launch { _events.emit(ev) } }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.w(TAG, "WebSocket failure: ${t.message}")
            ws = null
            scope.launch {
                delay(RECONNECT_MS)
                currentJwt?.let { connect(it) }
            }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            ws = null
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

    private fun parseEvent(text: String): RealtimeEvent? = runCatching {
        val table = extract(text, """"table":"""") ?: return null
        val eventType = extract(text, """"eventType":"""") ?: return null
        val schema = extract(text, """"schema":"""") ?: "public"
        val newStart = text.indexOf(""""new":{""")
        val fields = if (newStart >= 0) extractFields(text, newStart + 7) else emptyMap()
        RealtimeEvent(table = table, eventType = eventType, schema = schema, fields = fields)
    }.getOrNull()

    private fun extract(text: String, prefix: String): String? {
        val i = text.indexOf(prefix).takeIf { it >= 0 }?.plus(prefix.length + 1) ?: return null
        val end = text.indexOf('"', i).takeIf { it >= 0 } ?: return null
        return text.substring(i, end)
    }

    private fun extractFields(text: String, start: Int): Map<String, String> {
        val end = text.indexOf('}', start).takeIf { it >= 0 } ?: return emptyMap()
        val fragment = text.substring(start, end)
        return fragment.split(",").mapNotNull { pair ->
            val colon = pair.indexOf(':')
            if (colon < 0) return@mapNotNull null
            val k = pair.substring(0, colon).trim().trim('"')
            val v = pair.substring(colon + 1).trim().trim('"')
            k to v
        }.toMap()
    }

    private companion object {
        const val TAG = "RealtimeManager"
        const val HEARTBEAT_MS = 30_000L
        const val RECONNECT_MS = 5_000L
        const val CLOSE_NORMAL = 1000
    }
}
