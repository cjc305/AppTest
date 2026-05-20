# :core:designsystem — Public API

> 對外可 import。Internal 不列。

## Types

### `AppExtendedColors` (data class, @Immutable)
```kotlin
data class AppExtendedColors(
    val tierNewcomer, tierBronze, tierSilver, tierGold: Color,
    val tierPlatinumStart, tierPlatinumEnd: Color,    // gradient
    val success, onSuccess, successContainer, onSuccessContainer: Color,
    val warning, onWarning, warningContainer, onWarningContainer: Color,
)
```
- **Provided by:** `AppTheme { ... }` via `LocalAppExtendedColors` CompositionLocal
- **Access:** `AppExtended.colors.tierGold` (any @Composable, inside `AppTheme`)
- **Throws:** `IllegalStateException` if accessed outside `AppTheme` (catches buggy previews)
- **Threading:** read-only-composable; safe to read in composition phase

### `AppSpacing` (object)
```kotlin
object AppSpacing { val Xxs, Xs, Sm, Md, Lg, Xl, Xxl: Dp }
```
- Values: 2 / 4 / 8 / 16 / 24 / 32 / 48 dp
- **Convention:** screen padding default = `Md`; large screen → `Lg`

## Composables

### `AppTheme(darkTheme, dynamicColor, content)`
```kotlin
@Composable fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
)
```
- Wraps `MaterialTheme` with our color/type/shape tokens + extended colors CompositionLocal.
- Dynamic Color auto-applied on Android 12+ when `dynamicColor=true`; falls back to brand palette otherwise.
- Pure (no side effects). Idempotent — can nest safely (inner one overrides).

### `AppPreviewTheme(darkTheme, content)`
```kotlin
@Composable fun AppPreviewTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
)
```
- For `@Preview` only. Disables Dynamic Color (system-dependent → previews non-reproducible).
- Adds 16dp padding + surface background for visual consistency.

## Object accessors

### `AppExtended.colors`
```kotlin
object AppExtended {
    val colors: AppExtendedColors @Composable @ReadOnlyComposable get()
}
```
Sugar for `LocalAppExtendedColors.current`. Use everywhere over raw CompositionLocal.

## Annotations

### `@AppPreviewLightDark`
Meta-annotation = `@Preview(Light) + @Preview(Dark)`. Use on top of `@Composable` preview functions for instant light+dark dual rendering.

## Atoms (in `components/`)

最薄 M3 包裝層，固定 typography/icon/color defaults。所有 atoms 純 stateless，無 side effect。

### `AppText(text, modifier, style, color, maxLines, overflow, textAlign)`
M3 `Text` wrapper pinned to `MaterialTheme.typography.bodyLarge` + `LocalContentColor` defaults.

### `AppIcon(imageVector, contentDescription, modifier, tint, size = 24.dp)`
M3 `Icon` wrapper with mandatory cd (a11y) + standardized size (16/20/24/32/40 per design_system §10).

### `AppSpacer` family
```kotlin
fun AppVSpacer(height: Dp)    // vertical
fun AppHSpacer(width: Dp)     // horizontal
fun AppSquareSpacer(size: Dp) // both axes
```
Use with `AppSpacing.*` tokens, not raw dp literals.

### `AppDivider(modifier, thickness = 1.dp, color = outlineVariant)` / `AppVerticalDivider(...)`
M3 dividers with outline-variant default (subtler than `outline`).

### `AppPlaceholder(modifier, shape)`
Shimmer skeleton for loading. Caller provides dimensions via modifier. Animation: 1200ms linear infinite.

## NOT public (internal)

- `AppLightColors` / `AppDarkColors` — M3 schemes (only used by `AppTheme`)
- `AppExtendedLight` / `AppExtendedDark` — same
- `AppTypography` / `AppShapes` — same
- `LocalAppExtendedColors` — accessed via `AppExtended.colors` 包裝

These are `internal` 可見 — 外部模組需要 token 一律走 `MaterialTheme.*` 或 `AppExtended.colors.*`。
