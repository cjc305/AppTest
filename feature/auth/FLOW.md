# :feature:auth — Flow

## Flow 1: Google sign-in path

```mermaid
sequenceDiagram
    actor U
    participant S as SignInScreen
    participant VM as SignInViewModel
    participant R as AuthRepository (Fake)
    participant MA as MainActivity
    participant NH as NavHost

    U->>S: tap "Continue with Google"
    S->>VM: onGoogleClick
    VM->>VM: state = Working
    VM->>R: signInWithGoogle 600ms
    R->>R: state.value = NeedsOnboarding
    R-->>MA: StateFlow emit NeedsOnboarding
    MA->>MA: remember authState → startDestinationFor → OnboardingRoot
    MA->>NH: re-key NavHost
    NH-->>U: Onboarding step 1 visible
```

## Flow 2: Email magic link path

```mermaid
sequenceDiagram
    actor U
    participant S as SignInScreen
    participant VM as SignInViewModel
    participant R as AuthRepository

    U->>S: tap "Sign in with email"
    S->>VM: onEmailModeClick → state = EnteringEmail empty
    U->>S: type "alice@example.com"
    S->>VM: onEmailChange
    U->>S: tap "Send magic link"
    S->>VM: onSubmitEmail
    VM->>VM: state = Working
    VM->>R: requestMagicLink email
    R-->>VM: Success Unit
    VM->>VM: state = MagicLinkSent email
    Note over S: user sees "✓ Magic link sent" + Change email
    U->>U: opens mail, clicks link → apptest://verify/{email} (V1 mock)
    Note over MA: Deep link → AppDestination.EmailVerify
```

## Flow 3: EmailVerify (deep-link arrival)

```mermaid
flowchart LR
    DL[Deep link apptest://verify/email] --> NH[NavHost composable EmailVerify]
    NH --> R[EmailVerifyRoute]
    R --> VM[EmailVerifyViewModel]
    VM --> AR[authRepo.verifyMagicLink token=email]
    AR --> SF[state.value = NeedsOnboarding]
    SF -.->|StateFlow| MA[MainActivity re-keys NavHost]
    MA --> OB[OnboardingRoot]
    VM --> SU[state = Succeeded]
    SU --> R2[UI shows "Signed in! Loading…"]
```

## Flow 4: AuthState lifecycle (full)

```mermaid
stateDiagram-v2
    SignedOut --> NeedsOnboarding: signInWithGoogle or verifyMagicLink success
    NeedsOnboarding --> Ready: markOnboardingComplete (called by :feature:onboarding)
    Ready --> SignedOut: signOut (called by :feature:profile Settings)
    SignedOut --> SignedOut: requestMagicLink (sent confirmation, no state change)
    NeedsOnboarding --> SignedOut: signOut from onboarding (skip path)
```
