package com.apptest.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.apptest.core.data.session.DataStoreSessionStore
import com.apptest.core.data.session.SessionStore
import com.apptest.core.domain.auth.TokenProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt wiring for the auth session storage layer (`R-006`).
 *
 * - `DataStore<Preferences>` is a singleton bound to file `auth_session.preferences_pb`
 *   in app private storage.
 * - [DataStoreSessionStore] implements both [SessionStore] (writes) and [TokenProvider]
 *   (reads for `:core:network` interceptor); same instance, double-bound.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SessionBindingsModule {

    @Binds
    @Singleton
    abstract fun bindSessionStore(impl: DataStoreSessionStore): SessionStore

    @Binds
    @Singleton
    abstract fun bindTokenProvider(impl: DataStoreSessionStore): TokenProvider
}

@Module
@InstallIn(SingletonComponent::class)
object SessionDataStoreModule {

    private const val FILE_NAME = "auth_session"

    @Provides
    @Singleton
    fun provideAuthDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile(FILE_NAME) },
        )
}
