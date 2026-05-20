# :core:ui

> Atomic Design 中 **molecules + organisms + templates** 層。Atoms 在 `:core:designsystem`，pages 在 `:feature:*`。
> 任何 list / feed / screen 缺三態元件 (Loading/Error/Empty) = code review reject。

## Use it when

- 任何 `:feature:*` 需要組合 UI（Button / Card / TopBar / Scaffold / EmptyState / ...）
- 需要 tier-aware visualization (`AppTierBadge` 是專案唯一合法的 tier 顏色出口)
- 需要 responsive list/detail layout（`AdaptiveTwoPane`）
- 需要 semantic Snackbar（Success / Warning / Error / Info）

## Don't use it for

- Tokens (color/typography/shape/spacing) — 在 `:core:designsystem`
- Pages / full screens — 在 `:feature:*`
- 業務邏輯 / data fetch — feature 內部 + UseCase
- ViewModel 注入 — page-level only，這層不持 state
- Glass Surface / Floating Panel — 等 `AppGlass` token shipped (deferred companion task)

## Key concepts

- **Molecules** (`components/App<...>.kt`): 多 atoms 組合單功能。如 `AppButton`、`AppCard`、`AppListItem`、`AppChip`、`AppFilterChip`、`AppBadge`、`AppTierBadge`、`AppProgressBar`、`AppRating`.
- **Organisms** (`components/`): 含 layout + state。`AppTopBar`、`AppBottomBar`、`AppFAB`、`AppSearchBar`、`AppLoadingState` / `AppErrorState` / `AppEmptyState`、`AppSnackbar`.
- **Templates** (`templates/`): screen-level. `ScreenScaffold`（edge-to-edge）、`AdaptiveTwoPane`（responsive）.
- **三態必備**: `AppLoadingState`、`AppErrorState(error: AppError)`、`AppEmptyState(...)` — 每個 list 螢幕都要套。

## Quick example

```kotlin
@Composable
fun HomeScreen(state: HomeUiState, onAppClick: (String) -> Unit) {
    val snackbar = remember { SnackbarHostState() }
    ScreenScaffold(
        topBar = { AppTopBar(title = "AppTest") },
        bottomBar = { AppBottomBar(destinations = bottomDests, current = current, onSelect = ::onTab) },
        snackbarHost = snackbar,
    ) { padding ->
        when (state) {
            is HomeUiState.Loading -> AppLoadingState()
            is HomeUiState.Error   -> AppErrorState(state.error, onRetry = ::retry)
            is HomeUiState.Empty   -> AppEmptyState(
                illustration = Icons.Outlined.Inbox,
                title = "No matches yet",
                description = "Next batch in 4h 23m",
            )
            is HomeUiState.Loaded  -> /* LazyColumn with AppCard items */ ...
        }
    }
}
```

## Related

- spec_ref: [`_specs/compose_components.md`](../../_specs/compose_components.md) — atoms + molecules + rules
- spec_ref: [`_specs/compose_organisms_templates.md`](../../_specs/compose_organisms_templates.md) — organisms + templates
- depends on: `:core:designsystem` (tokens + atoms), `:core:common` (AppError, ReputationTier)
- dependents: 8 V1 features + `:app`
- 完整依賴與替換策略見 [`DEPENDENCY.md`](DEPENDENCY.md)
