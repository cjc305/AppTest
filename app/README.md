# :app

> Single Activity Android entry point. Hilt application + Compose NavHost wiring all 8 core modules +
> 13 destinations (all real, no stubs remaining bar `Settings` which is an inline minimal screen).

## Use it when

- 加新 top-level `AppDestination` 種類
- Wire 新 `:feature:*` graph 進 `AppNavHost`
- 改 splash / app-wide theme XML / 系統 bar 行為
- 加 launch-time initializer（WorkManager / Crashlytics / Firebase — landing R-042 / X-004）
- 改 bottom-bar 4-tab 組成（編 `MainTopLevelDestination`）
- 改 sign-out / share-invite 等 Activity-level side-effect 路徑

## Don't use it for

- 任何業務邏輯 — 全進 `:feature:*`
- Reusable UI 元件 — 進 `:core:ui`
- Token / theme — 進 `:core:designsystem`
- Nav contract — 進 `:core:navigation`
- Model / API / DB — 各自 `:core:*` 或 feature data

**Hard rule (per `modularization.md §3 #5`):** `:app` 模組 ≤ 30 source files。目前 4 個（Application / MainActivity / AppNavHost / MainTopLevelDestination）。

## Key concepts

- **`AppTestApplication`** — `@HiltAndroidApp`，產生 SingletonComponent。目前 onCreate 空；FCM channel 註冊 / Crashlytics init 加在這（R-042 / X-004）。
- **`MainActivity`** — `@AndroidEntryPoint`；observe `AuthRepository.state` 驅動 NavHost startDestination；提供 `shareInvite(uri)` / `signOut()` callback 給 NavHost（Activity-only API）。
- **`AppNavHost`** — Material3 `Scaffold` 包整個 NavHost，`bottomBar` 在 4 個 top-level tab 上自動渲染 [AppBottomBar]。Outer scaffold `contentWindowInsets = WindowInsets(0)` 避免與 feature `ScreenScaffold` 重複 padding。
- **`MainTopLevelDestination`** — 4 entry enum (Home/MyApps/Testing/Profile)。內含 icon + label key + 對應 `AppDestination` + `matches(NavDestination?)` 函式，配 `currentBackStackEntryAsState` 同步 selection。
- **`MainRoot` redirect** — 命中 `AppDestination.MainRoot` 時 `LaunchedEffect` 立即 navigate `Home` + popUpTo MainRoot inclusive，讓 Home 成 back-stack root（按返回鍵直接退出 app）。
- **Tab navigation pattern** — `popUpTo(graph.findStartDestination()) { saveState = true } + launchSingleTop + restoreState`（NowInAndroid pattern）。
- **Settings** — inline composable in [AppNavHost.kt:SettingsStub]，無獨立 feature module；locale toggle 屬於 V2，sign-out 已接 `MainActivity.signOut()`。

## Quick example (wire a new feature)

```kotlin
// 1. feature 端先 export NavGraphBuilder.xxxGraph(...)
// 2. settings.gradle.kts include ":feature:xxx"
// 3. :app/build.gradle.kts dependencies block add `implementation(project(":feature:xxx"))`
// 4. AppNavHost.kt — add inside NavHost { ... }:
//    xxxGraph(onBackClick = navController::popBackStack, onYourAction = { ... })
// 5. 若 xxx 是 top-level tab，再加 entry 到 MainTopLevelDestination + icon
```

## Related

- spec_ref: [`_specs/navigation.md`](../_specs/navigation.md) — NavHost / subgraph / deep link
- spec_ref: [`_specs/feature_modules.md`](../_specs/feature_modules.md) — each feature's `NavGraphBuilder` contract
- depends on: 所有 8 個 `:core:*` 模組 + 全部 8 個 feature 模組
- dependents: 無（leaf application module）
- 完整 [`DEPENDENCY.md`](DEPENDENCY.md)
