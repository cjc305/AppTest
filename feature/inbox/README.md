# :feature:inbox

> In-app notification list (V1 替代 push notification center). Match alerts + heartbeat
> reminders + reputation events + completion notices.

## Use it when
- `:app/nav` 想 expose Inbox destination → `inboxGraph(onNavigateUp, onItemDeepLink)`
- 替換 Fake → 改 `data/di/InboxDataModule.kt` 內 @Binds

## Don't use it for
- 系統 push notification 顯示 — 那是 FCM service (APT-V1-R-042)
- 通知優先序設定 — 走 `notification_preferences` 表，feature:profile 內

## Key concepts
- `InboxNotificationType` (NewMatch / HeartbeatReminder / ReputationChange / Completion / System)
- `InboxNotification` 含 optional `deepLink` (`apptest://app/{id}` 等)
- Read state in-memory (V1)；V2 server-side persistence
- Tap item → mark read + open deep link (if any)

## Related
- spec_ref: [`_specs/api_contracts.md`](../../_specs/api_contracts.md) §10-11 — WebSocket realtime + FCM payload
- depends on: `:core:common`, `:core:designsystem`, `:core:ui`, `:core:domain`, `:core:navigation`
- dependents: `:app` only
- 完整 deps + 測試 [`DEPENDENCY.md`](DEPENDENCY.md)
