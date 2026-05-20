# :feature:auth — Public API

## Nav extension
```kotlin
fun NavGraphBuilder.authGraph()
```
No callbacks — navigation reacts to `AuthRepository.state` flow at MainActivity level.

## NOT public (internal)
- SignInRoute/Screen/ViewModel/UiState (ui/signin/)
- EmailVerifyRoute/ViewModel/UiState (ui/verify/)
- FakeAuthRepository / AuthDataModule (data/)

## Hilt bindings contributed
- `com.apptest.core.domain.auth.AuthRepository` → `FakeAuthRepository` (Singleton)
- `SignInViewModel` self `@HiltViewModel`
- `EmailVerifyViewModel` self `@HiltViewModel` (reads `AppDestination.EmailVerify.email` from SavedStateHandle)

## Public surface from `:core:domain/auth`
This feature is the **writer** of `AuthRepository`. The interface itself is `:core:domain`'s
public API (see `core/domain/.../auth/AuthRepository.kt`).
