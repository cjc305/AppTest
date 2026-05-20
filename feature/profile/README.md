# :feature:profile

> User profile: tier (large badge) + credits + 30d stats + reputation 4-sub-score breakdown
> + proof cards summary + activity history + invite CTA。
> V1: FakeProfileRepository.

## Use it when
- `:app/nav` mounts Profile tab → `profileGraph(onSettings, onInbox, onProof, onInvite)`

## Don't use it for
- Editing profile fields (display name / photo / locale) — V1 deferred；V2 加 Settings sub-screen
- Proof card 完整查看 — V1 點 proofs 卡片只導頁；V2 加 fullscreen proof viewer

## Key concepts
- `ProfileData` aggregate (user / stats / breakdown / proofs / activity)
- 4 sub-comps: `ProfileHeader`, `StatsCard`, `ReputationBreakdownCard`, inline `ProofsSummary`
- `AppTierBadge size=Large` 展示 Platinum gradient 在大尺寸
- 4 個 progress bars 對應 `_specs/reputation_system.md §2` 公式 maxValue

## Related
- spec_ref: [`_specs/wireframes_dev.md`](../../_specs/wireframes_dev.md) §3
- spec_ref: [`_specs/reputation_system.md`](../../_specs/reputation_system.md) §1, §2
- spec_ref: [`_specs/growth_and_network.md`](../../_specs/growth_and_network.md) §3.1 — invite CTA
- depends on: `:core:common`, `:core:designsystem`, `:core:ui`, `:core:domain`, `:core:navigation`
- dependents: `:app`
