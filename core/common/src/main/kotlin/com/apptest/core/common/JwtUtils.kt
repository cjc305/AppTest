package com.apptest.core.common

import java.util.Base64

/**
 * Extracts the `sub` claim (Supabase user UUID) from a compact JWS JWT.
 *
 * Decodes the Base64url payload segment and extracts `"sub"` via regex. Does NOT validate the
 * signature — used only for building PostgREST filters where the JWT is also sent as Bearer
 * token (server validates it). Returns `null` on any parse failure.
 *
 * Uses [Base64.getUrlDecoder] (Java 8 / API 26+; project minSdk = 28, safe to use).
 */
fun String.jwtSubject(): String? = runCatching {
    val payload = split(".").getOrNull(1) ?: return@runCatching null
    // Supabase JWTs follow RFC-7515 base64url-without-padding; java.util.Base64.getUrlDecoder()
    // is strict and throws when length % 4 != 0. Pad to a multiple of 4 first.
    val padded = payload.padEnd((payload.length + 3) and 3.inv(), '=')
    val json = String(Base64.getUrlDecoder().decode(padded))
    Regex(""""sub"\s*:\s*"([^"]+)"""").find(json)?.groupValues?.get(1)
}.getOrNull()
