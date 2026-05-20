# :core:ui — Internal Flow

> 元件如何被組合 + state 如何被外界 hoist 進來 + 跨組件之間的合作關係。

## Flow 1: Atomic composition (一個 list-feed screen)

```mermaid
flowchart TB
    A[ScreenScaffold] --> B[AppTopBar]
    A --> C[content slot]
    A --> D[AppBottomBar]
    A --> E[AppFAB]
    C --> F{state}
    F -->|Loading| L[AppLoadingState]
    F -->|Error| ER[AppErrorState<br/>error: AppError<br/>onRetry]
    F -->|Empty| EM[AppEmptyState<br/>illustration / title / cta]
    F -->|Loaded| LC[LazyColumn]
    LC --> CARD[AppCard]
    CARD --> LI[AppListItem]
    LI --> TXT[AppText]
    LI --> ICN[AppIcon]
    LI --> TB[AppTierBadge]
    LC --> PB[AppProgressBar]
```

組合層次：Templates → Organisms → Molecules → Atoms (:core:designsystem)。

## Flow 2: State hoisting (organism 永不持業務 state)

```mermaid
flowchart LR
    A[Page-level ViewModel] -->|StateFlow| R[Route Composable]
    R -->|state + callbacks| S[Screen Composable<br/>Stateless]
    S -->|state matches| T{three-state pick}
    T -->|state.isLoading| L[AppLoadingState]
    T -->|state.error| E[AppErrorState onRetry=vm::retry]
    T -->|state.isEmpty| EM[AppEmptyState onCta=vm::createApp]
    T -->|state.items| LC[List render]
    E -.->|onRetry| A
    EM -.->|onCta| A
    LC -.->|onItemClick| A
```

**Hard rule:** organism / molecule **不**自己持業務 state；只接 state + callbacks。
UI-only state (expanded/collapsed / focused) 允許自己 `remember { mutableStateOf(...) }`。

## Flow 3: Theme token resolution

```mermaid
flowchart LR
    A[AppTheme outermost wrapper] --> B[MaterialTheme provides<br/>colorScheme / typography / shapes]
    A --> C[CompositionLocalProvider<br/>LocalAppExtendedColors]
    B --> M[any :core:ui molecule]
    C --> M
    M --> R1[MaterialTheme.colorScheme.surface]
    M --> R2[AppExtended.colors.tierGold]
    M --> R3[AppSpacing.Md]
```

Spacing 不走 CompositionLocal（不變值，直接 object reference）。其他 tokens 全靠 wrapper provider。

## Flow 4: Three-state contract (per AppEmptyState / AppErrorState / AppLoadingState)

```mermaid
stateDiagram-v2
    [*] --> Loading: initial fetch
    Loading --> Loaded: success + data
    Loading --> Empty: success + zero data
    Loading --> Error: network/HTTP/auth fail
    Loaded --> Loading: refresh
    Empty --> Loading: refresh / CTA action
    Error --> Loading: onRetry
```

Page-level ViewModel 決定 which state; UI 只 render。沒有 4th state（如 "stale" / "offline" — 那些併入 Loaded with metadata）。
