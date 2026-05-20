# :feature:home — Dependencies

> Module DAG view + 替換策略 + 測試策略 + file budget.

## I depend on

### Internal modules

| Module | Why |
|---|---|
| `:core:common` | `AppResult`, `AppError`, `DispatcherProvider`, `ReputationTier` |
| `:core:designsystem` | `AppTheme`（透過 :app 套）、`AppText` / `AppVSpacer` atoms、`AppSpacing` tokens |
| `:core:ui` | `ScreenScaffold`, `AppTopBar`, `AppCard`, `AppButton`, `AppListItem`, `AppProgressBar`, `AppTierBadge`, `AppLoadingState`, `AppErrorState`, `AppEmptyState` |
| `:core:domain` | `NoParamUseCase`, `Repository` marker |
| `:core:navigation` | `AppDestination.Home` (composable type) |

**No** dependency on `:core:data`, `:core:network`, `:core:database`, or any other `:feature:*`.

### External

| Library | Why |
|---|---|
| `androidx.compose.bom` | Compose UI + Material3 + Foundation |
| `androidx.navigation:navigation-compose` | `composable<T>` + `NavGraphBuilder` extension target |
| `androidx.hilt:hilt-navigation-compose` | `hiltViewModel<HomeViewModel>()` |
| `androidx.lifecycle:lifecycle-runtime-compose` | `collectAsStateWithLifecycle` |
| `com.google.dagger:hilt-android` | Hilt runtime |
| `ksp × hilt-compiler` | Hilt code gen |

完整 Gradle 設定見 [`build.gradle.kts`](build.gradle.kts)。

## Modules depending on me

| Module | Uses what |
|---|---|
| `:app` | `homeGraph(onAppClick)` 唯一 |

完全 leaf-ish — 沒有其他 feature import 本 module（modularization hard rule §3 #1 強制）。

## How to replace

### Replace Fake with real backend
1. 新 impl `class RealHomeRepository @Inject constructor(...) : HomeRepository`（在新 `:feature:home` 內或單獨 `:core:data` 視 scope）
2. 改 `data/di/HomeDataModule.kt`:
   ```kotlin
   @Binds abstract fun bindHomeRepository(impl: RealHomeRepository): HomeRepository
   ```
3. 留 `FakeHomeRepository` 作 test fixture（可選）
4. UI / VM / UseCase / Route / Models 全不動

### Replace UI shell (e.g. V2 加 pull-to-refresh)
1. 改 `ui/HomeScreen.kt` 加 `SwipeRefresh` wrap
2. `HomeRoute` 提供 `onRefresh` lambda 連到 `viewModel.load`
3. UI 改不出本 module

### Replace UseCase logic (e.g. 加 cache freshness)
1. `domain/usecase/GetHomeDataUseCase` 加 cache layer
2. 可能引入新 `HomeCachePolicy` interface
3. Repository / UI 不動

## How to test

| Test | Tool | Scope |
|---|---|---|
| **Unit (ViewModel)** | JUnit + Turbine + MockK | `HomeViewModel.load()` 走 Loading→Loaded（or Empty / Error）;  `init` 自動觸發 |
| **Unit (UseCase)** | JUnit + Truth + Fake repo | `GetHomeDataUseCase.invoke()` 對 success / failure 各路徑 |
| **Unit (Fake repo)** | JUnit + Truth | `FakeHomeRepository.getHomeData()` 永遠 Success + 內容 stable |
| **Compose UI test** | createComposeRule + Hilt-android-testing | `HomeScreen(state = Loaded(mockData))` 渲染 4 段 + 點 card 觸發 `onAppClick` |
| **State coverage** | parametrized | Loading / Error / Empty / Loaded × Light / Dark |
| **Screenshot test** | Paparazzi | Loaded × Light / Dark / Compact / Medium widths |

CI 跑前 5 類 per `cicd.md §2`。

## File budget

| File | Lines | Notes |
|---|---:|---|
| `build.gradle.kts` | ~60 | 5 core deps + Compose + Hilt + lifecycle-compose + test |
| `src/main/AndroidManifest.xml` | ~2 | empty manifest with namespace |
| `nav/HomeNavGraph.kt` | ~17 | 1 fn extension — public surface |
| `ui/HomeRoute.kt` | ~26 | stateful wrapper |
| `ui/HomeScreen.kt` | ~175 | stateless + 5 private sub-Composables |
| `ui/HomeViewModel.kt` | ~52 | @HiltViewModel + load + map state |
| `ui/HomeUiState.kt` | ~21 | sealed 4-case |
| `domain/usecase/GetHomeDataUseCase.kt` | ~22 | thin delegation |
| `domain/model/HomeModels.kt` | ~50 | 5 data classes |
| `data/HomeRepository.kt` | ~18 | interface |
| `data/FakeHomeRepository.kt` | ~55 | V1 mock |
| `data/di/HomeDataModule.kt` | ~22 | Hilt @Binds |

每檔 ≤ 200 行 ✓ (hard rule)。`HomeScreen.kt` 最高 ~175，仍預留 buffer；若加更多 sub-Composable 接近 200 → 拆 `components/` 子包。

## Deferred (next polish)

- 真正的 Skip CTA 行為（persist 一次性 dismissal、refresh）— TODO 已標在 `HomeRoute`
- Pull-to-refresh wrap（SwipeRefresh from accompanist OR M3 PullToRefreshContainer）
- Per-screen `@Preview` × 4 states（待 Paparazzi CI 落地統一補）
- AppDetail navigation result handling（測試完回 Home 看到 Active tests +1）— 等 `:feature:appdetail` 上線
- Bottom-bar host wrap（需 R-023~027 各 feature 都上線後在 `:app` 統一加 MainScaffold）
