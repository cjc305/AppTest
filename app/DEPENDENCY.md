# :app — Dependencies

> Module DAG + 替換 + 測試。

## I depend on

### Internal modules (all 8 cores + 8 features)

| Module | Why |
|---|---|
| `:core:common` | `AuthState`（observe driver） |
| `:core:designsystem` | `AppTheme` / `AppL10n` / icons / spacing |
| `:core:ui` | `AppBottomBar` / `AppBottomDest` / `AppButton` / `ScreenScaffold` (via templates) |
| `:core:domain` | `AuthRepository` (Hilt-injected) |
| `:core:data` | build dep — `SessionModule` 注 Hilt graph |
| `:core:network` | build dep — `NetworkModule` 注 Hilt graph |
| `:core:database` | build dep — `DatabaseModule` 注 Hilt graph |
| `:core:navigation` | `AppDestination` / `AppDeepLink` / `startDestinationFor` |
| `:feature:auth` ... `:feature:inbox` | 全部 8 個 feature graph |

### External libraries

| Library | Why |
|---|---|
| `androidx.activity:activity-compose` | `ComponentActivity` + `setContent` + `enableEdgeToEdge` |
| `androidx.lifecycle.*` | runtime + viewmodel-compose（feature VM injection） |
| `androidx.core:core-splashscreen` | declared but unused yet (V1 defers splash) |
| `androidx.compose.bom` | Compose UI / Material3 / WindowSizeClass |
| `androidx.navigation:navigation-compose` | NavHost + `composable<T>` |
| `androidx.hilt:hilt-navigation-compose` | `hiltViewModel<T>()` |
| `com.google.dagger:hilt-android` | DI runtime |
| `ksp` × `hilt-compiler` | Hilt code gen |

## Modules depending on me

無 — leaf application module。

## How to replace

### 加新 feature module
1. 建模組 `feature/<name>/`（複製 `:feature:home` 結構）
2. `settings.gradle.kts` `include(":feature:<name>")`
3. `app/build.gradle.kts` `dependencies` 加 `implementation(project(":feature:<name>"))`
4. Feature 提供 `NavGraphBuilder.<name>Graph(...)`
5. `AppNavHost` NavHost block 內 `xxxGraph(...)`
6. 若是 top-level tab：`MainTopLevelDestination` enum 加 entry + `AppDestination` 新 case

### 替換 bottom-bar 為 NavigationRail（tablet）
1. 內 `AppNavHost` `windowSizeClass.widthSizeClass` 判定 `Compact` / `Medium+`
2. `Compact` 用 `bottomBar = { AppBottomBar(...) }`
3. `Medium+` 用 `NavigationRail` 包 NavHost
4. 一條 Compose UI test 跑兩種 width

### 加入 Splash screen (androidx-splashscreen)
1. `themes.xml` 加 `Theme.AppTest.Starting` extends `Theme.SplashScreen`
2. `Manifest`: MainActivity `android:theme=@style/Theme.AppTest.Starting`
3. `MainActivity.onCreate` 開頭：`installSplashScreen().setKeepOnScreenCondition { stillLoading }`
4. Style 設 `windowSplashScreenAnimatedIcon`

### 替換 Application class（加 WorkManager init）
1. `Configuration.Provider` impl on `AppTestApplication`
2. `androidx-hilt-work` 已在 libs — 注 `HiltWorkerFactory`
3. `onCreate { super.onCreate(); WorkManager.initialize(this, config) }`

## How to test

| Test type | Tool | Scope |
|---|---|---|
| **Smoke (instrumented)** | UiAutomator + HiltAndroidTest | App 啟動不 crash；MainActivity 渲染至少 1 個 feature |
| **NavHost integration** | Compose UI test + TestNavHostController | navigate(AppDestination.X) → currentBackStackEntry route 命中 |
| **Bottom-bar selection sync** | Compose UI test | 點 4 個 tab → 驗 bottom-bar selection 變化正確 |
| **Deep-link binding** | Compose UI test + Intent inject | `apptest://app/abc` → AppDetail composable with appId="abc" |
| **Share intent fires** | Robolectric + Shadows | `shareInvite("...")` → `ShadowApplication.nextStartedActivity` 含 ACTION_SEND |
| **Sign-out clears stack** | Compose UI test | trigger signOut → 後續 backStack 僅 SignIn |
| **Build smoke** | CI `./gradlew :app:assembleDebug` | Hilt 生成 OK，resource OK |

無 unit test —  `:app` 純 wiring，重心在 integration。

## Initializer registry (planned)

| Initializer | Lib | When | Owner task |
|---|---|---|---|
| Hilt root | `@HiltAndroidApp` | ✅ done (R-010) | — |
| Firebase auto-init | manifest ContentProvider | 加 `google-services.json` 後 | APT-V1-R-042 |
| Crashlytics | `FirebaseCrashlytics.getInstance()` | post-Firebase | APT-X-004 |
| WorkManager 自訂 config | `Configuration.Provider` on Application | R-041 | APT-V1-R-041 |
| LeakCanary (debug only) | `debugImplementation` | post-feature ship | APT-X-008 (new) |

## File budget

| File | Lines | Notes |
|---|---:|---|
| `build.gradle.kts` | ~80 | core + features + Compose + Hilt |
| `AndroidManifest.xml` | ~55 | 3 perms + 1 Activity + 3 intent-filters |
| `AppTestApplication.kt` | ~10 | Hilt marker |
| `MainActivity.kt` | ~60 | edge-to-edge + observe + 2 callbacks |
| `nav/AppNavHost.kt` | ~180 | Scaffold + NavHost + redirects + Settings |
| `nav/MainTopLevelDestination.kt` | ~80 | enum + label fn |
| `res/values/strings.xml` | ~15 | OS-level surfaces only |
| `res/values-zh-rTW/strings.xml` | ~10 | localized |
| `res/values/themes.xml` | ~16 | Theme.AppTest shell |

每檔 ≤ 200 行 ✓（gated by `./gradlew enforceFileLineLimit`）。

## Deferred (next companion tasks)

- Splash screen integration — `core-splashscreen` declared but unused
- App icon — 目前用 system default
- ProGuard / R8 rules — `isMinifyEnabled = false` 直到 shipping milestone
- Release signing config — V1 dev: assembleRelease 產 unsigned AAB（per `cicd.md §8`）
- Tablet NavigationRail — windowSizeClass branch
