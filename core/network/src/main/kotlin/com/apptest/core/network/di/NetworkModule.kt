package com.apptest.core.network.di

import com.apptest.core.network.ApiConfig
import com.apptest.core.network.AppJson
import com.apptest.core.network.apps.SupabaseAppsApiService
import com.apptest.core.network.auth.SupabaseAuthApiService
import com.apptest.core.network.matches.SupabaseMatchesApiService
import com.apptest.core.network.notifications.SupabaseNotificationsApiService
import com.apptest.core.network.profiles.SupabaseProfilesApiService
import com.apptest.core.network.testing.SupabaseTestingApiService
import com.apptest.core.network.interceptor.AuthInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

/**
 * Network singletons per `_specs/api_contracts.md` §1.
 *
 * Two Retrofit instances + two OkHttp clients:
 * - [KtorApi]    → Ktor matching engine (Bearer JWT only)
 * - [SupabaseRest] → Supabase PostgREST (apikey header + Bearer JWT for auth'd calls)
 *
 * Supabase requires `apikey: <anon_key>` on every request; the anon key is bound
 * by [SupabaseModule] in `:app` from BuildConfig (local.properties → compile-time).
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val MEDIA_TYPE_JSON = "application/json; charset=utf-8"
    private const val HEADER_API_KEY = "apikey"

    // LOW-1: BASIC level logs URL + status — gated on BuildConfig.DEBUG so release builds
    // don't leak request URLs to logcat (which manufacturer telemetry may collect).
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (com.apptest.core.network.BuildConfig.DEBUG)
            HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
    }
    // HIGH-2 host allowlist provider lives in [AuthAllowedHostsModule].

    /** Shared OkHttp for Ktor — auth JWT only. */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(ApiConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(ApiConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(ApiConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    /** Supabase OkHttp — adds `apikey` header; auth JWT added by [AuthInterceptor]. */
    @Provides
    @Singleton
    @SupabaseRest
    fun provideSupabaseOkHttpClient(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
        @SupabaseAnonKey anonKey: String,
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(ApiConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(ApiConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(ApiConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            chain.proceed(chain.request().newBuilder().header(HEADER_API_KEY, anonKey).build())
        }
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    @Provides
    @Singleton
    @KtorApi
    fun provideKtorRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(ApiConfig.KTOR_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(AppJson.asConverterFactory(MEDIA_TYPE_JSON.toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideBackendStatsApiService(
        @KtorApi retrofit: Retrofit,
    ): com.apptest.core.network.backend.BackendStatsApiService =
        retrofit.create(com.apptest.core.network.backend.BackendStatsApiService::class.java)

    @Provides
    @Singleton
    @SupabaseRest
    fun provideSupabaseRetrofit(
        @SupabaseRest supabaseClient: OkHttpClient,
        @SupabaseBaseUrl baseUrl: String,
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl("$baseUrl/rest/v1/")
            .client(supabaseClient)
            .addConverterFactory(AppJson.asConverterFactory(MEDIA_TYPE_JSON.toMediaType()))
            .build()

    /**
     * Supabase Auth OkHttp — **only** apikey header. Deliberately omits [AuthInterceptor] so
     * sign-in calls (`/otp`, `/verify`, `/token?grant_type=id_token`) are never sent with a
     * residual Bearer from a previous user (cross-account token pollution). Endpoints that
     * actually need a Bearer (`/logout`, `/token?grant_type=refresh_token`) pass it explicitly
     * via the `Authorization` header parameter on [SupabaseAuthApiService].
     */
    @Provides
    @Singleton
    @SupabaseAuth
    fun provideSupabaseAuthOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        @SupabaseAnonKey anonKey: String,
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(ApiConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(ApiConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(ApiConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            chain.proceed(chain.request().newBuilder().header(HEADER_API_KEY, anonKey).build())
        }
        .addInterceptor(loggingInterceptor)
        .build()

    /** Auth Retrofit points to `/auth/v1/`. Uses the dedicated [@SupabaseAuth] OkHttp (no auto-Bearer). */
    @Provides
    @Singleton
    @SupabaseAuth
    fun provideSupabaseAuthRetrofit(
        @SupabaseAuth authClient: OkHttpClient,
        @SupabaseBaseUrl baseUrl: String,
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl("$baseUrl/auth/v1/")
            .client(authClient)
            .addConverterFactory(AppJson.asConverterFactory(MEDIA_TYPE_JSON.toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideSupabaseAuthApiService(@SupabaseAuth retrofit: Retrofit): SupabaseAuthApiService =
        retrofit.create(SupabaseAuthApiService::class.java)

    // Supabase REST API services (PostgREST base URL via [@SupabaseRest][SupabaseRest] Retrofit).
    @Provides @Singleton
    fun provideNotificationsApi(@SupabaseRest r: Retrofit): SupabaseNotificationsApiService =
        r.create(SupabaseNotificationsApiService::class.java)

    @Provides @Singleton
    fun provideTestingApi(@SupabaseRest r: Retrofit): SupabaseTestingApiService =
        r.create(SupabaseTestingApiService::class.java)

    @Provides @Singleton
    fun provideAppsApi(@SupabaseRest r: Retrofit): SupabaseAppsApiService =
        r.create(SupabaseAppsApiService::class.java)

    @Provides @Singleton
    fun provideMatchesApi(@SupabaseRest r: Retrofit): SupabaseMatchesApiService =
        r.create(SupabaseMatchesApiService::class.java)

    @Provides @Singleton
    fun provideProfilesApi(@SupabaseRest r: Retrofit): SupabaseProfilesApiService =
        r.create(SupabaseProfilesApiService::class.java)
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class KtorApi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SupabaseRest

/** Marks the Supabase anon (publishable) key. Provided by [com.apptest.app.di.SupabaseModule]. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SupabaseAnonKey

/** Marks the Supabase Auth Retrofit instance (base: `/auth/v1/`). */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SupabaseAuth

/** Marks the Supabase project base URL. Provided by [com.apptest.app.di.SupabaseModule]. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SupabaseBaseUrl
