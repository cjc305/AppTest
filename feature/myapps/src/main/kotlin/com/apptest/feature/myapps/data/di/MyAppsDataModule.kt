package com.apptest.feature.myapps.data.di

import com.apptest.feature.myapps.data.MyAppsRepository
import com.apptest.feature.myapps.data.SupabaseMyAppsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MyAppsDataModule {

    @Binds
    @Singleton
    abstract fun bindMyAppsRepository(impl: SupabaseMyAppsRepository): MyAppsRepository
}
