# AppTest — Design System (M3 Expressive + 2026 H2)

> **Version:** 0.1 · **Last updated:** 2026-05-19 · **Owner:** TBD
> 視覺與互動的 vocabulary。Composable 本體在 `compose_components.md`，本檔只定義 tokens。

---

## 1. Design principles (5 條，任一決策都須對齊)

1. **Trust by visibility** — 任何信任訊號（tier、completion %、proof）都顯眼，不藏 menu 深處。
2. **Earned, not promoted** — 不用顏色或大小區別「有付費」帳號；只有 reputation tier 例外（earned）。
3. **Calm by default** — 預設低飽和、低對比；高對比保留給 actionable surface（CTA / 警示）。
4. **Spatial = Hierarchy** — 用景深（layers）表達重要性，不用顏色強度。
5. **Motion as feedback** — 每個 state transition 都有對應 motion；沒有 motion 的 UI 像「沒回應」。

## 2. Color tokens

### 2.1 Strategy
Dynamic Color 優先 (Android 12+)；fallback 用品牌 palette。所有顏色取自 `MaterialTheme.colorScheme.*`，**禁止 hard-code Color(0x...)**。

### 2.2 Brand palette (fallback, when no dynamic)
```
primary       = #4F46E5   // indigo-600
onPrimary     = #FFFFFF
primaryContainer = #E0E7FF
secondary     = #06B6D4   // cyan-500 (Tester action)
tertiary      = #F59E0B   // amber-500 (Reputation badges)
error         = #DC2626
success       = #16A34A   // 自訂語意（M3 無 success，extend via extra color scheme）
surface       = dynamic
surfaceTint   = dynamic
```

### 2.3 Reputation tier colors (語意)
```
Newcomer   surface variant + thin border
Bronze     #B97A56  (warm bronze)
Silver     #BFC3C9
Gold       #FFC93C  (high-luminance, M3 expressive accent)
Platinum   linear-gradient(135deg, #E5E4E2, #A8C5E6) -- 雙色金屬
```

Tier 顯示**永遠**用 `AppTierBadge` 元件，不可直接取色（換 theme 才一致）。

## 3. Typography (M3 Expressive scale)

依 M3 Expressive type scale，採變字重 (variable font weight) 過渡：

| Role | Size / Weight / LH | Usage |
|---|---|---|
| displayLarge | 57 / 400 / 64 | 慶祝畫面（升 tier / 達 12 testers） |
| headlineLarge | 32 / 600 / 40 | Screen titles |
| headlineMedium | 28 / 600 / 36 | Section heads |
| titleLarge | 22 / 500 / 28 | Card titles, dialog titles |
| bodyLarge | 16 / 400 / 24 | 主內文 |
| bodyMedium | 14 / 400 / 20 | secondary 內文 |
| labelLarge | 14 / 600 / 20 | Button label, badge |
| labelSmall | 11 / 600 / 16 | Metadata, time stamps |

Font: System default (Roboto on most Android)，preview 用 Variable font 體驗 expressive weight。

## 4. Shape tokens

```
extraSmall: 4dp     -- chip, badge
small:      8dp     -- button, small card
medium:     16dp    -- card
large:      28dp    -- bottom sheet, hero
extraLarge: 40dp    -- modal, full-screen-ish surfaces
```

M3 Expressive 鼓勵更大圓角；hero 一律用 `large` 起跳。

## 5. Elevation & surface

| Level | Use | M3 mapping |
|---|---|---|
| 0 (flat) | 背景 | `surface` |
| 1 | inactive card | `surfaceContainerLow` |
| 2 | active card | `surfaceContainer` |
| 3 | floating action | `surfaceContainerHigh` |
| 4 | dialog / sheet | `surfaceContainerHighest` |

不靠 dropShadow 區辨層級，靠 surfaceContainer 語意。

## 6. Motion system (M3 Expressive motion)

預設用 M3 spring tokens：

