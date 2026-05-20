# :core:common

> 純 kotlin-jvm 公共原語：`AppResult` / `AppError` / `DispatcherProvider` / `AuthState` / `ReputationTier` / `AppStrings`。
> No Android dep。No Compose dep。所有 module 可吃，不會把 Android library 拖進 backend / test infra。

## Use it when

- Repository / UseCase 要回傳 result → 用 `AppResult<T>`，不丟 exception
- 跨層次需要分類錯誤 → 用 `AppError` sealed 階層（Network/Http/Auth/Validation/...）
- 任何 suspend 程式要切 dispatcher → 注入 `DispatcherProvider`，**不要直接** call `Dispatchers.IO`
- UI 要顯示 reputation tier → 用 `ReputationTier` enum + `fromScore()` mapper
- UI 要顯示 i18n 字串 → 用 `AppStrings` data class（en + zh-TW 兩本 catalog 內建於本模組）
- 跨 module 要傳遞 auth 路由態 → 用 `AuthState` enum

## Don't use it for

- Compose / Android-specific helper（放 `:core:designsystem` 或 `:core:ui`）
- Domain-shaped 介面如 Repository marker（放 `:core:domain`）
- Networking client setup（放 `:core:network`）
- 一次性 feature-local enum / data class（放在 feature 自己模組裡）

## Key concepts

- **`AppResult<T>`** — sealed `Success<T>` / `Failure(AppError)`，配 `map` / `flatMap` / `onSuccess` / `onFailure` / `runCatchingApp`。`CancellationException` 永遠重 throw。
- **`AppError`** — 9 個分類 + `AuthReason` enum 細分 auth fail。`fromThrowable(t)` 為唯一 adapter 入口。
- **`DispatcherProvider`** — 4 個 dispatcher (main/io/default/unconfined)。`DefaultDispatcherProvider` 為 production binding；test 注 `UnconfinedTestDispatcher`。
- **`AuthState`** — 3 case routing-level enum，放這裡是為了避免 `:core:domain` ↔ `:core:navigation` cycle（兩邊都需引用）。
- **`ReputationTier`** — 5 階 + `fromScore()`，放這裡是因為 UI（AppTierBadge）與 domain（Reputation 計算）兩邊都需要。
- **`AppStrings`** — i18n catalog data class，兩個 const `EN` 與 `ZH_TW` 內建。Compose CompositionLocal 包裝在 `:core:designsystem`。

## Quick example

```kotlin
class JoinTestUseCase @Inject constructor(
    private val repo: TestRequestRepository,
    dispatchers: DispatcherProvider,
) : UseCase<JoinTestUseCase.Params, TestRequest>(dispatchers) {

    override suspend fun execute(params: Params): AppResult<TestRequest> =
        repo.join(params.appId, params.testerId)
            .onFailure { err ->
                when (err) {
                    is AppError.RateLimited -> /* show retry-after */ Unit
                    is AppError.Validation -> /* highlight field */ Unit
                    else -> Unit
                }
            }

    data class Params(val appId: String, val testerId: String)
}
```

## Related

- depends on: `kotlinx-coroutines-core` only — 無 Android、無 Compose
- dependents: 幾乎所有 module（`:core:domain` / `:core:data` / `:core:ui` / 每個 `:feature:*` / `:app`）
- 完整依賴 / 替換 / 測試見 [`DEPENDENCY.md`](DEPENDENCY.md)
