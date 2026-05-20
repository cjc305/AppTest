# :feature:testing — Dependencies

## I depend on
| Module | Why |
|---|---|
| `:core:common` | AppResult, AppError |
| `:core:designsystem` | tokens + AppText/AppVSpacer |
| `:core:ui` | ScreenScaffold, AppTopBar, AppListItem, AppFilterChip, AppButton, AppProgressBar, three-state organisms |
| `:core:domain` | Repository marker |
| `:core:navigation` | AppDestination.Testing |

## Modules depending on me
`:app` only via `testingGraph(...)`.

## How to replace Fake → real
1. RealTestingRepository wraps Supabase REST `GET /me/tests` + Realtime `test_requests:me`
2. di TestingDataModule 改 @Binds
3. UI/VM 不動 (Flow contract preserved)
4. submitHeartbeat 真實 impl 走 `POST /tests/:id/heartbeat` + Idempotency-Key

## How to test
| Test | Tool | Scope |
|---|---|---|
| Unit (VM) | Turbine | combine snapshot + filter; Active filter hides Completed; Done filter hides Active |
| Unit (Fake) | JUnit | heartbeat resets AtRisk → Active; abandon removes from active list |
| Compose UI | createComposeRule | filter chip toggle; AtRisk row shows extra actions |

## File budget
- TestingScreen ~150
- TestingViewModel ~45
- FakeTestingRepository ~52
- 其餘 < 30

每檔 ≤ 200 ✓

## Deferred
- Pull-to-refresh
- Abandon confirmation dialog + rep penalty preview
- Real-time heartbeat status badge color animation
