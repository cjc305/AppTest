# :feature:onboarding — Dependencies

## I depend on
| Module | Why |
|---|---|
| `:core:common` | AppError, AuthState (transitive via AuthRepository) |
| `:core:designsystem` | tokens + AppText/AppVSpacer |
| `:core:ui` | ScreenScaffold, AppTopBar, AppButton, AppFilterChip, AppProgressBar |
| `:core:domain` | `AuthRepository` interface (for markOnboardingComplete) |
| `:core:navigation` | AppDestination.OnboardingRoot |

## Modules depending on me
`:app` only via `onboardingGraph()`.

## How to extend (V2 polish)
- Welcome moment Composable as Step 0 (before progress bar appears)
- Done card with countdown to next match batch
- Permission ask for POST_NOTIFICATIONS (Android 13+) on Done
- Persist `OnboardingDraft` to `:feature:profile` ProfileRepository so categories drive matchmaking

## How to test
| Test | Tool | Scope |
|---|---|---|
| Unit VM | Turbine | step transitions; canProceed gating; submit triggers markOnboardingComplete |
| Compose UI | createComposeRule | each step renders required controls; Skip hits submit |

## File budget
- OnboardingScreen ~175 (3 steps + bottom actions inline)
- OnboardingViewModel ~65
- 其餘 < 30

每檔 ≤ 200 ✓
