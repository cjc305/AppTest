package com.apptest.core.common

import java.util.Base64

/**
 * Extracts the `sub` claim (Supabase user UUID) from a compact JWS JWT.
 *
 * Decodes the Base64url payload segment and extracts `"sub"` via regex. Does NOT validate the
 * signature — used only for building PostgREST filters where the JWT is also sent as Bearer
 * token (server validates it). Returns `null` on any parse failure.
 *
 * MED-002 (audit 2026-05-23): validates extracted `sub` against UUID v4 format before returning
 * it. A non-UUID sub would indicate a malformed or unexpected JWT and must not be used as a
 * PostgREST row-level-security filter key.
 *
 * Uses [Base64.getUrlDecoder] (Java 8 / API 26+; project minSdk = 28, safe to use).
 */
fun String.jwtSubject(): String? = runCatching {
    val payload = split(".").getOrNull(1) ?: return@runCatching null
    val padded = payload.padEnd((payload.length + 3) and 3.inv(), '=')
    val json = String(Base64.getUrlDecoder().decode(padded))
    val sub = Regex(""""sub"\s*:\s*"([^"]+)"""").find(json)?.groupValues?.get(1)
        ?: return@runCatching null
    // MED-002: reject non-UUID sub values (Supabase always uses UUID v4 for user IDs).
    if (!UUID_PATTERN.matches(sub)) return@runCatching null
    sub
}.getOrNull()

/**
 * Extracts the `exp` claim (Unix epoch seconds) from a compact JWS JWT.
 *
 * MED-003 (audit 2026-05-23): used to compute [AuthSession.expiresAtEpochMs] from the JWT's
 * own `exp` claim rather than `System.currentTimeMillis() + expiresIn`. Using the JWT claim
 * avoids clock-skew drift when the device time differs from the Supabase server time.
 * Returns `null` on any parse failure; callers should fall back to the `expires_in` response
 * field when null.
 */
fun String.jwtExpiryEpochMs(): Long? = runCatching {
    val payload = split(".").getOrNull(1) ?: return@runCatching null
    val padded = payload.padEnd((payload.length + 3) and 3.inv(), '=')
    val json = String(Base64.getUrlDecoder().decode(padded))
    Regex(""""exp"\s*:\s*(\d+)""").find(json)?.groupValues?.get(1)?.toLong()?.times(1_000L)
}.getOrNull()

private val UUID_PATTERN = Regex(
    "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$",
    RegexOption.IGNORE_CASE,
)
