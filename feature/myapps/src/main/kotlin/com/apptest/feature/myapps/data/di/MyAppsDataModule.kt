package com.apptest.feature.myapps.data.di

import com.apptest.feature.myapps.data.FakeMyAppsRepository
import com.apptest.feature.myapps.data.MyAppsRepository
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
    abstract fun bindMyAppsRepository(impl: FakeMyAppsRepository): MyAppsRepository
}
