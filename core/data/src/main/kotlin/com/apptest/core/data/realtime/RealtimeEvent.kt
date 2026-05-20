package com.apptest.core.data.realtime

/**
 * Decoded Supabase Realtime `postgres_changes` event from the WebSocket stream.
 *
 * [fields] contains all column values from the affected row as strings — safe for V1 because
 * notification records are small and string-representable.
 * Callers check [table] to filter relevant events.
 */
data class RealtimeEvent(
    val table: String,
    val eventType: String,        // INSERT | UPDATE | DELETE
    val schema: String,
    val fields: Map<String, String>,  // column → value (null cells → "null")
)
