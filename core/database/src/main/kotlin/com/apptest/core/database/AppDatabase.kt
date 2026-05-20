package com.apptest.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.apptest.core.database.dao.AppCacheDao
import com.apptest.core.database.entity.AppCacheEntry

/**
 * Room database singleton. Schema version starts at 1; bump + add a [androidx.room.migration.Migration]
 * whenever an entity changes.
 *
 * `exportSchema = true` so migrations are reviewable in git
 * (`core/database/schemas/com.apptest.core.database.AppDatabase/<version>.json`).
 *
 * Adding an entity:
 * 1. New `@Entity` in `entity/`
 * 2. New `@Dao` in `dao/` returning entity / Flow<entity>
 * 3. Append to `entities = [...]` here
 * 4. Bump `version` + write `Migration(N, N+1)` if data must survive upgrade
 * 5. Expose `abstract fun fooDao(): FooDao`
 * 6. Wire in `DatabaseModule`
 */
@Database(
    entities = [
        AppCacheEntry::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appCacheDao(): AppCacheDao
}
