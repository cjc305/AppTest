# :core:database — Public API

> 對外可注入的 entities / DAOs。Internal helpers 不列。

## `AppDatabase` (abstract class)

```kotlin
@Database(entities = [AppCacheEntry::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appCacheDao(): AppCacheDao
}
```

實作由 Room 編譯期生成。**不要** 直接 inject `AppDatabase` — inject 各別 DAO 即可。

## `AppCacheEntry` (entity, data class)

```kotlin
@Entity(tableName = "app_cache")
data class AppCacheEntry(
    @PrimaryKey val key: String,
    val payloadJson: String,
    val updatedAtEpochMs: Long,
)
```

- `key` — caller-defined namespace，例：`"home:feed:v1"`。Schema bump 改後綴 `v2`。
- `payloadJson` — caller 用 `AppJson` ser/de
- `updatedAtEpochMs` — caller-side TTL

## `AppCacheDao` (interface)

```kotlin
@Dao
interface AppCacheDao {
    suspend fun get(key: String): AppCacheEntry?
    fun observe(key: String): Flow<AppCacheEntry?>
    suspend fun upsert(entry: AppCacheEntry)
    suspend fun delete(key: String)
    suspend fun clear()
}
```

| Method | Threading | Notes |
|---|---|---|
| `get` | suspend, IO | Room 自動切 |
| `observe` | cold Flow | emit on row change |
| `upsert` | suspend, IO | atomic |
| `delete` | suspend, IO | by key |
| `clear` | suspend, IO | 全表清空（測試 / sign-out 用） |

## Hilt provided injects

| Inject 點 | 拿到的 type |
|---|---|
| `@Inject AppCacheDao` | `AppCacheDao` |
| `@Inject AppDatabase` | `AppDatabase` (singleton — 不推薦直接 inject) |

新加 entity：
1. `entity/XxxEntry.kt`
2. `dao/XxxDao.kt`
3. `AppDatabase.entities += [XxxEntry::class]`，version++
4. `DatabaseModule.@Provides fun provideXxxDao(db) = db.xxxDao()`

## NOT public (internal)

- `DatabaseModule` 內部 helpers — 由 Hilt 透過 inject 取得，**不**直接 import

## Schema migration policy

`fallbackToDestructiveMigration(dropAllTables = false)` 暫存 V1 dev — release 前 **必須** 改寫 `Migration(N, N+1)` 並從 builder 移除 fallback。CI 應加 lint 防止它留到 production build（待 APT-X-002 之後加 detekt rule）。
