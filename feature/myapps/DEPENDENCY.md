# :feature:myapps — Dependencies

## I depend on

### Internal

| Module | Why |
|---|---|
| `:core:common` | `AppResult`, `AppError`, `DispatcherProvider` (transitive) |
| `:core:designsystem` | tokens + atoms (`AppText`, `AppIcon`) |
| `:core:ui` | `ScreenScaffold`, `AppTopBar`, `AppFAB`, `AppListItem`, `AppProgressBar`, `AppEmptyState`, `AppErrorState`, `AppLoadingState`, `AppButton` |
| `:core:domain` | `UseCase` / `NoParamUseCase` bases |
| `:core:navigation` | `AppDestination.MyApps`, `AppDestination.AppEditor` |

### External

| Library | Why |
|---|---|
| `androidx.compose.bom` | UI + Material3 |
| `androidx.compose.material:material-icons-core` | `Icons.Filled.Add`, `Icons.Outlined.Apps`, `Icons.AutoMirrored.Filled.ArrowBack` (via :core:ui api) |
| `androidx.navigation:navigation-compose` | `composable<T>` + `toRoute<T>` (typed SavedStateHandle access) |
| `androidx.hilt:hilt-navigation-compose` | `hiltViewModel<T>()` |
| `androidx.lifecycle:lifecycle-runtime-compose` | `collectAsStateWithLifecycle` |
| `com.google.dagger:hilt-android` | DI |
| `ksp × hilt-compiler` | code gen |

## Modules depending on me

| Module | Uses what |
|---|---|
| `:app` | `myAppsGraph(...)` only |

## How to replace

### Fake → real backend
1. `class RealMyAppsRepository @Inject constructor(api, db) : MyAppsRepository`
2. `data/di/MyAppsDataModule.kt` 改 `@Binds bindMyAppsRepository(impl: RealMyAppsRepository)`
3. UI / VM / UseCase / Models 全不動
4. Note: Flow contract preserved — `observe()` 走 Supabase Realtime channel

### Add icon upload to Editor
1. 加 `iconUrl: String?` 到 `AppDraft`
2. Editor 加 `OutlinedButton("Upload icon")` → 跳 image picker (Activity Result API)
3. Upload via Firebase Storage → 拿 download URL → 更新 draft
4. `SaveAppUseCase.validate` 加 `iconUrl != null` check
5. Schema / API 已 spec → backend 已支援

### Add category dropdown
1. 加 `category: String` to AppDraft
2. Editor 加 `ExposedDropdownMenuBox` with `app_categories` lookup
3. Hard-code categories in V1 (sync with backend lookup later)

## How to test

| Test | Tool | Scope |
|---|---|---|
| **Unit (validator)** | JUnit + Truth | `PlayOptInUrlValidator.validate(...)` against 8 input cases (https/http/empty/wrong-host/play.google.com/...) |
| **Unit (UseCase)** | JUnit + Truth + Fake repo | `SaveAppUseCase` round-trip: invalid → AppError.Validation; valid → Success + repo state |
| **Unit (VM list)** | Turbine | `MyAppsViewModel.state` emits Loading → Loaded(seeded items); after `repo.save` emits new Loaded with extra item |
| **Unit (VM editor)** | Turbine | onField updates trigger urlValidation + canSave recompute; save flow emits savedId |
| **Compose UI test** | createComposeRule + Hilt-android-testing | empty state CTA tap → onCreate; list row tap → onEdit; FAB tap → onCreate |
| **Editor form a11y** | `onAllNodes(hasContentDescription...)` | every field has cd; error states surfaces via supportingText |

CI 跑 unit + Compose UI per `cicd.md §2`.

## File budget

| File | Lines | Notes |
|---|---:|---|
| `build.gradle.kts` | ~55 | feature-typical |
| `nav/MyAppsNavGraph.kt` | ~28 | 2 composable entries |
| `ui/list/MyAppsScreen.kt` | ~85 | list + empty + 三態 |
| `ui/list/MyAppsViewModel.kt` | ~32 | stateIn collector |
| `ui/list/MyAppsRoute.kt` | ~18 | stateful wrapper |
| `ui/list/MyAppsUiState.kt` | ~14 | sealed |
| `ui/editor/AppEditorScreen.kt` | ~155 | form 6 fields + actions |
| `ui/editor/AppEditorViewModel.kt` | ~78 | load + onField + save + recomputed |
| `ui/editor/AppEditorRoute.kt` | ~25 | LaunchedEffect on savedId |
| `ui/editor/AppEditorUiState.kt` | ~24 | single shape + flags |
| `domain/PlayOptInUrlValidator.kt` | ~30 | pure |
| `domain/usecase/GetMyAppsUseCase.kt` | ~16 | Flow delegation |
| `domain/usecase/SaveAppUseCase.kt` | ~42 | validate + repo.save |
| `domain/model/MyAppsModels.kt` | ~40 | OwnedAppRow / Status / AppDraft / PlayUrlValidation |
| `data/MyAppsRepository.kt` | ~28 | interface 6 methods |
| `data/FakeMyAppsRepository.kt` | ~85 | seed + observe + CRUD ops |
| `data/di/MyAppsDataModule.kt` | ~18 | Hilt @Binds |

每檔 ≤ 200 行 ✓ (hard rule)。AppEditorScreen 最高 ~155，預留 buffer for icon/category 後續加。

## Deferred (next polish)

- Icon upload field (Editor)
- Category dropdown (Editor)
- App stats view (post-launch metrics per App)
- Pause/Resume actions exposed on list rows（method 已備 in repo）
- Delete confirmation dialog（method 已備）
- Pull-to-refresh on list
- Per-screen `@Preview` × 4 states（待 Paparazzi CI）
