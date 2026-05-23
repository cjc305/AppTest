package com.apptest.core.navigation

import android.net.Uri

/**
 * Deep link scheme constants + URI parser per `_specs/navigation.md §6`.
 */
object AppDeepLink {
    const val SCHEME_CUSTOM = "apptest"
    const val SCHEME_WEB    = "https"
    const val WEB_HOST      = "apptest.dev"

    const val PATTERN_APP_DETAIL_CUSTOM = "apptest://app/{appId}"
    const val PATTERN_APP_DETAIL_WEB    = "https://apptest.dev/app/{appId}"
    const val PATTERN_TEST_DETAIL       = "apptest://test/{testId}"
    const val PATTERN_INVITE            = "apptest://invite"

    fun appDetail(appId: String) = "apptest://app/$appId"
    fun appDetailWeb(appId: String) = "https://$WEB_HOST/app/$appId"
    fun testDetail(testId: String) = "apptest://test/$testId"
    fun inviteWithRef(ref: String) = "apptest://invite?ref=$ref"

    /**
     * Safe ID shape (Supabase UUIDs / nanoids / short slugs).
     * Public so route entries can validate args *before* rendering.
     */
    val SAFE_ID: Regex = Regex("^[A-Za-z0-9_-]{1,64}$")

    private const val MAX_REF_LEN = 64

    /**
     * Validate an ID coming from ANY untrusted source (route arg, push payload, intent extra).
     * Returns the ID itself if valid, else null. Callers MUST handle null by aborting navigation.
     */
    fun safeId(raw: String?): String? = raw?.takeIf { it.matches(SAFE_ID) }

    fun parse(uri: Uri): AppDestination? {
        val scheme = uri.scheme?.lowercase() ?: return null
        val host = uri.host?.removeSuffix(".")?.lowercase()
        val segments = uri.pathSegments

        return when {
            scheme == SCHEME_CUSTOM && host == "app" && segments.size == 1 && segments[0].matches(SAFE_ID) ->
                AppDestination.AppDetail(appId = segments[0])

            scheme == SCHEME_WEB && host == WEB_HOST &&
                segments.firstOrNull() == "app" && segments.size == 2 && segments[1].matches(SAFE_ID) ->
                AppDestination.AppDetail(appId = segments[1])

            scheme == SCHEME_CUSTOM && host == "test" && segments.size == 1 && segments[0].matches(SAFE_ID) ->
                AppDestination.Testing

            scheme == SCHEME_CUSTOM && host == "invite" ->
                AppDestination.AuthRoot

            else -> null
        }
    }

    fun parse(uriString: String): AppDestination? =
        runCatching { parse(Uri.parse(uriString)) }.getOrNull()

    fun extractReferralCode(uri: Uri): String? {
        if (uri.scheme?.lowercase() != SCHEME_CUSTOM) return null
        if (uri.host?.lowercase() != "invite") return null
        val raw = uri.getQueryParameter("ref") ?: return null
        return raw.takeIf { it.length <= MAX_REF_LEN && it.matches(SAFE_ID) }
    }
}
