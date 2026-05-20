# AppTest — Compose Component Catalog (Organisms + Templates + Pages)

> **Version:** 0.1 · **Last updated:** 2026-05-19 · **Owner:** TBD
> Atoms / Molecules / rules 見 `compose_components.md`。本檔放有 layout + state 的中大型元件。

---

## 1. Organisms (in `:core:ui/components`)

### `AppTopBar`
```kotlin
AppTopBar(
  title: String,
  navIcon: @Composable (() -> Unit)? = null,
  actions: @Composable RowScope.() -> Unit = {},
  scrollBehavior: TopAppBarScrollBehavior? = null,
  expressiveStyle: AppTopBarStyle = LargeCollapsible,  // Small | CenterAligned | LargeCollapsible
)
```
Wraps `LargeTopAppBar` etc.；scroll behavior 預設 enterAlwaysScrollBehavior。

### `AppBottomBar` (M3 NavigationBar)
```kotlin
AppBottomBar(
  destinations: List<AppBottomDest>,
  current: AppBottomDest,
  onSelect: (AppBottomDest) -> Unit,
)
```
- 含 Glass Surface backdrop（fallback opaque per `design_system.md §7`）
- 自動隱藏在 keyboard 出現時

### `AppFAB`
```kotlin
AppFAB(text: String? = null, icon: ImageVector, onClick: () -> Unit, expanded: Boolean = true)
```
Expanded/collapsed transition 用 motionSchemeExpressive。

### `AppSearchBar`
```kotlin
AppSearchBar(
  query: String,
  onQueryChange: (String) -> Unit,
  onSubmit: () -> Unit,
  placeholder: String,
  trailingActions: @Composable RowScope.() -> Unit = {},
)
```

### `AppLoadingState` / `AppErrorState` / `AppEmptyState`
```kotlin
AppLoadingState(modifier: Modifier = Modifier, message: String? = null)
AppErrorState(error: AppError, onRetry: (() -> Unit)? = null, modifier: Modifier = Modifier)
AppEmptyState(
  illustration: ImageVector,
  title: String,
  description: String? = null,
  ctaText: String? = null,
  onCta: (() -> Unit)? = null,
)
```
三態 component **必須**搭配 every list/feed/screen。沒有 = code review reject。

### `AppToast` / `AppSnackbar`
M3 Snackbar wrapper with semantic variants (success/info/warn/error)。

### `AppFloatingPanel`
```kotlin
AppFloatingPanel(
  anchor: LayoutCoordinates?,            // 取自 onGloballyPositioned
  visible: Boolean,
  onDismiss: () -> Unit,
  content: @Composable () -> Unit,
)
```
Glass surface + spring-in motion。實作 `design_system.md §8` 規範。

## 2. Templates (in `:core:ui/templates`)

### `ScreenScaffold`
```kotlin
ScreenScaffold(
  topBar: @Composable () -> Unit = {},
  bottomBar: @Composable () -> Unit = {},
  fab: @Composable () -> Unit = {},
  snackbarHost: SnackbarHostState? = null,
  content: @Composable (PaddingValues) -> Unit,
)
```
- Edge-to-edge built-in (WindowInsets.systemBars)
- Auto handles ime padding
- 套 `AppTheme.colorScheme.surface` 背景

### `AdaptiveTwoPane`
```kotlin
AdaptiveTwoPane(
  list: @Composable () -> Unit,
  detail: @Composable () -> Unit,
  windowSizeClass: WindowSizeClass,
)
```
- Compact: 只顯示其一（navigation 控制）
- Medium / Expanded: 並排 + 分隔

### `CelebrationOverlay`
```kotlin
CelebrationOverlay(visible: Boolean, content: @Composable () -> Unit, onDismiss: () -> Unit)
```
全螢幕慶祝畫面（tier 升級 / 達 12 testers）；用 Spatial Layout 3 層 + 粒子 + haptic（per `design_system.md §9`）。

## 3. Per-feature pages (in `:feature:*/ui/`)

每個 feature 自己擁有 page-level Composables：
- `:feature:home` → `HomeScreen` / `HomeRoute` / `HomeViewModel` / `HomeUiState`
- `:feature:appdetail` → 同模式
- ...

每個 feature 必須提供：
- 1 `<Feature>Screen` (Stateless Composable，接 state + callbacks)
- 1 `<Feature>Route` (Stateful，注入 VM)
- 1 `<Feature>ViewModel` (Hilt @HiltViewModel)
- 1 `<Feature>UiState` (immutable sealed/data class)
- 子元件依需求拆檔（≤ 200 行/檔）

Page 不可被其他 feature import；跨 feature 跳轉走 `:core:navigation`。

## 4. State coverage matrix (organisms 額外 hard rule)

每個 organism 必須涵蓋以下 5 種 state 並有 `@Preview`：

| State | 範例 | 必須驗證 |
|---|---|---|
| **default** | 一般 data | 渲染、layout 不破 |
| **loading** | data 還沒到 | placeholder / skeleton 顯示 |
| **error** | network 失敗 | retry button 可按 |
| **empty** | data 為空 | empty state CTA 可按 |
| **dense / sparse** | 多 vs 少 data | scroll / overflow 正確 |

只有 1-2 個 preview 的 organism 進 code review 會被退。

## 5. Composition strategy

- **Prefer slot APIs over boolean flags.** 給 `actions: @Composable RowScope.() -> Unit` 而非 `showAction1: Boolean`。
- **Hoist state outward.** Organism 自己**不**持 `remember { mutableStateOf(...) }` 業務狀態；UI-only state（如 expanded/collapsed）才允許內留。
- **No side effects in composition.** `LaunchedEffect` / `DisposableEffect` 只能對 lifecycle / key 變化反應，不主動觸發業務動作。

## 6. Motion contract (organism level)

| Trigger | Motion |
|---|---|
| Screen enter | `fadeIn(120ms) + slideInVertically(200ms)` |
| Tab switch | `crossfade(200ms)` |
| State change in same screen | spring `motionSchemeStandard` |
| Celebration / tier upgrade | spring `motionSchemeExpressive` + 粒子 |
| List item appear | `animateItemPlacement()` |
| FAB expand/collapse | `motionSchemeExpressive` |

**禁止** 用 `tween(duration = 任意數字)` 不對齊 token。

## 7. Open decisions

| ID | Decision | Status |
|---|---|---|
| APT-P-023 | `AdaptiveTwoPane` list:detail 比例 | default 1:1.5 |
| APT-P-024 | `CelebrationOverlay` 粒子是否可關閉 | default: Reduce Motion 偏好開啟時關閉 |
| APT-P-025 | `AppFloatingPanel` 是否在 Compact 螢幕 fallback 到 bottom sheet | default: 是 |
