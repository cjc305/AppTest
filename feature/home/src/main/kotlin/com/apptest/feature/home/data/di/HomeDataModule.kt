package com.apptest.feature.home.data.di

import com.apptest.feature.home.data.FakeHomeRepository
import com.apptest.feature.home.data.HomeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt binding for [HomeRepository].
 * V1: binds [FakeHomeRepository]. Swap with real impl by changing this one line.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class HomeDataModule {

    @Binds
    @Singleton
    abstract fun bindHomeRepository(impl: FakeHomeRepository): HomeRepository
}
