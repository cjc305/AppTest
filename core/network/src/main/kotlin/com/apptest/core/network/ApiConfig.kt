package com.apptest.core.network

/**
 * Network configuration constants.
 *
 * Supabase URLs are injected at runtime from [com.apptest.app.BuildConfig]
 * (populated from `local.properties` at compile time — never hardcoded).
 * See [com.apptest.app.di.SupabaseModule] + [di.NetworkModule] for wiring.
 */
object ApiConfig {
    /** Ktor matching service base URL. Path-versioned (`/v1`). */
    const val KTOR_BASE_URL = "https://api.apptest.dev/v1/"

    /** Default per-request timeout, seconds. */
    const val TIMEOUT_SECONDS = 20L
}
