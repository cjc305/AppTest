# :core:domain

> 跨 feature 共享的 domain contracts：`UseCase` / `Repository` marker + 跨 feature 介面（目前 `AuthRepository`）。
> 純 kotlin-jvm。每 UseCase 一檔、≤ 50 行。

## Use it when

- 寫 use case → extend `UseCase<P, R>` 或 `NoParamUseCase<R>`
- 兩個以上 feature 都要存取的 repository contract → 介面定義在這
- Feature A 要呼叫 feature B 的能力 → 介面提到這（**禁止** feature A import feature B；走 `:core:domain` 中介）
- 例：`:feature:auth` 寫 `AuthRepository`，`:feature:onboarding` 與 `:app` 讀 — 三者均靠 `:core:domain/auth/AuthRepository` 介面溝通

## Don't use it for

- Feature 自有的 repository 介面（如 `HomeRepository`）— 放 feature 自己 `data/` 包
- Domain data class 模型（如 `App` / `TestRequest`）— 短期放 feature 自己；若兩個以上 feature 共享才移到這
- 任何 Android-specific 工具
- DI Module — Hilt @Module 屬於消費端

## Key concepts

- **`UseCase<P, R>`** — 抽象 base，`invoke(params): AppResult<R>` 自動套 `withContext(io)` + `CancellationException` 重 throw + 其他 throwable → `AppError`。
- **`NoParamUseCase<R>`** — `UseCase<Unit, R>` 的便利 subclass。
- **`VoidUseCaseResult`** — `typealias AppResult<Unit>`，給副作用型 use case 用。
- **`Repository`** — marker interface。Repository methods 一律回 `AppResult`、不丟 exception；suspend 或 Flow，無 callback。每個 aggregate root 一個 Repository。
- **`auth/AuthRepository`** — V1 唯一在這的「跨 feature」repository。詳見 [`API.md`](API.md) §AuthRepository。

## Quick example

```kotlin
// In :feature:home/domain/
class GetMatchedAppsUseCase @Inject constructor(
    private val repo: HomeRepository,           // feature-local interface, lives in feature/home
    dispatchers: DispatcherProvider,
) : NoParamUseCase<List<MatchedApp>>(dispatchers) {

    override suspend fun execute(params: Unit): AppResult<List<MatchedApp>> =
        repo.getMatchedFeed()
}

// ViewModel
class HomeViewModel @Inject constructor(
    private val getMatched: GetMatchedAppsUseCase,
) : ViewModel() {
    init {
        viewModelScope.launch {
            getMatched()
                .onSuccess { _state.value = HomeUiState.Ready(it) }
                .onFailure { _state.value = HomeUiState.Error(it) }
        }
    }
}
```

## Related

- spec_ref: [`_specs/modularization.md`](../../_specs/modularization.md) §3 — Repository / UseCase 規約
- spec_ref: [`_specs/feature_modules.md`](../../_specs/feature_modules.md) — per-feature contract 慣例
- depends on: `:core:common`
- dependents: 每個 `:feature:*`（吃 `UseCase` base），`:feature:auth/data` + `:feature:onboarding` + `:app`（吃 `AuthRepository`）
- 完整依賴 / 替換 / 測試見 [`DEPENDENCY.md`](DEPENDENCY.md)
