# :core:designsystem

> 視覺與互動的 vocabulary：M3 Expressive theme + Dynamic Color + 自定 tier/success/warning colors + spacing tokens + preview wrapper + **5 個 atomic Composables**.
> Molecules / Organisms / Templates 在 `:core:ui`（往上一層）。Pages 在 `:feature:*`。

## Use it when

- 任何 `:feature:*` 或 `:core:ui` 元件需取 color / typography / shape / spacing token。
- 任何 screen 需要 wrap `AppTheme { ... }`（通常 `:app` 的 MainActivity 套一次即可）。
- 任何 `@Preview` 需要 reproducible theme — 用 `AppPreviewTheme { ... }` (Dynamic Color 自動關閉)。

## Don't use it for

- 客製單一元件外觀 — 那是 `:core:ui` 的事。
- 業務邏輯 / data — 純 UI tokens module。
- Glass Surface / Floating Panel / Spatial Layout 實作 — V1 範圍未含 (per `design_system.md` §7-9)，由未來 companion files 提供。

## Key concepts

- `AppTheme(darkTheme, dynamicColor, content)` — entry point. 套一次包整個 App。
- `MaterialTheme.colorScheme.*` — 標準 M3 顏色（primary, surface, error, ...）。
- `AppExtended.colors.tierGold` / `.success` / `.warning` — M3 沒有的 slot，走 CompositionLocal。
- `AppSpacing.Md` — 8dp grid 上的固定值。**禁止** hard-code `16.dp` 字面值（per `compose_components.md §6`）。
- `AppPreviewTheme { ... }` — 包 `@Preview` 用，Dynamic Color off + 統一 padding。
- **Atoms** (`components/`): `AppText` / `AppIcon` / `AppSpacer` (V/H/Square) / `AppDivider` (H/V) / `AppPlaceholder` — 最薄包裝層，固定 token defaults。

## Quick example

```kotlin
@Composable
fun MyAtom() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.padding(AppSpacing.Md),
    ) {
        Text(
            text = "Hello",
            style = MaterialTheme.typography.titleLarge,
            color = AppExtended.colors.tierGold,
        )
    }
}

@AppPreviewLightDark
@Composable
private fun MyAtomPreview() = AppPreviewTheme { MyAtom() }
```

## Related

- spec_ref: [`_specs/design_system.md`](../../_specs/design_system.md) — canonical token specs
- spec_ref: [`_specs/reputation_system.md`](../../_specs/reputation_system.md) §1 — tier semantics
- depends on: (only Compose + M3 from version catalog; no other internal module)
- dependents: `:core:ui`, all `:feature:*`, `:app`
- 完整依賴清單見 [`DEPENDENCY.md`](DEPENDENCY.md)
