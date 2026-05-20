# :core:database

> Room base: `AppDatabase` + generic `AppCacheEntry` key-value table + Hilt wiring.
> 後續所有需要本地持久化的 feature（match cache / proofs / test requests）在這加 entity + DAO。

## Use it when

- Feature 想 cache HTTP response 跨 process death → 用 `AppCacheDao`（key + JSON blob + ttl）
- Feature 需要 typed table（如 R-044 match_cache）→ 加 entity 在 `entity/` + DAO 在 `dao/`
- 任何 cross-feature 共用的本地表（V2 reputation history）→ 也加在這

## Don't use it for

- 一次性記憶體 cache — 用 ViewModel `stateIn`，不要走 Room
- Auth session 持久化 — 已在 `:core:data/SessionStore`（DataStore Preferences；Room 太重）
- 圖片 cache — Coil 自己有 disk cache，不要重發明

## Key concepts

- **`AppDatabase`** — Room database singleton。新 entity 必須註冊在 `entities = [...]` 並 bump version + 寫 migration。
- **`AppCacheEntry`** — generic key/value/payloadJson/updatedAtMs entity。Caller 負責 namespace + TTL + 序列化。
- **`AppCacheDao`** — get / observe (Flow) / upsert / delete / clear。
- **Schema export** — `exportSchema = true`，schema JSON 自動寫到 `core/database/schemas/`（已加 ksp arg）。Migration review 走 git diff。
- **`fallbackToDestructiveMigration(dropAllTables = false)`** — V1 dev-only 安全網；正式 release **必須** 改成嚴格 migration（移除這行 → 強迫寫 Migration）。

## Quick example

```kotlin
// In :feature:home/data
class HomeRepositoryImpl @Inject constructor(
    private val api: HomeApi,
    private val cache: AppCacheDao,
) : HomeRepository {

    private val CACHE_KEY = "home:feed:v1"
    private val TTL_MS = 5 * 60 * 1000L  // 5 min

    override suspend fun getMatchedFeed(): AppResult<List<MatchedApp>> {
        cache.get(CACHE_KEY)?.takeIf { (System.currentTimeMillis() - it.updatedAtEpochMs) < TTL_MS }
            ?.let { return AppResult.Success(AppJson.decodeFromString(it.payloadJson)) }

        return runCatchingApp { api.matches().toDomain() }
            .onSuccess { data ->
                cache.upsert(AppCacheEntry(CACHE_KEY, AppJson.encodeToString(data), System.currentTimeMillis()))
            }
    }
}
```

## Related

- spec_ref: [`_specs/database_schema.md`](../../_specs/database_schema.md) — 主資料庫在 Supabase 端，這只是 client-side cache
- depends on: `:core:common` / Room runtime + ktx + compiler / Hilt
- dependents: `:core:data` (build dep) / 任何要 cache 的 feature
- 完整 [`DEPENDENCY.md`](DEPENDENCY.md)
