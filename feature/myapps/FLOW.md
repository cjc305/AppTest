# :feature:myapps — Internal Flow

> List ↔ Editor 同步、Editor validation、Save 路徑。

## Flow 1: List render with live updates

```mermaid
flowchart LR
    NH[NavHost composable AppDestination.MyApps] --> MAR[MyAppsRoute]
    MAR --> VM[hiltViewModel MyAppsViewModel]
    VM --> UC[GetMyAppsUseCase invoke]
    UC --> REPO[MyAppsRepository.observe]
    REPO --> FAKE[FakeMyAppsRepository<br/>MutableStateFlow seed]
    FAKE -->|Flow List| MAP[map: items.isEmpty? Empty : Loaded]
    MAP --> SF[stateIn WhileSubscribed]
    SF --> MAS[MyAppsScreen state]
```

`stateIn(WhileSubscribed(5_000))` 讓 ViewModel 在無 collector 時釋放 upstream；rotation 內
不重新 collect（5s 緩衝）。

## Flow 2: Create flow

```mermaid
sequenceDiagram
    actor U as User
    participant L as MyAppsScreen list
    participant N as NavController
    participant E as AppEditorRoute
    participant V as AppEditorViewModel
    participant UC as SaveAppUseCase
    participant R as FakeMyAppsRepository

    U->>L: tap [+] FAB
    L->>N: onCreate → onNavigateToEditor null
    N->>E: navigate AppEditor null
    E->>V: hiltViewModel + savedStateHandle.toRoute
    V->>V: state isEdit=false draft=AppDraft default
    loop user typing
        U->>E: onField copy field
        E->>V: onField update
        V->>V: recomputed (urlValidation + canSave)
        V->>E: emit new state
        E->>U: re-render isError flags
    end
    U->>E: tap Save
    E->>V: save
    V->>UC: invoke draft
    UC->>UC: validate (server-side rules)
    UC->>R: save draft
    R->>R: id = UUID + append to _items
    R-->>UC: AppResult.Success id
    UC-->>V: onSuccess
    V->>V: state savedId=id
    E->>E: LaunchedEffect savedId then onSaved
    E->>N: popBackStack
    N->>L: re-render list shows new row (Flow auto)
```

## Flow 3: Edit flow

```mermaid
sequenceDiagram
    actor U
    participant L as List
    participant N as NavController
    participant E as Editor
    participant V as VM
    participant R as Repo

    U->>L: tap row
    L->>N: onEdit appId → navigate AppEditor appId
    N->>E: enter
    E->>V: init
    V->>V: route.appId not null → isEdit=true isLoading=true
    V->>R: get id
    R-->>V: OwnedAppRow
    V->>V: state draft=AppDraft(id, name, packageName, ...) isLoading=false
    Note over V: packageName field disabled (unique constraint)
    U->>E: edit fields + Save
    E->>V: save (existing draft.id set)
    V->>R: save draft (id present)
    R->>R: map update existing row
    R-->>V: Success id
    V->>E: savedId set → onSaved
```

## Flow 4: Play URL validation (per keystroke)

```mermaid
stateDiagram-v2
    Empty: PlayUrlValidation.Empty
    Valid: PlayUrlValidation.Valid
    Invalid: PlayUrlValidation.Invalid reason

    [*] --> Empty: initial / cleared
    Empty --> Invalid: typed non-empty NOT https
    Empty --> Valid: typed https://play.google.com/x
    Valid --> Invalid: changed away from play.google.com
    Invalid --> Valid: corrected
    Valid --> Empty: cleared
    Invalid --> Empty: cleared
```

實作在 `PlayOptInUrlValidator.kt`（pure object）。VM `recomputed()` 每次 onField 都呼叫。

## Flow 5: Save validation cascade

Two layers of validation:
1. **UI live** (`AppEditorViewModel.recomputed`) — drives `canSave` button enabled state
2. **Final-shot** (`SaveAppUseCase.validate`) — re-checks all rules before repo call; returns
   `AppError.Validation(field, message)` if any fail. Never trust UI alone.

If validation fails server-side (post-real-backend) → `AppResult.Failure(AppError)` → `saveError`
shown above action row.

## State machine: AppEditorUiState

Single mutable shape (not sealed) — form always visible. Flags toggle behaviour:
- `isLoading` (initial edit fetch only)
- `isSaving` (during save call)
- `savedId != null` (signal to navigate up via `LaunchedEffect`)
- `loadError` / `saveError` (banner / inline)
- `canSave` (derived; not user-settable)
