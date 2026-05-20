# :feature:onboarding

> 3-step first-run wizard (Intent / Category / Language) per `_specs/onboarding_ux.md`.
> Single screen with internal `currentStep` state — simpler than nested NavHost for V1.

## Use it when
- `:app/nav` mounts OnboardingRoot → `onboardingGraph()` (no callbacks)

## Don't use it for
- Welcome moment / Done card — those are aspirational per spec §6/§10, V1 deferred
- Re-onboarding (settings change) — V1 only first-run; future flow needs separate entry

## Key concepts
- `OnboardingDraft { intent, categories, languages }` form state
- Validates per step: Step 1 always valid (default selected); Step 2 ≥ 1 category; Step 3 ≥ 1 language
- On Done → `authRepo.markOnboardingComplete()` → AuthState flips to Ready → MainActivity re-keys NavHost
- Skip CTA = accept current defaults + submit (works because Step 1 default is `TestOthers`, Step 2/3 require chips; if user hasn't completed step 2/3 → submit may fail validation? V1 fake never fails)

## Related
- spec_ref: [`_specs/onboarding_ux.md`](../../_specs/onboarding_ux.md)
- spec_ref: [`_specs/wireframes.md`](../../_specs/wireframes.md) §2
- depends on: `:core:common`, `:core:designsystem`, `:core:ui`, `:core:domain` (AuthRepository), `:core:navigation`
- dependents: `:app` only via `onboardingGraph()`
