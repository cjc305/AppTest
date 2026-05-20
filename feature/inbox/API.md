# :feature:inbox — Public API

## Nav extension
```kotlin
fun NavGraphBuilder.inboxGraph(
    onNavigateUp: () -> Unit,
    onItemDeepLink: (String) -> Unit,    // tester app deep link
)
```
Mounts `composable<AppDestination.Inbox>` → InboxRoute.

## NOT public (internal)
- InboxRoute / InboxScreen / InboxViewModel / InboxUiState (ui/)
- InboxRepository / FakeInboxRepository / InboxDataModule (data/)
- ObserveInboxUseCase (domain/usecase/)
- InboxNotification / InboxNotificationType (domain/model/)

## Hilt bindings
| Type | Bound | Scope |
|---|---|---|
| `InboxRepository` | `FakeInboxRepository` | Singleton |
| `InboxViewModel` | self `@HiltViewModel` | per-NavGraph |
| `FakeInboxRepository` | self `@Singleton` | singleton |
