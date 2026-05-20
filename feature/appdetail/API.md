# :feature:appdetail — Public API

> 對外可 import。Internal 不列。

## Nav extension (the only public surface)

### `fun NavGraphBuilder.appDetailGraph(onNavigateUp, deepLinks)`
```kotlin
appDetailGraph(
    onNavigateUp: () -> Unit,
    deepLinks: List<NavDeepLink> = listOf(
        navDeepLink { uriPattern = AppDeepLink.PATTERN_APP_DETAIL_CUSTOM },
        navDeepLink { uriPattern = AppDeepLink.PATTERN_APP_DETAIL_WEB },
    ),
)
```
- Mounts `composable<AppDestination.AppDetail>` with default deep-link bindings
- `onNavigateUp` 由 caller 提供（通常 `nav.popBackStack()`）
- `deepLinks` 預設兩個 pattern，caller 想 override 可傳新 list（罕見）
- Source: [`nav/AppDetailNavGraph.kt`](src/main/kotlin/com/apptest/feature/appdetail/nav/AppDetailNavGraph.kt)

## NOT public (internal)

| Type | Path |
|---|---|
| `AppDetailRoute` / `AppDetailScreen` / `AppDetailViewModel` / `AppDetailUiState` | ui/ |
| `AppDetailHeader` / `RequirementsSection` / `ExplainabilityCard` | ui/components/ |
| `AppDetailRepository` (interface) / `FakeAppDetailRepository` / `AppDetailDataModule` | data/ |
| `GetAppDetailUseCase` | domain/usecase/ |
| `AppDetailData` / `OwnerInfo` / `Requirements` / `MatchReason` | domain/model/ |

If `AppDetailData` ends up shared by ≥ 2 features (Home preview cards 想直接取 detail? 不會 — Home 自己 model 已足) → 不太可能升 `:core:domain`。

## Hilt bindings contributed

| Type | Bound to | Scope |
|---|---|---|
| `AppDetailRepository` | `FakeAppDetailRepository` (V1) | `@Singleton` |
| `AppDetailViewModel` | self (`@HiltViewModel`) | per-NavGraph; SavedStateHandle.toRoute 讀 appId |
| `GetAppDetailUseCase` | self (`@Inject`) | per-VM |
| `FakeAppDetailRepository` | self (`@Inject` + `@Singleton`) | singleton |

`DispatcherProvider`（`UseCase` 基類用）由 `:app/di/CoreModule` 提供。

## Events

ViewModel exposes a `Flow<String>` (`openPlayStoreEvents`) for the Play opt-in URL.
Route observes via `LaunchedEffect` and dispatches `Intent.ACTION_VIEW`. This pattern keeps
the side effect out of Composition + ViewModel (no Android Context in VM).

## V2 / V3 forecast public surface

- (V2) `appDetailGraph(onNavigateUp, onTestStarted)` — 真實 TestRequest creation 完成後 callback 讓 caller refresh Home
- (V2) Expose `AppDetailScreenshotLoader` interface — V2 加 Coil-based carousel 時 ext point
