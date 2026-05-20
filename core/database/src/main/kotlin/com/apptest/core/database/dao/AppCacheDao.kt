package com.apptest.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.apptest.core.database.entity.AppCacheEntry
import kotlinx.coroutines.flow.Flow

/**
 * Generic cache DAO. Use [observe] for reactive consumers; [get] for one-shot reads.
 *
 * Caller responsibilities:
 * - Choose a stable [AppCacheEntry.key] and bump it when payload schema changes
 * - Apply own TTL via `updatedAtEpochMs` comparison
 * - Ser/de payloadJson via `:core:network/AppJson`
 */
@Dao
interface AppCacheDao {

    @Query("SELECT * FROM app_cache WHERE `key` = :key LIMIT 1")
    suspend fun get(key: String): AppCacheEntry?

    @Query("SELECT * FROM app_cache WHERE `key` = :key LIMIT 1")
    fun observe(key: String): Flow<AppCacheEntry?>

    @Upsert
    suspend fun upsert(entry: AppCacheEntry)

    @Query("DELETE FROM app_cache WHERE `key` = :key")
    suspend fun delete(key: String)

    @Query("DELETE FROM app_cache")
    suspend fun clear()
}
