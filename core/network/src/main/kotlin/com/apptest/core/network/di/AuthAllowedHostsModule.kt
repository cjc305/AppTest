package com.apptest.core.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

/**
 * HIGH-2 (audit 2026-05-23): the Supabase JWT carries row-level-security claims — sending it
 * to any other host (Ktor backend, third-party APIs) is a cross-domain token leak. The
 * AuthInterceptor consults this allowlist on every request; hosts outside it get no
 * Authorization header.
 *
 * Derived from the configured Supabase URL so it stays in sync with the active backend.
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthAllowedHostsModule {

    @Provides
    @Singleton
    @Named("auth_interceptor_allowed_hosts")
    fun provideAllowedHosts(@SupabaseBaseUrl baseUrl: String): Set<String> {
        val host = baseUrl
            .removePrefix("https://").removePrefix("http://")
            .substringBefore("/")
            .substringBefore(":")
        return setOf(host)
    }
}
