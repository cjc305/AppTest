package com.apptest.core.network.di

import com.apptest.core.network.ApiConfig
import com.apptest.core.network.AppJson
import com.apptest.core.network.auth.SupabaseAuthApiService
import com.apptest.core.network.notifications.SupabaseNotificationsApiService
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

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }

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

    /** Auth Retrofit points to `/auth/v1/` — reuses the Supabase OkHttp client (apikey + JWT). */
    @Provides
    @Singleton
    @SupabaseAuth
    fun provideSupabaseAuthRetrofit(
        @SupabaseRest supabaseClient: OkHttpClient,
        @SupabaseBaseUrl baseUrl: String,
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl("$baseUrl/auth/v1/")
            .client(supabaseClient)
            .addConverterFactory(AppJson.asConverterFactory(MEDIA_TYPE_JSON.toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideSupabaseAuthApiService(@SupabaseAuth retrofit: Retrofit): SupabaseAuthApiService =
        retrofit.create(SupabaseAuthApiService::class.java)

    /** Notifications CRUD — uses [@SupabaseRest][SupabaseRest] Retrofit (PostgREST base URL). */
    @Provides
    @Singleton
    fun provideSupabaseNotificationsApiService(
        @SupabaseRest retrofit: Retrofit,
    ): SupabaseNotificationsApiService =
        retrofit.create(SupabaseNotificationsApiService::class.java)

    /** Active-match queries + abandon — used by heartbeat worker (R-040). */
    @Provides
    @Singleton
    fun provideSupabaseTestingApiService(
        @SupabaseRest retrofit: Retrofit,
    ): SupabaseTestingApiService =
        retrofit.create(SupabaseTestingApiService::class.java)
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
