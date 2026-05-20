package com.apptest.feature.testing.data.di

import com.apptest.feature.testing.data.FakeTestingRepository
import com.apptest.feature.testing.data.TestingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TestingDataModule {
    @Binds @Singleton
    abstract fun bindTestingRepository(impl: FakeTestingRepository): TestingRepository
}
