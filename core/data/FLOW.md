# :core:data — Internal Flow

> Auth session 讀寫 + token 注入路徑。

## Flow 1: Sign-in writes session

```mermaid
flowchart LR
    UI[SignInScreen] -->|magic link token| VM[SignInViewModel]
    VM -->|verifyMagicLink| AR[RealAuthRepository<br/>:feature:auth]
    AR -->|Supabase verifyOtp| SB[(Supabase Auth)]
    SB -->|accessToken+refresh+exp| AR
    AR -->|save AuthSession| SS[SessionStore impl<br/>DataStoreSessionStore]
    SS -->|edit preferences| DS[(DataStore preferences file)]
    SS -.emit.-> FLOW[session: Flow&lt;AuthSession?&gt;]
    FLOW --> MA[:app MainActivity<br/>maps to AuthState]
```

寫者唯一 = `:feature:auth/RealAuthRepository`（V1 = FakeAuthRepository 不會走這條，留作 R-043 切換用）。

## Flow 2: Network request attaches token

```mermaid
flowchart LR
    UC[Any UseCase] -->|http call| RT[Retrofit api]
    RT --> OK[OkHttp client]
    OK --> INT[AuthInterceptor :core:network]
    INT -->|tokenProvider.token| TP[TokenProvider impl<br/>DataStoreSessionStore]
    TP -->|session.firstOrNull| DS[(DataStore)]
    TP -->|null when expired| INT
    INT -->|Bearer hdr 或 略| REQ[wire request]
    REQ --> SRV[Ktor / Supabase REST]
```

Token expired (server clock skew tolerated)：
- 不 attach header → server 回 401
- `:feature:auth` 監聽 401 → 觸發 refresh flow（用 refreshToken 換新 jwt）→ 寫回 SessionStore → caller 重試一次

## Flow 3: Sign-out clears session

```mermaid
flowchart LR
    UI[Settings: Sign out] -->|onSignOut| MA[MainActivity.signOut]
    MA -->|launch viewmodelScope| AR[AuthRepository.signOut]
    AR -->|Supabase auth signOut| SB[(Supabase)]
    AR -->|clear| SS[SessionStore.clear]
    SS -->|edit { it.clear }| DS[(DataStore)]
    SS -.emit null.-> FLOW[session Flow → null]
    FLOW --> MA2[MainActivity re-evals AuthState → SignedOut]
    MA2 --> NV[Navigate to AuthRoot]
```

`SessionStore.clear()` 是 idempotent — 重複呼叫無副作用，這對 `:feature:auth` 監測 401 後一律 clear 友善。

## Flow 4: App cold start restoration

```mermaid
flowchart LR
    LAUNCH[App process start] --> HILT[Hilt builds SessionDataStore singleton]
    HILT --> READ[session Flow first emission]
    READ -->|有 stored session, not expired| AS1[AuthState.Ready 或 NeedsOnboarding]
    READ -->|無 / expired| AS2[AuthState.SignedOut]
    AS1 --> ND[Nav startDestination = MainRoot]
    AS2 --> NS[Nav startDestination = AuthRoot]
```

注意：cold start 時 `session.firstOrNull()` 還沒回來前，MainActivity 已用 `initialValue = AuthState.SignedOut` 顯示 sign-in 1 frame，下個 recomposition 才換 Ready。這對 UX 影響極小（< 100ms）；若要消除可在 MainActivity 加 splash 直到 first emission。

## Threading

| Op | Dispatcher | 由誰決定 |
|---|---|---|
| DataStore read / write | IO (DataStore internal) | DataStore 預設 |
| `session.firstOrNull()` | caller's dispatcher | caller 注 dispatcher |
| `save()` / `clear()` 內部 | IO | 同上 |
| Interceptor `tokenProvider.token()` | OkHttp dispatcher pool | OkHttp 預設 |

不需手動 `withContext(io)` — DataStore 已切 IO。
