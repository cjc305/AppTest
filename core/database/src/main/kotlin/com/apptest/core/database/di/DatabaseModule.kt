package com.apptest.core.database.di

import android.content.Context
import androidx.room.Room
import com.apptest.core.database.AppDatabase
import com.apptest.core.database.dao.AppCacheDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt wiring for Room. Single-instance database (`apptest.db`) provided as singleton;
 * each DAO is a thin provider that reads the singleton.
 *
 * Migrations register here once we bump schema versions — keep them ordered + idempotent.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DATABASE_NAME = "apptest.db"

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
            // V1 dev-only: destructive migration. Replace with proper Migration(N, N+1) before
            // release per `_specs/database_schema.md` §migration policy. CI lint (post-X-002)
            // should fail release builds that still have this call.
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideAppCacheDao(database: AppDatabase): AppCacheDao = database.appCacheDao()
}
