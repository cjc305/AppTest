# :app — Public API

> Leaf module — 沒有真正「對外」消費者。本檔記錄對 Android 系統 + Hilt graph 的 surface。

## Application

### `AppTestApplication : Application`
- `@HiltAndroidApp` — Hilt SingletonComponent root
- onCreate 目前空。新 startup initializer 在這：
  - FCM notification channel 註冊（R-042）
  - Crashlytics enable（X-004）
  - WorkManager scheduling（R-041）
- Manifest: `android:name=".AppTestApplication"`

## Activity

### `MainActivity : ComponentActivity`
- `@AndroidEntryPoint`
- `@OptIn(ExperimentalMaterial3WindowSizeClassApi)` — `calculateWindowSizeClass(this)`
- `onCreate` 流程：
  1. `enableEdgeToEdge()`
  2. `setContent { AppTheme { ... } }`
  3. 觀察 `authRepo.state` 對應 `startDestinationFor(...)`
  4. Render `AppNavHost(startDestination, windowSizeClass, onShareInvite, onSignOut)`
- Activity-only side-effect helpers:
  - `shareInvite(uri: String)` — `Intent.createChooser(ACTION_SEND, text=uri)`
  - `signOut()` — `lifecycleScope.launch { authRepo.signOut() }`
- Manifest:
  - `exported="true"` (launcher)
  - `configChanges`: 全套處理（避免整 Activity recompose）
  - Deep-link intent-filters: `apptest://...` + `https://apptest.dev/...`（後者 `autoVerify=false` 直到 assetlinks.json 上線）

## Top-level NavHost

### `AppNavHost(startDestination, windowSizeClass, onShareInvite, onSignOut, modifier, navController)`

(in `nav/AppNavHost.kt`)

| Param | Type | Notes |
|---|---|---|
| `startDestination` | `AppDestination` | 由 `startDestinationFor(authState)` 決定 |
| `windowSizeClass` | `WindowSizeClass` | passed down for responsive routing |
| `onShareInvite` | `(String) -> Unit` | activity launches `ACTION_SEND` |
| `onSignOut` | `() -> Unit` | activity scope `authRepo.signOut()` |
| `modifier` | `Modifier` | default `Modifier` |
| `navController` | `NavHostController` | default `rememberNavController()` — 可注入測試 |

Outer Material3 `Scaffold` with `contentWindowInsets = WindowInsets(0)`，`bottomBar` 渲染 `AppBottomBar` 當 `currentBackStackEntry?.destination` 屬於 `MainTopLevelDestination`。

13 destinations 全 wired，無 stub（`Settings` 為 inline minimal composable）。

## Tab metadata

### `internal enum class MainTopLevelDestination`
(in `nav/MainTopLevelDestination.kt`)

4 entry: Home / MyApps / Testing / Profile。
- `id: String` — bottom-bar stable key
- `labelKey: TopLevelLabelKey` — 對應 EN/ZH-TW pair（暫時 `tabLabelFor()` 函式對應）
- `icon: ImageVector` — `Icons.Filled.*`（core icons only）
- `route: AppDestination` — navigate target
- `matches(NavDestination?): Boolean` — 同步 bottom-bar selection
- `companion fun fromDestination(NavDestination?): MainTopLevelDestination?` — reverse lookup

## Deep links wired

| URI | Destination |
|---|---|
| `apptest://app/{appId}` | `AppDestination.AppDetail(appId)` (navDeepLink in AppDetail composable) |
| `https://apptest.dev/app/{appId}` | 同上 (autoVerify=false until APT-OPS-001) |
| `apptest://test/{id}` | `AppDestination.Testing`（透過 `AppDeepLink.parse`） |
| `apptest://invite?ref=...` | `AppDestination.AuthRoot`（referral 屬性由 caller 解析） |
| Inbox 內部點擊 | `AppDeepLink.parse(Uri)` → `navController.navigate(...)` |

## Theme (XML shell)

### `Theme.AppTest` (in `res/values/themes.xml`)
- Parent: `android:Theme.Material.Light.NoActionBar`
- 純 shell — Compose 端 (`:core:designsystem/AppTheme`) 才是 dynamic color + tokens 真實出口
- 透明 status / navigation bar (`@android:color/transparent`)

## NOT public

`SignInRedirect` / `MainRootRedirect` / `SettingsStub` / `tabLabelFor` 為 `private`。
