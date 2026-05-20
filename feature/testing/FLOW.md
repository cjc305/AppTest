# :feature:testing — Flow

## Flow 1: render + filter

```mermaid
flowchart LR
    R[TestingRoute] --> VM[hiltViewModel]
    VM --> UC[ObserveTestingUseCase Flow snapshot]
    VM --> F[MutableStateFlow filter Active default]
    UC --> CB[combine snap filter]
    F --> CB
    CB --> M[mapState: empty? Empty : Loaded snap filter]
    M --> SF[stateIn]
    SF --> S[TestingScreen render]
    S --> R[onFilterChange] --> F
```

## Flow 2: AtRisk → Heartbeat-now

```mermaid
sequenceDiagram
    actor U
    participant S as Screen
    participant VM
    participant R as TestingRepository
    U->>S: tap "Heartbeat now"
    S->>VM: heartbeat testId
    VM->>R: submitHeartbeat testId
    R->>R: copy(pingStatusOk=true, status=Active)
    R-->>VM: Flow emit new snapshot
    VM->>S: re-render: row no longer AtRisk
```

## Flow 3: Abandon (with caution)

V1 直接呼叫 `repo.abandon(testId)` — 從 list 移除。V2 應加 confirmation dialog +
reputation penalty preview (per `reputation_system.md §6`).
