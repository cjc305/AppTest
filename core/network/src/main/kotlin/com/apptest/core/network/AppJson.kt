package com.apptest.core.network

import kotlinx.serialization.json.Json

/**
 * Shared [Json] instance for all DTO ser/de. Configured per `_specs/api_contracts.md` §1:
 * - `ignoreUnknownKeys = true` — survive forward-compatible additions
 * - `explicitNulls = false` — omit null fields on wire
 * - `coerceInputValues = true` — null → default for non-nullable fields
 */
val AppJson: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    coerceInputValues = true
    prettyPrint = false
}
