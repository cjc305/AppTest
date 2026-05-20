package com.apptest.core.navigation

import android.net.Uri

/**
 * Deep link scheme constants + URI parser per `_specs/navigation.md §6`.
 *
 * Custom scheme: `apptest://...` (App-internal, always opens AppTest)
 * Web scheme: `https://apptest.dev/...` (App Links — verified via `assetlinks.json`)
 *
 * Bind to Compose Nav destinations via `navDeepLink { uriPattern = ... }` inside `composable<T>`.
 */
object AppDeepLink {
    const val SCHEME_CUSTOM = "apptest"
    const val SCHEME_WEB    = "https"
    const val WEB_HOST      = "apptest.dev"

    // ── URI patterns (used by navDeepLink in :app NavHost) ─────────────────
    const val PATTERN_APP_DETAIL_CUSTOM = "apptest://app/{appId}"
    const val PATTERN_APP_DETAIL_WEB    = "https://apptest.dev/app/{appId}"
    const val PATTERN_TEST_DETAIL       = "apptest://test/{testId}"
    const val PATTERN_INVITE            = "apptest://invite"   // ?ref= captured separately
    // Verify URL (apptest://verify/{proofId}) intentionally NOT routed in-app — falls to web (apptest.dev/v/<id>)

    // ── URI builders (for sharing / referrals) ─────────────────────────────
    fun appDetail(appId: String) = "apptest://app/$appId"
    fun appDetailWeb(appId: String) = "https://$WEB_HOST/app/$appId"
    fun testDetail(testId: String) = "apptest://test/$testId"
    fun inviteWithRef(ref: String) = "apptest://invite?ref=$ref"

    /**
     * Attempt to map an incoming [Uri] to an [AppDestination] for cases where in-app routing
     * needs to translate a raw URI (e.g., received from a push payload). Compose Nav's
     * `navDeepLink` handles the host case; this fn is the manual fallback / push handler.
     *
     * Returns null when URI doesn't match any known pattern.
     */
    fun parse(uri: Uri): AppDestination? {
        val scheme = uri.scheme ?: return null
        val host = uri.host
        val segments = uri.pathSegments

        return when {
            scheme == SCHEME_CUSTOM && host == "app" && segments.size == 1 ->
                AppDestination.AppDetail(appId = segments[0])

            scheme == SCHEME_WEB && host == WEB_HOST && segments.firstOrNull() == "app" && segments.size == 2 ->
                AppDestination.AppDetail(appId = segments[1])

            scheme == SCHEME_CUSTOM && host == "test" && segments.size == 1 ->
                // Lands in Testing dashboard; caller resolves entry from testId
                AppDestination.Testing

            scheme == SCHEME_CUSTOM && host == "invite" ->
                AppDestination.AuthRoot  // referral attribution side-effect handled by caller

            else -> null
        }
    }

    /** Extract referral code from an `apptest://invite?ref=xxx` URI. */
    fun extractReferralCode(uri: Uri): String? =
        if (uri.scheme == SCHEME_CUSTOM && uri.host == "invite") uri.getQueryParameter("ref") else null
}
