# :core:navigation — Dependencies

> Module DAG view + 替換策略 + 測試策略。

## I depend on

| Dep | Why |
|---|---|
| `:core:common` | (currently unused; reserved for `AppError` if nav parse failures need typing) |
| `androidx.navigation:navigation-compose:2.8.5` | type-safe destinations (`composable<T>`, `navigation<T>`, `toRoute<T>`), deep-link binding (`navDeepLink`) |
| `androidx.hilt:hilt-navigation-compose:1.2.0` | `hiltViewModel<T>()` for feature Routes (re-exported for downstream features) |
| `org.jetbrains.kotlinx:kotlinx-serialization-json` | `@Serializable` codecs for type-safe nav args |
| `androidx.compose.bom` | `Composable` annotation 來自 Compose UI（透過 navigation-compose 帶入） |

**No** dependency on `:core:domain`, `:core:data`, `:core:network`, `:core:database`, `:core:ui`, `:core:designsystem`, any `:feature:*`.

完整 Gradle 設定見 [`build.gradle.kts`](build.gradle.kts)。

## Modules depending on me

| Module | Uses what |
|---|---|
| `:app` | `AppDestination` (NavHost startDestination + composable types) / `AuthState` / `startDestinationFor` / `AppDeepLink.PATTERN_*` |
| `:feature:auth` | `AppDestination.AuthRoot` / `SignIn` / `EmailVerify` (provides graph + emits AuthState) |
| `:feature:onboarding` | `AppDestination.OnboardingRoot` (provides graph) |
| `:feature:home` | `AppDestination.Home`, navigates to `AppDetail` |
| `:feature:myapps` | `AppDestination.MyApps` / `AppEditor` |
| `:feature:appdetail` | `AppDestination.AppDetail` (consumes appId arg) |
| `:feature:testing` | `AppDestination.Testing` |
| `:feature:profile` | `AppDestination.Profile` / `Settings` / `Inbox` |
| `:feature:inbox` | `AppDestination.Inbox` |

任何 feature 跳轉到 *其他 feature* 一律經過 `AppDestination`，不可 import 對方 nav class（hard rule per `modularization.md §3`）。

## How to replace

### 加新 destination (e.g., V2 Rating screen)
1. 加 `@Serializable data class Rating(val testId: String) : AppDestination` in `AppDestination.kt`
2. 若有 deep link 加對應 `PATTERN_RATING` 在 `AppDeepLink.kt` + builder fn
3. Update `API.md` 表 + `README.md` 概述
4. 由 `:feature:rating` 在自己 `nav/` 包提供 `NavGraphBuilder.ratingGraph(...)`
5. `:app/MainActivity` NavHost 加 `composable<AppDestination.Rating> { ... }`

### 加新 AuthState (e.g., V3 「等待 email 驗證」)
1. 加 enum case 在 `AuthState`
2. 更新 `startDestinationFor(state)` 對應
3. Update FLOW.md state diagram
4. `:feature:auth` 負責何時 emit 新 state

### 替換整個 nav library (e.g., Voyager 替 Compose Nav)
1. Public API 保留 — 換內部 dep
2. `AppDestination` / `AuthState` 不動（純資料 + 純函式）
3. `:app/MainActivity` NavHost 設定改寫；feature graph fn signature 改
4. 風險高 — APT-A-017 預設不換

## How to test

| Test type | Tool | Scope |
|---|---|---|
| **Unit (pure)** | JUnit + Truth | `startDestinationFor` 對 3 cases 結果正確 |
| **Unit (parser)** | JUnit + Truth | `AppDeepLink.parse(uri)` 對 ≥ 8 種 URI（含 happy + null + malformed）|
| **Unit (builders)** | JUnit + Truth | `appDetail(id)` 等 builders 對 special chars (URL-encoded) 行為一致 |
| **Integration (NavHost)** | Compose UI test in `:app` | navigate(AppDestination.X) 後 `currentBackStackEntry?.destination?.route` 應命中對應 type |
| **Deep-link binding** | Compose UI test in `:app` | inject Intent with URI → NavHost auto-route |

Pure tests 跑於 `:core:navigation` 自己；NavHost 整合測在 `:app`（避免循環依賴）。

## File budget

| File | Lines | Notes |
|---|---:|---|
| `AppDestination.kt` | ~38 | 13 entries, sealed |
| `AuthState.kt` | ~28 | enum + startDestinationFor |
| `AppDeepLink.kt` | ~62 | constants + builders + parser |

每檔 ≤ 200 行 ✓ (hard rule)。Sum 128 lines；模組刻意 lean。

## Deferred / not in scope

- **Result-passing helpers** — Compose Nav 2.8+ 的 `savedStateHandle` 即足；待真有需求才包 wrapper
- **Navigation result events bus** — V2 才考慮，V1 用 Flow from shared repo（per `navigation.md §11`）
- **Per-destination motion tokens** — design_system.md §6 motion contract 已固定整體規範，無需 nav 級覆寫
