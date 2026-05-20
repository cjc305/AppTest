# :feature:appdetail — Internal Flow

> Load → render → Join CTA → 開 Play Store 路徑。Side-effect 走 Channel 而非 Composition。

## Flow 1: Initial load (from Home tap or deep link)

```mermaid
flowchart LR
    NH[NavHost composable AppDetail appId] --> R[AppDetailRoute]
    R --> VM[hiltViewModel AppDetailViewModel]
    VM --> SH[savedStateHandle.toRoute appId]
    VM --> INIT[init / load]
    INIT --> UC[GetAppDetailUseCase invoke appId]
    UC --> REPO[AppDetailRepository.getById]
    REPO --> FAKE[FakeAppDetailRepository mock 250ms]
    FAKE -->|Success AppDetailData| UC
    UC --> OK[onSuccess Loaded data]
    OK --> SF[state StateFlow update]
    SF --> R2[Route collectAsStateWithLifecycle]
    R2 --> S[AppDetailScreen render Loaded branch]
    S --> H[AppDetailHeader + ScreenshotsPlaceholder + Description + RequirementsSection + ExplainabilityCard + JoinFooter]
```

## Flow 2: Join CTA → open Play Store

```mermaid
sequenceDiagram
    actor U as User
    participant S as AppDetailScreen
    participant R as AppDetailRoute
    participant VM as AppDetailViewModel
    participant CH as Channel<String>
    participant CTX as Android Context

    U->>S: tap "Join test (1 credit)"
    S->>R: onJoin lambda
    R->>VM: onJoinClicked
    VM->>VM: state copy joinInProgress=true
    VM->>CH: send playOptInUrl
    VM->>VM: state copy joinInProgress=false
    R->>CH: collect via LaunchedEffect
    CH-->>R: emit url
    R->>CTX: startActivity Intent VIEW uri
    CTX-->>U: opens Play Store opt-in page
```

**Why Channel not StateFlow?** Channel is one-shot — re-collecting after recompose doesn't re-fire.
Avoids accidentally re-opening Play Store on rotation.

## Flow 3: Three-state branching

```mermaid
stateDiagram-v2
    [*] --> Loading: init
    Loading --> Loaded: getDetail Success
    Loading --> Error: getDetail Failure (AppError)
    Loaded --> Loaded: joinInProgress toggle (during open intent)
    Error --> Loading: onRetry
```

`joinInProgress` 是 Loaded 內部 boolean，不另開 state — 鈕變 loading 樣式即可。

## Flow 4: Deep link arrival

```mermaid
flowchart LR
    EXT[Push payload / External intent] -->|Uri apptest://app/abc| AM[AndroidManifest intent-filter match]
    AM --> MA[MainActivity onNewIntent]
    MA --> NH[NavHost auto-routes via navDeepLink]
    NH --> CD[composable AppDetail entry]
    CD --> TR[entry.toRoute appId=abc]
    TR --> R[AppDetailRoute appId=abc]
    R --> VM[VM loads abc]
```

設定在 `nav/AppDetailNavGraph.kt` 而非 :app — caller 不需知道 deep-link 細節。

## Flow 5: Empty-id error path

```mermaid
flowchart LR
    R[Route entered with appId empty - bug case] --> VM[VM load with empty]
    VM --> UC[invoke empty]
    UC --> REPO[FakeRepo getById empty]
    REPO -->|AppResult.Failure NotFound app| UC
    UC --> ERR[onFailure state=Error]
    ERR --> S[AppDetailScreen render AppErrorState onRetry=load]
```

Real backend should never send empty id; this guard is defense in depth.
