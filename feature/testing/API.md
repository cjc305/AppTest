# :feature:testing — Public API

## Nav extension
```kotlin
fun NavGraphBuilder.testingGraph(
    onTestClick: (appId: String) -> Unit,
    onProofClick: (proofId: String) -> Unit,
)
```

## NOT public
- TestingRoute/Screen/ViewModel/UiState (ui/)
- TestingRepository / FakeTestingRepository / TestingDataModule (data/)
- ObserveTestingUseCase (domain/usecase/)
- ActiveTestEntry / CompletedTestEntry / TestStatus / TestFilter / TestingSnapshot (domain/model/)

## Hilt bindings
- `TestingRepository` → `FakeTestingRepository` (Singleton)
- `TestingViewModel` self `@HiltViewModel`
