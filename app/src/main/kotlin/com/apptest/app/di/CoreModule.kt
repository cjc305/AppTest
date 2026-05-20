package com.apptest.app.di

import com.apptest.core.common.DefaultDispatcherProvider
import com.apptest.core.common.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt bindings for `:core:common` types. Lives in `:app` because `:core:common` is a pure
 * Kotlin (kotlin-jvm) module with no Hilt dep — keeping it that way preserves its testability
 * + zero-Android constraint.
 *
 * Any future `:core:common` interface that needs to be `@Inject`able into features should get
 * its provider here.
 */
@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()
}
