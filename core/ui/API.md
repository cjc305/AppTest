# :core:ui — Public API

> 對外可 import。Internal 不列。

## Molecules (in `components/`)

### `AppButton(text, onClick, modifier, variant, leadingIcon, loading, enabled)`
```kotlin
enum class AppButtonVariant { Primary, Secondary, Tonal, Text, Destructive }
fun AppButton(text, onClick, modifier=Modifier, variant=Primary, leadingIcon=null, loading=false, enabled=true)
```
Wraps M3 Button family. `loading=true` swaps content for spinner + disables click.

### `AppCard(modifier, variant, onClick, content)`
```kotlin
enum class AppCardVariant { Elevated, Outlined, Filled }
fun AppCard(modifier=Modifier, variant=Elevated, onClick=null, content: ColumnScope.() -> Unit)
```
M3 Elevated/Outlined/Card. Non-null `onClick` enables ripple + a11y.

### `AppListItem(headline, modifier, supporting, leading, trailing, onClick)`
M3 ListItem wrapper with slot APIs and AppTypography pinning.

### `AppChip(text, onClick, modifier, leadingIcon)` / `AppFilterChip(text, selected, onSelectedChange, modifier, leadingIcon)`
M3 AssistChip / FilterChip wrappers.

### `AppBadge(modifier, text, count)`
M3 Badge. Pass `text` for label OR `count` for numeric (auto "99+"). Neither = dot.

### `AppTierBadge(tier, modifier, showLabel, size)`
```kotlin
enum class AppTierBadgeSize { Small, Medium, Large }
fun AppTierBadge(tier: ReputationTier, modifier=Modifier, showLabel=true, size=Medium)
```
**唯一**合法的 reputation tier 視覺化出口。Owns tier→color mapping via `AppExtended.colors`.

### `AppProgressBar(progress, modifier, label, height)`
M3 LinearProgressIndicator + optional label. `progress` clamped to [0f, 1f].

### `AppRating(value, modifier, max, onChange)`
Star rating. readOnly when `onChange=null`. V1 internal only (V2 對外曝光 per APT-P-018).

## Organisms (in `components/`)

### `AppTopBar(title, modifier, navIcon, actions, scrollBehavior, style)`
```kotlin
enum class AppTopBarStyle { Small, CenterAligned, LargeCollapsible }
```
Default = LargeCollapsible per design_system §3. Plumb `scrollBehavior` from Scaffold for collapse.

### `AppBottomBar(destinations, current, onSelect, modifier)`
```kotlin
data class AppBottomDest(val id: String, val label: String, val icon: ImageVector)
```
M3 NavigationBar. Glass Surface backdrop deferred (TODO APT-X-006).

### `AppFAB(icon, onClick, modifier, text, expanded)`
ExtendedFAB when `text != null`; FloatingActionButton otherwise. `expanded` only effective with text.

### `AppSearchBar(query, onQueryChange, onSubmit, placeholder, modifier, trailingActions)`
OutlinedTextField wrapper with IME=Search. Migrate to M3 SearchBar if expandable history needed.

### `AppLoadingState(modifier, message)` / `AppErrorState(error: AppError, modifier, onRetry)` / `AppEmptyState(illustration, title, modifier, description, ctaText, onCta)`
**三態必備**. AppErrorState consumes `AppError` from `:core:common` and renders user-facing copy.

### `AppSnackbar(message, modifier, severity, actionLabel, onAction)`
```kotlin
enum class AppSnackbarSeverity { Success, Info, Warning, Error }
```
Semantic colors via `AppExtended.colors` + M3 error/inverse fallbacks. Toast = `Info + no action + short`.

## Templates (in `templates/`)

### `ScreenScaffold(modifier, topBar, bottomBar, fab, snackbarHost, content)`
Edge-to-edge wrapper (`contentWindowInsets = WindowInsets.systemBars`) over M3 Scaffold.

### `AdaptiveTwoPane(list, detail, windowSizeClass, modifier, currentPane)`
```kotlin
enum class AdaptiveTwoPanePane { List, Detail }
```
Compact: shows one pane only (caller manages nav). Medium+: side-by-side 1 : 1.5.

## NOT public (internal)

- `colorsFor(severity)` in AppSnackbar — internal helper
- `errorTitle(error)` in AppStateViews — internal helper
- `ButtonBody(...)` in AppButton — internal layout helper
- `TierDot(...)` in AppTierBadge — internal Composable helper
- `SnackbarColorSet` — internal data class
