package com.apptest.feature.profile.data.di

import com.apptest.feature.profile.data.ProfileRepository
import com.apptest.feature.profile.data.SupabaseProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileDataModule {
    @Binds @Singleton
    abstract fun bindProfileRepository(impl: SupabaseProfileRepository): ProfileRepository
}
