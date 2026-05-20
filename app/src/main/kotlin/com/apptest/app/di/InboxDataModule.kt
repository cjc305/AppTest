package com.apptest.app.di

import com.apptest.core.data.inbox.SupabaseInboxRepository
import com.apptest.core.domain.inbox.InboxRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class InboxDataModule {
    @Binds @Singleton
    abstract fun bindInboxRepository(impl: SupabaseInboxRepository): InboxRepository
}
