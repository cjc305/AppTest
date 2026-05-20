# :feature:auth

> Sign-in (Google + Email magic link) + Verify screens. Provides V1 `FakeAuthRepository`
> impl for the `AuthRepository` interface in `:core:domain/auth`.

## Use it when
- `:app/nav` mounts AuthRoot subgraph → `authGraph()` (no callbacks — AuthState drives nav)
- Real Supabase / Google integration when APT-V1-R-043 lands → replace FakeAuthRepository

## Don't use it for
- Account settings / sign-out — Settings 在 `:feature:profile` 內 (calls `authRepo.signOut`)
- Onboarding completion signal — `:feature:onboarding` 自己 calls `authRepo.markOnboardingComplete`

## Key concepts
- **Public `AuthRepository`** in `:core:domain/auth` — both `:feature:auth` (writer) and `:feature:onboarding`/`:app` (readers) import without cross-feature dep
- **Nav driven by AuthState**, not callbacks — flip `state` → MainActivity re-keys NavHost startDestination
- **`SignInUiState` sealed 5 case** — Idle / EnteringEmail / Working / MagicLinkSent / Error
- **EmailVerify** consumes route arg `email` (V1 mock token = email itself)

## V1 fakes
- `signInWithGoogle()` 600ms → NeedsOnboarding
- `requestMagicLink(email)` 400ms (validates contains @ and .)
- `verifyMagicLink(token)` 400ms → NeedsOnboarding
- `markOnboardingComplete()` → Ready
- `signOut()` → SignedOut

## Related
- spec_ref: [`_specs/wireframes.md`](../../_specs/wireframes.md) §1 Sign In
- spec_ref: [`_specs/onboarding_ux.md`](../../_specs/onboarding_ux.md) §5
- spec_ref: [`_specs/navigation.md`](../../_specs/navigation.md) §5 Auth gate
- depends on: `:core:common`, `:core:designsystem`, `:core:ui`, `:core:domain`, `:core:navigation`
- dependents: `:app` (provides Hilt impl) + `:feature:onboarding` (consumes interface)
