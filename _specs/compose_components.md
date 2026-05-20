# AppTest — Compose Component Catalog (Atoms + Molecules + Rules)

> **Version:** 0.2 (split) · **Last updated:** 2026-05-19 · **Owner:** TBD
> Atomic Design 分類；Atoms / Molecules + 跨檔規則。
> Organisms / Templates / Pages 見 `compose_organisms_templates.md`。
> Tokens 來自 `design_system.md`。

---

## 1. Atomic taxonomy

```
Atoms      → 最小不可分（Text, Icon, Button）→ :core:designsystem
Molecules  → 多個 atom 組合（Card, ListItem, Chip）→ :core:ui
Organisms  → 含 layout + state（TopBar, FAB, EmptyState）→ :core:ui  [見 organisms 檔]
Templates  → screen-level scaffold（ScreenScaffold, AdaptiveTwoPane）→ :core:ui  [見 organisms 檔]
Pages      → feature 專屬完整 screen → :feature:*                       [見 organisms 檔 §6]
```

Hard rule: pages 永不寫進 `:core:ui`；atoms 永不寫進 `:feature:*`。

## 2. Atoms (in `:core:designsystem`)

| Component | Public API | Notes |
|---|---|---|
| `AppText` | `(text, style = MaterialTheme.typography.bodyLarge, color = LocalContentColor.current)` | Pin to AppTypography tokens |
| `AppIcon` | `(imageVector, contentDescription, tint = LocalContentColor.current, size = 24.dp)` | Standard icon |
| `AppSpacer` | `(size: Dp)` | Typed wrapper avoiding magic numbers |
| `AppDivider` | `(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)` | M3 outline-variant default |
| `AppPlaceholder` | `(modifier, shape = MaterialTheme.shapes.small)` | shimmer placeholder for loading |

## 3. Molecules (in `:core:ui/components`)

### `AppButton`
```kotlin
AppButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  variant: AppButtonVariant = Primary,   // Primary | Secondary | Tonal | Text | Destructive
  leadingIcon: ImageVector? = null,
  loading: Boolean = false,
  enabled: Boolean = true,
)
```
Wraps M3 `Button/FilledTonalButton/TextButton/OutlinedButton` per variant.

### `AppCard`
```kotlin
AppCard(
  modifier: Modifier = Modifier,
  variant: AppCardVariant = Elevated,    // Elevated | Outlined | Filled
  onClick: (() -> Unit)? = null,
  content: @Composable ColumnScope.() -> Unit,
)
```
- Animated state on click (ripple + scale 0.98 spring)
- Outlined variant uses `outlineVariant` border

### `AppListItem`
```kotlin
AppListItem(
  headline: String,
  supporting: String? = null,
  leading: @Composable (() -> Unit)? = null,
  trailing: @Composable (() -> Unit)? = null,
  onClick: (() -> Unit)? = null,
)
```
Wraps M3 `ListItem` with type-safety + slot APIs.

### `AppChip` / `AppFilterChip`
```kotlin
AppChip(text, onClick, selected = false, leadingIcon = null)
AppFilterChip(text, selected, onSelectedChange)
```
For category filters, tag display.

### `AppBadge`
```kotlin
AppBadge(text: String? = null, count: Int? = null)  // small dot or numeric badge
```

### `AppTierBadge` (project-specific)
```kotlin
AppTierBadge(
  tier: ReputationTier,                  // Newcomer..Platinum
  showLabel: Boolean = true,
  size: AppTierBadgeSize = Medium,
)
```
Owns colors per tier per `design_system.md §2.3`. Platinum 用 gradient + subtle shimmer。

### `AppProgressBar`
```kotlin
AppProgressBar(
  progress: Float,                       // 0f..1f
  label: String? = null,                 // e.g. "5 / 14 days"
  height: Dp = 8.dp,
)
```
M3 linear progress + label。

### `AppRating`
```kotlin
AppRating(value: Float, max: Int = 5, onChange: ((Float) -> Unit)? = null)
```
Stars; readOnly when onChange null. (V1 內部 dev profile 用，V2 才對外曝光)

## 4. Public API rules (hard)

每個 `:core:ui` / `:core:designsystem` 元件公開 API 必須：
- 用 named params + default values（不 expose internal types）
- 不接 `Modifier` 以外的 layout-coupled 參數
- 接 `() -> Unit` callbacks，不接 `Flow / StateFlow / suspend`（那是 ViewModel 責任）
- 必須有 `@Preview` 至少 3 個（default / loading-or-error / empty 視組件性質）
- 不接 `Context`；用 `LocalContext.current` 或不用 Context

## 5. Testing

- 每個元件 1 個 `@Preview` 套 `AppPreviewTheme`
- Atoms / molecules: Compose UI test (`createComposeRule`) → 驗 render + click
- Organisms 含 state: 同上 + state-table 測試（loading/success/error/empty 覆蓋）— 見 organisms 檔
- Screenshot tests: Paparazzi 或 Roborazzi，跑在 PR check (見 `cicd.md`)

## 6. Anti-patterns (code review reject)

1. 在 `:core:ui` 元件內注入 ViewModel → 應該 page-level 才注入
2. 元件接 `Context` 參數 → 用 `LocalContext.current` 或不要 Context
3. Hard-code `Color(0xFF...)` → 用 `MaterialTheme.colorScheme.*`
4. Hard-code `Dp` 不在 `8dp` 倍數 → 用 token
5. List 缺三態元件（loading/error/empty）→ reject
6. 超過 200 行的 Composable file → reject，必須拆

## 7. Open decisions

| ID | Decision | Status |
|---|---|---|
| APT-P-018 | `AppRating` 在 V1 是否曝光給其他 user | default: 不曝（V2 才有 tester 評分） |
| APT-P-019 | `CelebrationOverlay` 預設開啟 haptic? | default: 是（在 organisms 檔定義） |
