package com.apptest.feature.auth.data.di

import com.apptest.core.data.auth.SupabaseAuthRepository
import com.apptest.core.domain.auth.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Switches from [com.apptest.feature.auth.data.FakeAuthRepository] (V1 dev)
 * to [SupabaseAuthRepository] (R-043 real integration).
 *
 * To revert to Fake for offline development, change the impl parameter back.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AuthDataModule {
    @Binds @Singleton
    abstract fun bindAuthRepository(impl: SupabaseAuthRepository): AuthRepository
}
