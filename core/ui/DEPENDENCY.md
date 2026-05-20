# :core:ui — Dependencies

> Module DAG view + 替換策略 + 測試策略。

## I depend on

| Dep | Why |
|---|---|
| `:core:designsystem` | tokens (color/typography/shape/spacing/extended) + atoms (AppText/Icon/Spacer/Divider/Placeholder) |
| `:core:common` | `AppError` (consumed by `AppErrorState`), `ReputationTier` (consumed by `AppTierBadge`) |
| `androidx.compose.material3` (BOM) | M3 Button family, Card, ListItem, Chip, Badge, ProgressIndicator, Snackbar, Scaffold, NavigationBar, FAB, TopAppBar, SearchBar, AdaptiveLayout |
| `androidx.compose.ui` (BOM) | `Modifier`, `Composable`, `Color`, `Dp`, layout primitives, semantics |
| `androidx.compose.foundation` (BOM) | `clickable`, `Row` / `Column` / `Box`, shapes |
| `androidx.material3:material3-window-size-class` | `WindowSizeClass` for `AdaptiveTwoPane` |
| `io.coil-kt.coil3:coil-compose` | reserved for future remote image use; molecules don't import yet |

**No** dependency on `:core:domain`, `:core:data`, `:core:network`, `:core:database`, any `:feature:*`.

完整 Gradle 設定見 [`build.gradle.kts`](build.gradle.kts)。

## Modules depending on me

| Module | Uses what |
|---|---|
| `:app` | `ScreenScaffold` in MainActivity host; `AppBottomBar` for top-level nav |
| `:feature:auth` | `AppButton` (sign-in CTAs), `AppText`, `AppSearchBar` (email field, future) |
| `:feature:onboarding` | `AppProgressBar` (step indicator), `AppFilterChip` (category picker), `AppCard` (welcome) |
| `:feature:home` | full org set: TopBar, BottomBar, FAB, three-state, AppCard for feed items |
| `:feature:myapps` | List + EmptyState + Editor (forms use molecules) |
| `:feature:appdetail` | AppCard (hero), AppTierBadge (owner), AppButton (Join), AppRating |
| `:feature:testing` | AppCard, AppProgressBar (days completion), AppBadge (streak) |
| `:feature:profile` | AppTierBadge (large), AppProgressBar × 4 (sub-score breakdown), AppListItem (activity) |
| `:feature:inbox` | AppListItem + AppSnackbar variants |

(Dependents 維護 best-effort; CI lint 警告 stale 條目 per `auto_docs.md §6`.)

## How to replace

### Replace a single molecule (e.g., new AppButton variant)
1. Add variant to `AppButtonVariant` enum + handle branch in `AppButton.kt`
2. Updated `API.md` 條目
3. Run `:core:ui` Compose UI tests + screenshot tests

### Replace whole UI catalog (e.g., V3 rebrand)
1. Tokens 換在 `:core:designsystem` (per its `DEPENDENCY.md` "How to replace")
2. Public API of `:core:ui` 不改 — 所有 dependent feature 自動視覺更新
3. 跑 all `:feature:*` screenshot tests → 視差視為 expected, commit baseline

### A/B test (e.g., 兩種 AppCard 樣式 試比)
- 不改 `:core:ui` — 在 feature 內條件 render 不同 variant 或 wrap with feature flag
- 或 expose 新 variant，feature 控制使用比例

## How to test

| Test type | Tool | Scope |
|---|---|---|
| **Unit (Compose UI test)** | `createComposeRule` | molecule render + click; `AppButton(loading=true)` 不可 click; `AppFilterChip` toggle works |
| **State coverage** | `createComposeRule` parametrized | 三態組件 (Loading/Error/Empty) 各種 inputs |
| **Screenshot test** | Paparazzi | Light + Dark × 每個 component 主 state；commits baseline PNGs |
| **CompositionLocal guard** | `createComposeRule` | `AppTierBadge` 套 plain MaterialTheme（無 AppExtended）should throw |
| **Accessibility** | `assertContentDescriptionEquals` | every icon-only molecule has cd; AppRating semantics 對 |

CI 跑前 3 類 (`cicd.md §2`)。Accessibility 跑 weekly nightly。

## File budget

| File | Lines | Notes |
|---|---:|---|
| `components/AppButton.kt` | ~60 | 5 variants |
| `components/AppCard.kt` | ~43 | 3 variants × onClick null/non-null |
| `components/AppListItem.kt` | ~28 | M3 wrapper |
| `components/AppChips.kt` | ~50 | AppChip + AppFilterChip combined |
| `components/AppBadge.kt` | ~25 | wrapper |
| `components/AppTierBadge.kt` | ~75 | Platinum gradient special-case |
| `components/AppProgressBar.kt` | ~37 | label + progress |
| `components/AppRating.kt` | ~38 | character-glyph stars |
| `components/AppTopBar.kt` | ~52 | 3 styles |
| `components/AppBottomBar.kt` | ~45 | destinations contract |
| `components/AppFAB.kt` | ~37 | extended / mini |
| `components/AppSearchBar.kt` | ~38 | OutlinedTextField wrapper |
| `components/AppStateViews.kt` | ~120 | 3 states + errorTitle |
| `components/AppSnackbar.kt` | ~70 | 4 severities |
| `templates/ScreenScaffold.kt` | ~42 | edge-to-edge default |
| `templates/AdaptiveTwoPane.kt` | ~42 | responsive 2-pane |

每檔 ≤ 200 行 ✓ (hard rule)

## Deferred (next companion tasks)

- `AppFloatingPanel` (organism) — 需 `AppGlass` token in :core:designsystem
- `CelebrationOverlay` (template) — 需 Spatial Layout 3-layer + particle helper
- `AppToast` 獨立 wrapper — 暫用 `AppSnackbar(severity=Info)` 替代
- 每個 organism 的 `@Preview` 5-state matrix（per `compose_organisms_templates.md §4`）— 待 Paparazzi CI 接上時一起補
