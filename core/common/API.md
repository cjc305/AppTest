# :core:common — Public API

> 對外可 import 的 types / functions。Internal helpers 不列。

## `AppResult<T>` (sealed interface)

```kotlin
sealed interface AppResult<out T> {
    data class Success<T>(val value: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}
```

### Extensions
| Fn | Signature | Notes |
|---|---|---|
| `getOrNull()` | `AppResult<T>.() -> T?` | branchless 取值 |
| `errorOrNull()` | `AppResult<T>.() -> AppError?` | branchless 取錯 |
| `map(transform)` | `(T) -> R` | Success-only 變換 |
| `flatMap(transform)` | `(T) -> AppResult<R>` | 鏈接需錯誤短路的計算 |
| `onSuccess(action)` | `(T) -> Unit` | side-effect on success，回傳原 result |
| `onFailure(action)` | `(AppError) -> Unit` | side-effect on failure |

### Adapter
```kotlin
inline fun <T> runCatchingApp(block: () -> T): AppResult<T>
```
Wraps a throwing thunk. **Re-throws `CancellationException`** per coroutine cancel 合約 — 不可吞。

## `AppError` (sealed class)

| Subtype | Fields | When |
|---|---|---|
| `Network` | message?, cause? | IOException / 連線失敗 |
| `Http(code, message)` | code: Int | 4xx/5xx 已收到 response |
| `Auth(reason)` | reason: AuthReason | 認證類 |
| `Validation(field, message)` | field: String | 欄位驗證 |
| `NotFound(resource)` | resource: String | 404 等資源不存在 |
| `Forbidden` | message? | 已認證但無權限 |
| `Conflict` | message? | 409 / 衝突 |
| `RateLimited(retryAfterSeconds)` | Long? | 429 |
| `Unknown` | message?, cause? | fallback |

`AppError.AuthReason`: `Unauthenticated | TokenExpired | InvalidCredential | SignInCancelled`

`AppError.fromThrowable(t: Throwable): AppError` — 把 IOException 收進 Network，其他歸 Unknown。

## `DispatcherProvider` (interface)

```kotlin
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val unconfined: CoroutineDispatcher
}

class DefaultDispatcherProvider : DispatcherProvider   // production binding
```

## `AuthState` (enum)

```kotlin
enum class AuthState { SignedOut, NeedsOnboarding, Ready }
```
Routing-level state，非 domain 狀態。Producer = `:feature:auth/AuthRepository`；Consumer = `:app` 與 `:core:navigation/startDestinationFor`。

## `ReputationTier` (enum)

```kotlin
enum class ReputationTier { Newcomer, Bronze, Silver, Gold, Platinum
    companion object { fun fromScore(score: Int): ReputationTier }
}
```
Breakpoints: 0-199 / 200-399 / 400-599 / 600-799 / 800-1000。Newcomer 的「首 7 天」窗判定由 caller 自管。

## `AppStrings` (data class)

i18n catalog — 一 record 全部 V1 用得到的字串 keys。Compose 端透過 `:core:designsystem` 的 `LocalAppStrings` 取值。

```kotlin
data class AppStrings(/* …大量 val …: String */)
object AppStringsCatalog {
    val EN: AppStrings
    val ZH_TW: AppStrings
}
```

新增字串：見 `AppStrings.kt` 檔頭 KDoc。Pure data；無 Compose 依賴。

## NOT public (internal)

- 無 internal extension — 本模組刻意 100% public surface（純 contract layer）。
