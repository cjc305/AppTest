# :feature:testing

> Tester progress dashboard: Active tests (含 ⚠ ping-overdue warning + Heartbeat-now / Abandon
> actions) + Completed tests with proof links. Filter chips (Active/Done/All).
> V1: FakeTestingRepository.

## Use it when
- `:app/nav` mounts Testing tab → `testingGraph(onTestClick, onProofClick)`

## Don't use it for
- Live heartbeat scheduling — `submitHeartbeat()` 是 demo stub；真排程 by APT-V1-R-041 WorkManager
- Proof card 顯示 — 由 `:feature:profile` 處理 proof gallery

## Key concepts
- `TestingSnapshot { active, completed }` — repo Flow 推送
- `TestStatus.AtRisk` 顯示 warning + extra action row
- Filter (Active/Done/All) combine 進 state；client-side filter

## Related
- spec_ref: [`_specs/wireframes.md`](../../_specs/wireframes.md) §5
- spec_ref: [`_specs/testing_exchange_flow.md`](../../_specs/testing_exchange_flow.md) F3/F4
- depends on: `:core:common`, `:core:designsystem`, `:core:ui`, `:core:domain`, `:core:navigation`
- dependents: `:app`
