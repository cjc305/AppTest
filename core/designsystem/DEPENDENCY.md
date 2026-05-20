# :core:designsystem — Dependencies

> Module DAG view + 替換策略 + 測試策略。

## I depend on

| Dep | Why |
|---|---|
| `androidx.compose.material3` (BOM) | M3 `ColorScheme` / `Typography` / `Shapes` / Dynamic Color builders |
| `androidx.compose.ui` (BOM) | `Color`, `Dp`, `TextStyle`, `@Composable`, CompositionLocal primitives |
| `androidx.compose.ui-tooling-preview` | `@Preview` annotation used by `AppPreviewTheme` |
| `androidx.core:core-ktx` | `Context` helper used by Dynamic Color path |

**沒有**任何 `:core:*` / `:feature:*` 依賴 — 本模組是 leaf node（除了上面的 Compose / AndroidX）。

完整 Gradle 設定見 [`build.gradle.kts`](build.gradle.kts)。

## Modules depending on me

| Module | Uses what |
|---|---|
| `:app` | `AppTheme` (wrap setContent) |
| `:core:ui` | tokens for all atoms / molecules / organisms |
| `:feature:auth` | `AppTheme` (預覽用), `AppExtended.colors` for tier badges on sign-in social proof |
| `:feature:onboarding` | tokens for wizard UI |
| `:feature:home` | tokens for feed + cards |
| `:feature:myapps` | tokens for list + editor |
| `:feature:appdetail` | tokens + `AppExtended.colors.tierGold` for owner badge |
| `:feature:testing` | tokens + `AppExtended.colors.warning` for at-risk state |
| `:feature:profile` | tokens + all tier colors for reputation breakdown |
| `:feature:inbox` | tokens |

(Dependents list maintained best-effort; CI lint warns if stale per `auto_docs.md §6`)

## How to replace

替換整個 design system（如 V3 重 rebrand）：

1. 改 `AppColorScheme.kt`、`AppExtendedColors.kt`、`AppTypography.kt`、`AppShapes.kt`、`AppSpacing.kt` 內的值
2. **`AppTheme` 的 signature 不改**（避免 dependent 全 break）
3. 跑 `./gradlew :core:designsystem:assembleDebug`
4. 跑全部 `:core:ui` + `:feature:*` 的 Compose UI / screenshot test → 視覺差異視為 expected (commit baseline 更新)
5. 跑 `:app:assembleDebug` smoke

若要 A/B test 兩套 theme：
- 不改本模組
- 在 `:app` 自己提供 `AppTheme` wrapper 接 GrowthBook flag，根據 flag 切換 `darkTheme` / 自家 wrapper 或不同 token set

## How to test

| Test type | Tool | Scope |
|---|---|---|
| **Visual smoke** | Compose `@Preview` (Android Studio) | 每改 token 後手動掃所有 preview |
| **Screenshot test** | Paparazzi (no emulator) | run `./gradlew :core:designsystem:verifyPaparazzi`；CI 跑（per `cicd.md §2`） |
| **Token consistency** | Compose UI test | `createComposeRule` → assert `MaterialTheme.colorScheme.primary == AppLightColors.primary` 等 |
| **CompositionLocal** | Compose UI test | 套 `AppTheme` 內讀 `AppExtended.colors.tierGold` should equal expected hex |
| **Outside-theme guard** | Compose UI test | 在 plain `MaterialTheme` 內讀 `AppExtended.colors` should throw `IllegalStateException` |

V1 不對外發 SDK，故無 binary compatibility test。

## File budget

| File | Lines | Notes |
|---|---:|---|
| `theme/AppTheme.kt` | ~46 | entry composable |
| `theme/AppColorScheme.kt` | ~100 | 2 schemes × ~30 slots |
| `theme/AppExtendedColors.kt` | ~85 | tier + success + warning |
| `theme/AppTypography.kt` | ~70 | 8 type styles |
| `theme/AppShapes.kt` | ~18 | 5 shape tokens |
| `spacing/AppSpacing.kt` | ~22 | 7 spacing tokens |
| `preview/AppPreviewTheme.kt` | ~45 | preview wrapper + annotation |

每檔 ≤ 200 行 ✓ (hard rule)
