# :feature:auth — Dependencies

## I depend on
| Module | Why |
|---|---|
| `:core:common` | AppResult/AppError/AuthState enum |
| `:core:designsystem` | tokens + AppText/AppVSpacer |
| `:core:ui` | ScreenScaffold + AppButton + AppLoadingState |
| `:core:domain` | `AuthRepository` interface + `Repository` marker |
| `:core:navigation` | AppDestination.SignIn / EmailVerify (with email arg) |

## Modules depending on me (Hilt binding consumers)
- `:app` via Hilt-resolved AuthRepository (MainActivity inject)
- `:feature:onboarding` via Hilt-resolved AuthRepository (markOnboardingComplete)
- `:feature:profile` later (sign-out from Settings)

These don't `import :feature:auth.*` directly — only `:core:domain/auth/AuthRepository`.

## How to replace Fake → real (Supabase)
1. `class SupabaseAuthRepository @Inject constructor(client: SupabaseClient, ...) : AuthRepository`
2. `data/di/AuthDataModule` 改 @Binds 指向 `SupabaseAuthRepository`
3. SignIn UI 改: Google button 接 Credential Manager + Google ID; Email submit 接 `supabase.auth.signInWith(OTP)`
4. Verify: subscribe `supabase.auth.sessionStatus`; or use deep-link `?token=` arg
5. SignedOut state: clear DataStore JWT cache

## How to test
| Test | Tool |
|---|---|
| Unit FakeAuthRepository | JUnit + Truth → 5 methods state transitions |
| Unit SignInViewModel | Turbine → Idle → Working → MagicLinkSent flow |
| Compose UI SignInScreen | createComposeRule per UiState case |
| Integration EmailVerify deep link | NavHost test with Intent(VIEW, apptest://verify/...) |

## File budget
- FakeAuthRepository ~60
- SignInScreen ~150
- SignInViewModel ~50
- EmailVerifyViewModel + Route ~70 combined
- 其餘 < 30

每檔 ≤ 200 ✓
