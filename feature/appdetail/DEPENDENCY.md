# :feature:appdetail — Dependencies

## I depend on

### Internal

| Module | Why |
|---|---|
| `:core:common` | `AppResult`, `AppError`, `DispatcherProvider`, `ReputationTier` (owner tier) |
| `:core:designsystem` | tokens + `AppText`, `AppIcon`, `AppVSpacer` atoms |
| `:core:ui` | `ScreenScaffold`, `AppTopBar`, `AppCard`, `AppButton`, `AppProgressBar`, `AppTierBadge`, three-state organisms |
| `:core:domain` | `UseCase` base |
| `:core:navigation` | `AppDestination.AppDetail`, `AppDeepLink.PATTERN_APP_DETAIL_*` |

### External

| Library | Why |
|---|---|
| `androidx.compose.bom` | Compose UI + Material3 (incl. `IconButton`, `Surface`) |
| `androidx.compose.material:material-icons-core` | `Icons.AutoMirrored.Filled.ArrowBack` (via :core:ui api) |
| `androidx.navigation:navigation-compose` | `composable<T>`, `navDeepLink`, `toRoute<T>` |
| `androidx.hilt:hilt-navigation-compose` | `hiltViewModel<T>()` |
| `androidx.lifecycle:lifecycle-runtime-compose` | `collectAsStateWithLifecycle` |
| `androidx.core:core-ktx` | `String.toUri()` extension (`toUri()` from `androidx.core.net`) |
| `com.google.dagger:hilt-android` | DI |
| `ksp × hilt-compiler` | code gen |

## Modules depending on me

| Module | Uses what |
|---|---|
| `:app` | `appDetailGraph(onNavigateUp)` only |

## How to replace

### Fake → real backend
1. `class RealAppDetailRepository @Inject constructor(api, db) : AppDetailRepository`
2. `data/di/AppDetailDataModule.kt` 改 `@Binds` 指向新 impl
3. UI / VM / UseCase / Models / nav 全不動
4. Deep links remain bound at `appDetailGraph` — no caller change

### Replace placeholder screenshots with carousel
1. 加 Coil `AsyncImage` 在新 `ScreenshotsCarousel` Composable（新檔 `ui/components/ScreenshotsCarousel.kt`）
2. `AppDetailData.screenshotCount` 改成 `screenshots: List<String>`（URL list）
3. `AppDetailScreen.LoadedBody` 用新 carousel 取代 `ScreenshotsPlaceholder`
4. `FakeAppDetailRepository` 給 5 個 placeholder URL

### Replace Join CTA "open intent" with real flow
- (APT-V1-R-040) `onJoinClicked` 改成 → call new `JoinTestUseCase` → 寫 TestRequest → schedule heartbeat WorkManager → 然後 open intent
- 增加 `JoinResult` event channel (success / error)
- UI 加 success snackbar

## How to test

| Test | Tool | Scope |
|---|---|---|
| **Unit (UseCase)** | JUnit + Truth + Fake | `GetAppDetailUseCase` success / NotFound |
| **Unit (VM init)** | Turbine | state emits Loading → Loaded(data); empty appId path → Error |
| **Unit (VM join)** | Turbine | onJoinClicked emits joinInProgress=true→false; openPlayStoreEvents emits URL once |
| **Unit (Fake repo)** | JUnit | mockFor returns stable data; empty id → NotFound |
| **Compose UI test** | createComposeRule | Loaded state renders header + requirements + explainability + Join button enabled |
| **Deep-link binding** | NavHost integration test in :app | inject Intent VIEW apptest://app/xyz → AppDetail composable enters with appId=xyz |

CI: unit + Compose UI per `cicd.md §2`.

## File budget

| File | Lines | Notes |
|---|---:|---|
| `build.gradle.kts` | ~55 | feature-typical |
| `nav/AppDetailNavGraph.kt` | ~25 | 1 composable + default deep links |
| `ui/AppDetailRoute.kt` | ~36 | stateful + Channel collector + Intent |
| `ui/AppDetailScreen.kt` | ~135 | three-state + Loaded body + JoinFooter + screenshots placeholder |
| `ui/AppDetailViewModel.kt` | ~60 | load + onJoinClicked + Channel event |
| `ui/AppDetailUiState.kt` | ~15 | sealed 3 case |
| `ui/components/AppDetailHeader.kt` | ~70 | icon placeholder + name + owner tier |
| `ui/components/RequirementsSection.kt` | ~45 | requirements card + progress bar |
| `ui/components/ExplainabilityCard.kt` | ~38 | top-3 reasons card |
| `domain/usecase/GetAppDetailUseCase.kt` | ~22 | thin |
| `domain/model/AppDetailModels.kt` | ~38 | 4 data classes |
| `data/AppDetailRepository.kt` | ~15 | interface |
| `data/FakeAppDetailRepository.kt` | ~52 | mock factory |
| `data/di/AppDetailDataModule.kt` | ~18 | Hilt @Binds |

每檔 ≤ 200 行 ✓.

## Deferred

- Screenshots carousel (Coil AsyncImage + HorizontalPager)
- Share action in top-bar (`Icons.Filled.Share` + Intent.ACTION_SEND)
- Real Join flow → JoinTestUseCase + heartbeat scheduling (APT-V1-R-040 / R-041)
- "Maybe later" CTA wiring (currently just text — should `onNavigateUp`)
- Read-more expand for description (currently auto-shows full text since not truncated)
