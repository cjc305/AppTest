# :feature:onboarding — Public API

## Nav extension
```kotlin
fun NavGraphBuilder.onboardingGraph()
```
No callbacks. Wizard completion triggers `AuthRepository.markOnboardingComplete()` which
drives navigation via AuthState.

## NOT public (internal)
- OnboardingRoute/Screen/ViewModel/UiState (ui/)
- OnboardingDraft / OnboardingIntent / OnboardingCatalog (domain/model/)

## Hilt bindings contributed
- `OnboardingViewModel` self `@HiltViewModel` (injects `AuthRepository` from `:core:domain/auth`)
- No own Repository — writes via shared AuthRepository (V1)；future: also write to ProfileRepository for primary_category / preferred_categories / locale persistence (`database_schema.md §2`)
