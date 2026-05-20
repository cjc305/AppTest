# :feature:profile — Flow

## Flow 1: render

```mermaid
flowchart LR
    R[ProfileRoute] --> VM[hiltViewModel]
    VM --> UC[GetProfileUseCase]
    UC --> REPO[ProfileRepository.getMyProfile]
    REPO --> FAKE[FakeProfileRepository mock 200ms]
    FAKE --> OK[onSuccess Loaded data]
    OK --> SF[StateFlow]
    SF --> S[ProfileScreen render]
    S --> H[ProfileHeader: avatar + name + AppTierBadge Large + credits]
    S --> ST[StatsCard 4 numbers]
    S --> RB[ReputationBreakdownCard 4 ProgressBars]
    S --> PS[ProofsSummary card tap → onProofClick first id]
    S --> AH[items activity events AppListItem]
    S --> IV[Invite CTA AppButton Tonal]
```

## Flow 2: TopBar actions
- Inbox icon → caller `onInboxClick` → `nav.navigate(AppDestination.Inbox)`
- Settings icon → caller `onSettingsClick` → `nav.navigate(AppDestination.Settings)`
- Invite CTA → caller `onInviteClick` → V1 stub (V2 share sheet with referral code)
