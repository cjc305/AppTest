# :feature:inbox — Flow

## Flow 1: List + mark-read

```mermaid
flowchart LR
    NH[NavHost Inbox] --> R[InboxRoute]
    R --> VM[hiltViewModel]
    VM --> UC[ObserveInboxUseCase]
    UC --> REPO[InboxRepository.observe Flow]
    REPO --> MAP[map: empty? Empty : Loaded with unreadCount]
    MAP --> SF[stateIn StateFlow]
    SF --> S[InboxScreen render]
    S -->|tap row| R2[Route onItemClick]
    R2 --> VM2[markRead id]
    R2 --> DL{deepLink null?}
    DL -->|no| OUT[caller onItemDeepLink url]
    DL -->|yes| END[no-op]
    VM2 --> REPO2[repo.markRead]
    REPO2 --> SF
```

## Flow 2: Mark-all-read action
TopBar action visible only when `unreadCount > 0`. Tap → `markAllRead()` → repo mutates Flow → UI auto-refresh (rows lose unread dot opacity).
