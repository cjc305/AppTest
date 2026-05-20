package com.apptest.feature.appdetail.data.di

import com.apptest.feature.appdetail.data.AppDetailRepository
import com.apptest.feature.appdetail.data.FakeAppDetailRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppDetailDataModule {

    @Binds
    @Singleton
    abstract fun bindAppDetailRepository(impl: FakeAppDetailRepository): AppDetailRepository
}
