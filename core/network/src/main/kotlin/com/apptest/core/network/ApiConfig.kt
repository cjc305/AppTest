package com.apptest.core.network

/**
 * Network base URLs per `_specs/api_contracts.md` §1.
 *
 * Real production values are wired via [com.apptest.app.BuildConfig] at app-module level once
 * the Supabase / Ktor projects are provisioned (APT-OPS-001). Until then the placeholders
 * below are used for build-success but no network call will actually succeed.
 */
object ApiConfig {
    /** Ktor matching service base URL. Path-versioned (`/v1`). */
    const val KTOR_BASE_URL = "https://api.apptest.dev/v1/"

    /** Supabase project REST base URL — apptest-prod (ap-southeast-1, Singapore). */
    const val SUPABASE_REST_BASE_URL = "https://jefgixmmlqtgbxobukkt.supabase.co/rest/v1/"

    /** Supabase Realtime WebSocket base URL. */
    const val SUPABASE_REALTIME_URL = "wss://jefgixmmlqtgbxobukkt.supabase.co/realtime/v1"

    /** Supabase Auth base URL. */
    const val SUPABASE_AUTH_URL = "https://jefgixmmlqtgbxobukkt.supabase.co/auth/v1/"

    /** Supabase Edge Functions base URL. */
    const val SUPABASE_FUNCTIONS_URL = "https://jefgixmmlqtgbxobukkt.supabase.co/functions/v1/"

    /** Default per-request timeout, seconds. */
    const val TIMEOUT_SECONDS = 20L
}
