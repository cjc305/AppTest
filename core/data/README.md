# :core:data

> Shared adapter layer: persistent storage for cross-feature state (currently auth session) +
> future `:core:data`-owned aggregate repositories.

## Use it when

- 任何 feature 需要讀取目前 auth session → 注 `SessionStore`
- `:feature:auth` 寫真實 Supabase Auth 完成後 → call `SessionStore.save(...)`
- `:core:network` 的 `Authorization` header 自動帶 token → 注 `TokenProvider`（impl 同 `SessionStore`）
- 未來：跨 feature 共用的 aggregate repository（Profile / Credits / Reputation）會放這

## Don't use it for

- Feature 內部 repository（仍放 `:feature:X/data/`）
- 一次性 cache（用 Room 的話放 `:core:database`，記憶體用 ViewModel + `stateIn`）
- DI 對 feature-local repository 的 binding（feature 自己 di module）

## Key concepts

- **`AuthSession`** — data class，封裝 jwt / refresh / expiresAtEpochMs；`isExpired()` 純函式判定。
- **`SessionStore`** — interface (read Flow + save + clear)；feature:auth 與 :app 使用。
- **`TokenProvider`** (in :core:domain) — read-only `suspend fun token(): String?`；`:core:network` 使用。
- **`DataStoreSessionStore`** — Preferences-DataStore-backed singleton，**同時** 實作 `SessionStore` + `TokenProvider`，Hilt 雙 `@Binds` 同一個 instance。
- **`SessionModule`** — Hilt module 提供 `DataStore<Preferences>` (file: `auth_session.preferences_pb`) + 兩個 binding。

## Quick example

```kotlin
// :feature:auth/RealAuthRepository (lands with APT-V1-R-043)
class RealAuthRepository @Inject constructor(
    private val sessionStore: SessionStore,
    private val supabase: SupabaseClient,
) : AuthRepository {
    override suspend fun verifyMagicLink(token: String): AppResult<Unit> = runCatchingApp {
        val response = supabase.auth.verifyOtp(token)
        sessionStore.save(
            AuthSession(
                jwt = response.accessToken,
                refreshToken = response.refreshToken,
                expiresAtEpochMs = response.expiresAt * 1000,
            )
        )
    }
}

// :core:network AuthInterceptor (R-007)
class AuthInterceptor @Inject constructor(
    private val tokenProvider: TokenProvider,
) : Interceptor { /* attaches Bearer header per request */ }
```

## Related

- spec_ref: [`_specs/backend_architecture.md`](../../_specs/backend_architecture.md) §1 + §7 — auth secrets
- depends on: `:core:common` / `:core:domain` / `:core:network` (build) / `:core:database` (build) / DataStore Preferences / Hilt
- dependents: `:feature:auth` (writer) / `:app` (reader) / `:core:network` (via `TokenProvider`)
- 完整依賴 / 替換 / 測試見 [`DEPENDENCY.md`](DEPENDENCY.md)
