# :core:navigation — Internal Flow

> NavHost startDestination 決策 + deep-link 解析 + 跨 feature 跳轉的 wiring。

## Flow 1: Auth-gated start destination

```mermaid
flowchart LR
    A[:feature:auth<br/>AuthRepository] -->|Flow&lt;AuthState&gt;| B[:app MainActivity]
    B --> R[remember(authState)]
    R --> F[startDestinationFor]
    F -->|SignedOut| AR[AppDestination.AuthRoot]
    F -->|NeedsOnboarding| OR[AppDestination.OnboardingRoot]
    F -->|Ready| MR[AppDestination.MainRoot]
    AR --> NH[NavHost startDestination]
    OR --> NH
    MR --> NH
```

`startDestinationFor` 是 pure 函數，可放在 `remember(authState) { ... }` 內不引發無限重組。

## Flow 2: Cross-feature navigation (type-safe)

```mermaid
flowchart LR
    H[Home Composable] -->|onCardClick id| N[navController.navigate<br/>AppDestination.AppDetail id]
    N --> RES[NavHost composable&lt;AppDetail&gt;<br/>resolves entry]
    RES --> T[entry.toRoute&lt;AppDetail&gt;]
    T --> AD[AppDetailRoute appId=...]
```

每個 feature 在自己 `nav/` 包提供 `NavGraphBuilder.<feature>Graph(navController)`；本 module 不提供 helper（Compose Nav 2.8+ 內建 `composable<T>` 已足夠）。

## Flow 3: Deep-link routing

```mermaid
flowchart LR
    EXT[Outside intent / FCM payload] -->|Uri| MA[MainActivity onNewIntent]
    MA --> P{AppDeepLink.parse}
    P -->|AppDetail| ND[nav.navigate AppDestination.AppDetail]
    P -->|Testing| NT[nav.navigate AppDestination.Testing]
    P -->|AuthRoot+ref| NA[nav.navigate AuthRoot + record referral]
    P -->|null| FB[ignore, log warning]

    NH[NavHost composable&lt;AppDetail&gt;<br/>+ navDeepLink uriPattern] -.->|auto-bind FROM intent| NP[no manual parse needed]
```

兩條路徑：
- **Auto-binding** (`navDeepLink { uriPattern = AppDeepLink.PATTERN_APP_DETAIL_* }` in composable) — Compose Nav 自動接住外部 intent，無需 parse 程式碼
- **Manual parse** (`AppDeepLink.parse(uri)`) — 只在 push payload / 自家程式碼從非 nav 來源拿到 URI 時用

## Flow 4: Sign-out / state-reset

```mermaid
flowchart LR
    U[user taps Sign out] --> AR[AuthRepository.signOut]
    AR --> SE[emit AuthState.SignedOut]
    SE --> AS[MainActivity re-evaluates start]
    AS --> NA[nav.navigate AuthRoot<br/>popUpTo&lt;MainRoot&gt; inclusive]
```

Sign-out 不只切換 state — 同步 popBackStack to clear 主畫面歷史，避免按返回鍵回到登入後狀態。

## State machine (AuthState)

```mermaid
stateDiagram-v2
    [*] --> SignedOut: 首次啟動或 token 失效
    SignedOut --> NeedsOnboarding: sign-in 成功 + new profile
    SignedOut --> Ready: sign-in 成功 + existing profile + onboarded
    NeedsOnboarding --> Ready: onboarding 完成
    Ready --> SignedOut: sign-out / token expired
    NeedsOnboarding --> SignedOut: skip / sign-out from onboarding
```

3 states × 受限 transitions。`:feature:auth` 負責所有寫；`:app` 只讀。
