package com.apptest.feature.inbox.data.di

import com.apptest.feature.inbox.data.InboxRepository
import com.apptest.feature.inbox.data.SupabaseInboxRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Switches from [FakeInboxRepository] (V1 dev) to [SupabaseInboxRepository] (R-044 real).
 *
 * To revert to Fake for offline development:
 * change the impl parameter back to [com.apptest.feature.inbox.data.FakeInboxRepository].
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class InboxDataModule {
    @Binds @Singleton
    abstract fun bindInboxRepository(impl: SupabaseInboxRepository): InboxRepository
}
