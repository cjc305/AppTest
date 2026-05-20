# :core:database — Dependencies

> Module DAG + 替換 + 測試。

## I depend on

| Dep | Why |
|---|---|
| `:core:common` (implementation) | `AppResult`（測試端 wrap 用） |
| `androidx.room:room-runtime` (api) | Room |
| `androidx.room:room-ktx` (api) | suspend + Flow extensions |
| `androidx.room:room-compiler` (ksp) | DAO 代碼生成 |
| `com.google.dagger:hilt-android` | DI |

`api` 對 Room runtime/ktx：feature inject DAO 拿到 type 不需重宣告 Room dep。

## Modules depending on me

| Module | Uses what |
|---|---|
| `:core:data` (build dep — reserved) | 未來 shared aggregate repo 可能讀 cache |
| 任何 feature 要 cache 的 — inject `AppCacheDao` |

## How to replace

### Add a typed entity（例 R-044 match_cache）

1. `entity/MatchCacheEntry.kt` — `@Entity` data class
2. `dao/MatchCacheDao.kt` — `@Dao` interface
3. `AppDatabase.kt`：
   - `entities = [AppCacheEntry::class, MatchCacheEntry::class]`
   - `version = 2`
   - `abstract fun matchCacheDao(): MatchCacheDao`
4. 寫 `Migration(1, 2) { db -> db.execSQL("CREATE TABLE match_cache (...)") }`
5. `DatabaseModule.@Provides fun provideMatchCacheDao(db)`
6. 加 `MigrationTestHelper` 測試

### Switch to SQLDelight / drift

不建議。Room + ksp 對 Android 友善，TypeConverters / Migration 路線成熟。SQLDelight 對 KMP 有優勢但 V1 不需要。

### Encrypt the DB

1. 加 SQLCipher `androidx.room:room-sqlcipher`（額外 dep）
2. `Room.databaseBuilder(...).openHelperFactory(SupportFactory(passphrase))`
3. Passphrase 用 Android Keystore alias 生 + 存

V1 不做（cache 都是已脫敏資料，加密成本 > 收益）。

## How to test

| Test type | Tool | Scope |
|---|---|---|
| **DAO** | Robolectric + JUnit + Room in-memory builder | upsert / get / clear / observe 行為 |
| **Migration** | `MigrationTestHelper` | every Migration(N→N+1) 跑一輪 |
| **Schema** | git diff `schemas/` | review schema bump 是否有對應 migration |

In-memory builder example:
```kotlin
val db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
    .allowMainThreadQueries()
    .build()
```

## File budget

| File | Lines | Notes |
|---|---:|---|
| `AppDatabase.kt` | ~35 | abstract + 1 dao fn + KDoc |
| `entity/AppCacheEntry.kt` | ~22 | 3 fields |
| `dao/AppCacheDao.kt` | ~28 | 5 methods |
| `di/DatabaseModule.kt` | ~32 | 2 providers |

每檔 ≤ 200 ✓。

## Deferred / not in scope

- **Typed entities for V1 feed cache** — 真有效能瓶頸再加，AppCacheEntry 夠用
- **`MigrationTestHelper` 接 CI** — APT-X-003 統一處理
- **SQLCipher encryption** — 同上「encrypt the DB」評估
- **Multi-tenant partitioning** — V3 全球 / 多帳號 才考慮
- **Background prune of stale entries** — V2 加 WorkManager job（V1 caller-side 重 upsert 自然覆蓋舊行）
