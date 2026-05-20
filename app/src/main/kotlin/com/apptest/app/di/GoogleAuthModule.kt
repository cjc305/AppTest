package com.apptest.app.di

import com.apptest.app.BuildConfig
import com.apptest.core.common.GoogleWebClientId
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Provides the Google OAuth 2.0 Web Client ID for Credential Manager.
 *
 * Value comes from `BuildConfig.GOOGLE_WEB_CLIENT_ID`, which is set in
 * `app/build.gradle.kts` from `local.properties` (never committed to git).
 *
 * Setup steps (one-time, per environment):
 * 1. Firebase Console → apptest-7fced → Authentication → Sign-in method → Google → Enable
 * 2. Copy "Web SDK configuration" → Web client ID
 * 3. Add to `local.properties`: GOOGLE_WEB_CLIENT_ID=<paste_here>
 * 4. Add SHA-1 of debug keystore to Firebase Console → Project settings → Android app
 */
@Module
@InstallIn(SingletonComponent::class)
object GoogleAuthModule {

    @Provides
    @GoogleWebClientId
    fun provideGoogleWebClientId(): String = BuildConfig.GOOGLE_WEB_CLIENT_ID
}