| Token | Spec | Use |
|---|---|---|
| `motionSchemeStandard` | spring 300ms damping 0.85 | 一般 transition |
| `motionSchemeExpressive` | spring 450ms damping 0.7 (overshoot) | 慶祝 / 強調 |
| `fadeIn(120ms) + slideInVertically(200ms)` | combined | Screen enter |
| `crossfade(200ms)` | | Tab switch |

Hard rule: **任何 state change 都搭 motion**；不准 hard cut。

## 7. Glass Surface (2026 H2 新元素)

霧化背板，用在覆蓋層（dialog 底、bottom sheet 頂）：

```kotlin
GlassSurface(
  blurRadius = 32.dp,
  noiseAlpha = 0.04f,         // 加微噪點避免 banding
  surfaceTint = colorScheme.surfaceContainer,
  tintAlpha = 0.72f,
)
```

**禁用條件：**
- Battery saver on → fallback opaque `surfaceContainerHighest`
- Reduce Motion accessibility on → 同 fallback
- API < 31 → 同 fallback (RenderEffect 需 S+)

## 8. Floating Panels

浮動面板（不是 dialog 不是 sheet），錨定在某 element 旁，spring-in：

```
Anchor: 觸發元素 bounds
Offset: 8dp from anchor edge, biased to inside-screen
Motion: motionSchemeExpressive
Shape:  large rounded + glass surface
Dismiss: tap outside / Esc / swipe down
A11y:   trap focus inside, ESC keyboard support
```

用例：篩選器 (filter popover)、quick action menu、reputation tier 解釋卡。

## 9. Spatial Layout (depth + parallax)

三個 z-depth 層級：

```
Layer 0 (background)   z=-1   parallax 0.4×
Layer 1 (content)      z=0    parallax 1.0× (anchor)
Layer 2 (foreground)   z=+1   parallax 1.2×
```

Hero 畫面（升 tier 慶祝 / 達標證明）才用全 3 layers；一般 list 畫面只用 layer 1。

## 10. Iconography

- **System icons:** Material Symbols (Outlined 預設, Rounded 在 expressive 場景)
- **Brand icons:** custom SVG，放 `:core:designsystem/src/main/res/drawable/`
- **Size scale:** 16 / 20 / 24 / 32 / 40 dp（與 typography 對齊）

## 11. Layout & spacing

8dp grid。間距 token：

```
spaceXxs = 2dp · spaceXs = 4dp · spaceSm = 8dp · spaceMd = 16dp ·
spaceLg = 24dp · spaceXl = 32dp · spaceXxl = 48dp
```

Screen padding: `spaceMd` 預設；large screen ≥ `WindowSizeClass.Medium` 改 `spaceLg + max-width 720dp 置中`。

## 12. Accessibility (hard rules)

- 所有可點擊 element 提供 `Modifier.semantics { contentDescription = "..." }`
- 動態文字大小（Settings 字體放大）至 1.5× 不壞版
- Color contrast ≥ AA (4.5:1 for body, 3:1 for large text)
- TalkBack focus 順序符合視覺順序
- Reduce Motion / Reduce Transparency 偏好自動偵測，影響 §6/§7

## 13. Token export

所有 tokens 都包成 Composable functions 在 `:core:designsystem/theme/`：
- `AppTheme(...)` — 入口
- `AppColors`, `AppTypography`, `AppShapes`, `AppMotion` — token holders
- `AppGlass`, `AppFloating`, `AppSpatial` — extra tokens

Compose preview 都用 `AppPreviewTheme()` wrapper 確保一致。

## 14. Open decisions

| ID | Decision | Status |
|---|---|---|
| APT-P-015 | Brand palette 是否找設計師專業調色 | open: V1 用上述 placeholder |
| APT-P-016 | Reputation tier 動畫升級表現（粒子？光效？） | draft: 粒子 + 光暈 + haptic |
| APT-P-017 | 字體是否引入品牌字（vs system default） | default: V1 用 system |
