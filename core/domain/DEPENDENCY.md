# :core:domain — Dependencies

> Module DAG view + 替換策略 + 測試策略。

## I depend on

| Dep | Why |
|---|---|
| `:core:common` (api) | `AppResult` / `AppError` / `DispatcherProvider` / `AuthState`（UseCase base + AuthRepository 介面用到） |
| `org.jetbrains.kotlinx:kotlinx-coroutines-core` (api) | `withContext` / `StateFlow` |

**No** Android dep。**No** Compose dep。**No** DI dep（Hilt @Inject 在 concrete UseCase / Repository impl 端）。**No** Retrofit / Room / Supabase。

```kotlin
// core/domain/build.gradle.kts
plugins { alias(libs.plugins.kotlin.jvm) }
dependencies {
    api(project(":core:common"))
    api(libs.kotlinx.coroutines.core)
}
```

`kotlin.jvm` = 跑 JVM unit test 飛快，沒 Android resource 拖累。

## Modules depending on me

| Module | Uses what |
|---|---|
| 每個 `:feature:*` | `UseCase<P,R>` / `NoParamUseCase<R>` / `Repository` marker |
| `:feature:auth/data` | `AuthRepository` 介面（impl 處） |
| `:feature:onboarding` | `AuthRepository.markOnboardingComplete` |
| `:feature:profile` | (未來) `AuthRepository.signOut` |
| `:app` | `AuthRepository.state` (observe for startDestination) |
| `:core:data` | (未來 shared aggregate Repository impl) `Repository` marker |

`:core:network` / `:core:database` / `:core:ui` / `:core:designsystem` / `:core:navigation` 均 **不** 依賴本模組（純展示層或基礎設施層，無 domain 概念）。

## How to replace

### 加新 cross-feature Repository（候選：未來 `ProfileRepository` 升級）

1. 確認 ≥ 2 個 feature 真的引用（未達門檻先留 feature local）
2. 在 `:core:domain/<topic>/` 加介面檔（如 `profile/ProfileRepository.kt`），所有方法回 `AppResult`
3. 原 feature 把 impl 改成實作此介面
4. Hilt @Binds 在 feature 自己的 di Module，binding to `:core:domain` 介面
5. Consumer feature 透過 Hilt 注 `:core:domain` 介面
6. **不搬 impl**（除非要共用 DB / network adapter — 那就搬到 `:core:data`）

### 加新 UseCase（feature 內）

1. 新 file in feature `domain/`，extend `UseCase<P, R>` 或 `NoParamUseCase<R>`
2. constructor 注 repository + DispatcherProvider
3. `execute(params)` 一句話呼叫 repository，**不** 加業務 if/else 在 use case 裡（薄）
4. ≤ 50 行；超過代表責任太大，拆兩個

### 替換 `UseCase` base（如改回 直接放 ViewModel 內）

不建議。`UseCase` 提供：
- 統一 dispatcher 切換
- 統一 CancellationException 處理
- 統一 throwable → AppError 包裝
- 一致的測試切面（單獨測 use case 不用啟動 VM）

拿掉的話這些行為要在每個 VM 重做。

## How to test

| Test type | Tool | Scope |
|---|---|---|
| **UseCase base** | JUnit + Truth + coroutines-test | `invoke` 包 dispatcher / 處理 throwable / 重 throw cancellation |
| **Concrete UseCase** | JUnit + mockk | 注 fake repository，驗證呼叫順序與回傳 mapping |
| **Repository impl** | feature-local test | 不在本模組測（介面無 impl） |

範例：
```kotlin
class JoinTestUseCaseTest {
    private val dispatchers = TestDispatcherProvider()
    private val repo = mockk<TestRequestRepository>()
    private val useCase = JoinTestUseCase(repo, dispatchers)

    @Test fun `success path returns request`() = runTest {
        coEvery { repo.join("a", "t") } returns AppResult.Success(testRequest)
        val r = useCase(JoinTestUseCase.Params("a", "t"))
        assertThat(r).isInstanceOf(AppResult.Success::class.java)
    }
}
```

## File budget

| File | Lines | Notes |
|---|---:|---|
| `UseCase.kt` | ~47 | base + NoParam + typealias |
| `Repository.kt` | ~11 | marker only |
| `auth/AuthRepository.kt` | ~40 | interface + state machine KDoc |

每檔 ≤ 200 行 ✓。模組刻意 lean — contract layer 不該變胖。

## Deferred / not in scope

- **Flow-based UseCase variant** — 目前無需 `StreamingUseCase<P, Flow<R>>` 抽象；feature 直接讓 repository 暴露 Flow，VM `viewModelScope.launch { flow.collect { ... } }` 即可。真有 ≥ 3 處重複 boilerplate 才包。
- **Domain models 集中** — 目前 `App` / `TestRequest` / `Profile` 等 data class 散在各 feature；待跨 feature 共用真正發生才移到 `:core:domain/model/`。
- **Validation rules 抽象** — 各 feature 自己驗（`PlayOptInUrlValidator` 在 `:feature:myapps`），有共用需求再抽。
