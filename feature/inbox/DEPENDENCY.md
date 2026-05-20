# :feature:inbox — Dependencies

## I depend on
| Module | Why |
|---|---|
| `:core:common` | AppResult, AppError |
| `:core:designsystem` | tokens + AppText/AppIcon |
| `:core:ui` | ScreenScaffold, AppTopBar, AppListItem, AppEmptyState |
| `:core:domain` | (Repository marker; no UseCase base needed since ObserveUseCase is sync Flow) |
| `:core:navigation` | AppDestination.Inbox |

## Modules depending on me
`:app` only — via `inboxGraph(...)`.

## How to replace Fake → real
1. `class RealInboxRepository @Inject constructor(api, db) : InboxRepository`
2. `data/di/InboxDataModule` 改 @Binds
3. UI 不動。
4. Real impl 接 Supabase Realtime channel `notifications:me` + FCM 收信時 cache 到 local Room。

## How to test
| Test | Tool | Scope |
|---|---|---|
| Unit (VM) | Turbine | state emits Empty / Loaded with unreadCount; markAllRead → all isRead=true |
| Unit (Fake) | JUnit | seed gives 4 items; markRead by id mutates only that row |
| Compose UI | createComposeRule | Mark-all-read button only when unread > 0; tap row → onItemClick |

## File budget
| File | Lines |
|---|---:|
| build.gradle.kts | ~45 |
| nav/InboxNavGraph.kt | ~18 |
| ui/InboxScreen.kt | ~115 |
| ui/InboxViewModel.kt | ~35 |
| ui/InboxRoute.kt | ~22 |
| ui/InboxUiState.kt | ~12 |
| domain/usecase/ObserveInboxUseCase.kt | ~10 |
| domain/model/InboxModels.kt | ~16 |
| data/InboxRepository.kt | ~12 |
| data/FakeInboxRepository.kt | ~72 |
| data/di/InboxDataModule.kt | ~16 |

每檔 ≤ 200 行 ✓.

## Deferred
- Pull-to-refresh / pagination
- 通知分類 tabs (Match / Heartbeat / Other)
- Snooze / archive actions
