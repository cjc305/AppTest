package com.apptest.core.common

import javax.inject.Qualifier

/**
 * Hilt qualifier for the Google OAuth 2.0 **Web** Client ID used by Credential Manager.
 *
 * Bound in `:app/di/GoogleAuthModule` from `BuildConfig.GOOGLE_WEB_CLIENT_ID`
 * (read from `local.properties` at build time — never hardcoded or committed).
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GoogleWebClientId
