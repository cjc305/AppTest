package com.apptest.core.network

/**
 * Network configuration constants.
 *
 * Supabase URLs are injected at runtime from [com.apptest.app.BuildConfig]
 * (populated from `local.properties` at compile time — never hardcoded).
 * See [com.apptest.app.di.SupabaseModule] + [di.NetworkModule] for wiring.
 */
object ApiConfig {
    /**
     * Ktor matching service base URL.
     * Cloud Run: https://apptest-backend-726162458626.asia-northeast1.run.app
     * Custom domain (future): https://api.apptest.dev
     */
    const val KTOR_BASE_URL = "https://apptest-backend-726162458626.asia-northeast1.run.app/"

    /** Default per-request timeout, seconds. */
    const val TIMEOUT_SECONDS = 20L
}
