package com.apptest.app.di

import com.apptest.core.data.auth.SupabaseAuthRepository
import com.apptest.core.domain.auth.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds [SupabaseAuthRepository] → [AuthRepository] in the app's DI graph.
 * Lives in :app so :feature:auth only depends on :core:domain (the interface),
 * not on :core:data (the implementation).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AuthDataModule {
    @Binds @Singleton
    abstract fun bindAuthRepository(impl: SupabaseAuthRepository): AuthRepository
}
