# :feature:profile — Public API

## Nav extension
```kotlin
fun NavGraphBuilder.profileGraph(
    onSettingsClick: () -> Unit,
    onInboxClick: () -> Unit,
    onProofClick: (proofId: String) -> Unit,
    onInviteClick: () -> Unit,
)
```

## NOT public (internal)
- ProfileRoute/Screen/ViewModel/UiState (ui/)
- ProfileHeader / StatsCard / ReputationBreakdownCard (ui/components/)
- ProfileRepository / FakeProfileRepository / ProfileDataModule (data/)
- GetProfileUseCase (domain/usecase/)
- ProfileUser / ProfileStats30d / ReputationBreakdown / ProofCardSummary / ActivityEvent / ProfileData (domain/model/)

## Hilt bindings
- `ProfileRepository` → `FakeProfileRepository` Singleton
- `ProfileViewModel` self `@HiltViewModel`
- `GetProfileUseCase` self `@Inject` (uses `DispatcherProvider` from `:app/di/CoreModule`)
