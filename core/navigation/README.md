# :core:navigation

> Cross-feature routing contract: type-safe `AppDestination` + `AuthState` gate + deep-link URI helpers.
> Compose Navigation 2.8+ 型別安全 destinations，no string routes anywhere.

## Use it when

- 任何 feature 需要跳到「其他 feature 的入口」（Home → AppDetail / MyApps → AppEditor 等）
- `:app/MainActivity` 設定 top-level NavHost 與 startDestination
- `:feature:auth` 暴露 `Flow<AuthState>` 給 `:app` 做 gate
- 收到 deep link URI（push payload / outside intent）需轉成 in-app destination

## Don't use it for

- Feature 內部的 sub-route（如 onboarding step 1 → step 2）— 那是 feature 自己 `nav/` 子包的事
- 業務狀態 (`AuthState` 是**路由**態，不是「我有沒有 valid session」這種 domain 態 — 兩者不同抽象層)
- Deep link 在 NavHost 內的綁定 — 用 Compose Nav `navDeepLink { ... }` API 直接吃本檔的 `PATTERN_*` 字串

## Key concepts

- **`AppDestination`** — sealed interface 含 13 個 `@Serializable` nested types。Top-level: AuthRoot / OnboardingRoot / MainRoot。Main 子: Home / MyApps / Testing / Profile。Pushed: AppDetail(id) / AppEditor(id?) / Inbox / Settings。Auth 子: SignIn / EmailVerify(email)。
- **`AuthState`** — 3-case enum (SignedOut / NeedsOnboarding / Ready)。配套 `startDestinationFor(state)` pure fn。
- **`AppDeepLink`** — URI scheme constants (`apptest://` + `https://apptest.dev`) + URL builders + `parse(uri)` 函式。

## Quick example

```kotlin
// :app/MainActivity
class MainActivity : ComponentActivity() {
    @Inject lateinit var authRepo: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                val nav = rememberNavController()
                val authState by authRepo.observeState().collectAsState(AuthState.SignedOut)
                val start = remember(authState) { startDestinationFor(authState) }

                NavHost(navController = nav, startDestination = start) {
                    navigation<AppDestination.AuthRoot>(startDestination = AppDestination.SignIn) {
                        authGraph(nav, onAuthenticated = { nav.navigate(AppDestination.OnboardingRoot) })
                    }
                    navigation<AppDestination.MainRoot>(startDestination = AppDestination.Home) {
                        homeGraph(nav)
                        // ... other feature graphs
                        composable<AppDestination.AppDetail>(
                            deepLinks = listOf(
                                navDeepLink { uriPattern = AppDeepLink.PATTERN_APP_DETAIL_CUSTOM },
                                navDeepLink { uriPattern = AppDeepLink.PATTERN_APP_DETAIL_WEB },
                            )
                        ) { entry ->
                            val args = entry.toRoute<AppDestination.AppDetail>()
                            AppDetailRoute(appId = args.appId, ...)
                        }
                    }
                }
            }
        }
    }
}
```

## Related

- spec_ref: [`_specs/navigation.md`](../../_specs/navigation.md) — full nav architecture
- spec_ref: [`_specs/feature_modules.md`](../../_specs/feature_modules.md) §3 — per-feature `nav/` convention
- depends on: `:core:common`, Compose Nav 2.8+, kotlinx-serialization-json
- dependents: `:app`, every `:feature:*`
- 完整依賴 / 替換 / 測試見 [`DEPENDENCY.md`](DEPENDENCY.md)
