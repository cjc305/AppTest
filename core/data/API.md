# :core:data — Public API

> 對外可注入的 contract。Internal impl 不列。

## `AuthSession` (data class)

```kotlin
data class AuthSession(
    val jwt: String,
    val refreshToken: String,
    val expiresAtEpochMs: Long,
) {
    fun isExpired(nowEpochMs: Long = System.currentTimeMillis()): Boolean
}
```

Pure value type。**不要** 把它放 ViewModel UI state — 用者只該觀察 `SessionStore.session`。

## `SessionStore` (interface)

```kotlin
interface SessionStore {
    val session: Flow<AuthSession?>
    suspend fun save(session: AuthSession)
    suspend fun clear()
}
```

| Method | Threading | Notes |
|---|---|---|
| `session` | cold Flow | 內部 DataStore 已切到 IO；collect 端可在任意 dispatcher |
| `save(...)` | suspend, IO | DataStore atomic write |
| `clear()` | suspend, IO | idempotent — 連 clear 兩次 OK |

## `TokenProvider` (declared in `:core:domain/auth`)

```kotlin
interface TokenProvider {
    suspend fun token(): String?    // null = unauthenticated 或 expired
}
```

Production impl = `DataStoreSessionStore`（同 SessionStore）。每 request 重 call，**不** cache。

## Hilt bindings (auto-wired via `SessionModule`)

| Inject 點 | 拿到的 type | 實際 instance |
|---|---|---|
| `@Inject SessionStore` | `SessionStore` | `DataStoreSessionStore` (singleton) |
| `@Inject TokenProvider` | `TokenProvider` | 同上 instance |
| `@Inject DataStore<Preferences>` | `DataStore<Preferences>` | 内部建構，理論上不該被 feature 直接拿，僅供 module 用 |

## NOT public (internal)

- `DataStoreSessionStore` — concrete class，從 Hilt 注 interface 即可。直接 import class 名違反封裝。
- `SessionDataStoreModule` / `SessionBindingsModule` — Hilt module，無 caller-facing 用途。

## 未來會放這的

- `ProfileRepository` impl（若跨 feature 共用 — 目前在 `:feature:profile`）
- `CreditsRepository`（V2 多處顯示 credits）
- `ReputationRepository`（V2 多處讀 reputation breakdown）

當下 YAGNI — 兩 feature 真正需要才搬。
