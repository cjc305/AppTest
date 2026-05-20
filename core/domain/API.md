# :core:domain — Public API

> 對外可 import。本模組純 contract，無 impl。

## Base types

### `UseCase<in P, out R>`

```kotlin
abstract class UseCase<in P, R>(private val dispatchers: DispatcherProvider) {
    suspend operator fun invoke(params: P): AppResult<R>
    protected abstract suspend fun execute(params: P): AppResult<R>
}
```
- `invoke` 已包 `withContext(dispatchers.io)`
- `CancellationException` 重 throw（per coroutine 合約）
- 其他 throwable → `AppResult.Failure(AppError.fromThrowable(t))`
- Subclass **不應** 自己 try/catch — base 已處理

### `NoParamUseCase<R>`

```kotlin
abstract class NoParamUseCase<R>(dispatchers: DispatcherProvider) : UseCase<Unit, R>(dispatchers) {
    suspend operator fun invoke(): AppResult<R>
}
```
語法糖 — 給不需參數的 use case。

### `VoidUseCaseResult`

```kotlin
typealias VoidUseCaseResult = AppResult<Unit>
```
給回傳成功與否、無資料的 use case 用（如 `MarkOnboardingCompleteUseCase`）。

## Marker

### `Repository`

```kotlin
interface Repository
```
僅作分類標記。實作端慣例：
- 方法回傳 `AppResult<T>`，**永不** throw
- suspend 或 `Flow<T>`，無 callback
- 每個 aggregate root 一 Repository（User / App / TestRequest / Review）
- Feature-local Repository impl 放 `feature/X/data/`；shared Repository 放 `:core:data`

## Cross-feature contracts

### `auth.AuthRepository`

```kotlin
interface AuthRepository : Repository {
    val state: StateFlow<AuthState>
    suspend fun signInWithGoogle(): AppResult<Unit>
    suspend fun requestMagicLink(email: String): AppResult<Unit>
    suspend fun verifyMagicLink(token: String): AppResult<Unit>
    suspend fun markOnboardingComplete(): AppResult<Unit>
    suspend fun signOut(): AppResult<Unit>
}
```

**Producer:** `:feature:auth`（V1 = `FakeAuthRepository`；APT-V1-R-043 替換為 Supabase 真實 impl）
**Consumers:**
- `:app/MainActivity` — observe `state` 驅動 NavHost startDestination
- `:feature:onboarding` — 呼叫 `markOnboardingComplete()`
- `:feature:profile` — 未來呼叫 `signOut()`

State machine：見 `AuthRepository.kt` KDoc + [`_specs/navigation.md`](../../_specs/navigation.md) §5。

## NOT public (internal)

- 無。本模組所有 type 均為跨層 contract，必須 public。

## 未來會放這的 (候選)

當下列 repository 真的被 ≥ 2 個 feature 引用時，從 feature 移到 `:core:domain`：
- `ProfileRepository`（目前 `:feature:profile`） — 未來 `:feature:onboarding` 寫 draft + profile 讀
- `CreditsRepository` — V2 多 feature 顯示 credits
- `ReputationRepository` — V2 多處顯示分數

YAGNI 原則：**只有兩個以上 feature 真的引用才搬**。
