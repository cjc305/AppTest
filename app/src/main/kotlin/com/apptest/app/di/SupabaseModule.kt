package com.apptest.app.di

import com.apptest.app.BuildConfig
import com.apptest.core.network.di.SupabaseAnonKey
import com.apptest.core.network.di.SupabaseBaseUrl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Wires Supabase credentials from BuildConfig (populated from local.properties at compile time)
 * into the Hilt graph.
 *
 * Only the anon key is provided here. The service_role key is NEVER exposed to the Android
 * client — it lives only in Supabase Edge Function env vars.
 */
@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @SupabaseAnonKey
    fun provideSupabaseAnonKey(): String = BuildConfig.SUPABASE_ANON_KEY

    @Provides
    @SupabaseBaseUrl
    fun provideSupabaseBaseUrl(): String = BuildConfig.SUPABASE_URL
}
