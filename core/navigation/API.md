# :core:navigation — Public API

> 對外可 import。Internal 不列。

## Types

### `AppDestination` (sealed interface)
所有跨 feature 公開的 nav destination。13 entries:

| Entry | Args | Purpose |
|---|---|---|
| `AuthRoot` | — | sign-in subgraph 入口 |
| `OnboardingRoot` | — | 首次設定 subgraph 入口 |
| `MainRoot` | — | bottom-bar 主畫面 subgraph 入口 |
| `Home` | — | 配對 feed |
| `MyApps` | — | 開發者自己 Apps 列表 |
| `Testing` | — | 我正在測 / 已完成 dashboard |
| `Profile` | — | reputation / credits / history |
| `AppDetail` | `appId: String` | 單一 App 詳情頁 |
| `AppEditor` | `appId: String? = null` | 建立 (null) 或編輯 |
| `Inbox` | — | 通知 / 配對歷史 |
| `Settings` | — | locale / sign-out |
| `SignIn` | — | sign-in screen |
| `EmailVerify` | `email: String` | magic-link confirm |

每個皆 `@Serializable`。`data object` for no-arg；`data class` for args。

### `AuthState` (enum)
```kotlin
enum class AuthState { SignedOut, NeedsOnboarding, Ready }
```
路由態，非 domain 態。`:feature:auth` 寫，`:app` 讀。

## Functions

### `startDestinationFor(state: AuthState): AppDestination`
Pure mapping function. SignedOut→AuthRoot / NeedsOnboarding→OnboardingRoot / Ready→MainRoot.
- Pure: yes
- Throws: never

## Object: `AppDeepLink`

URI scheme 常數 + builder + parser。

### Constants
```kotlin
const val SCHEME_CUSTOM = "apptest"
const val SCHEME_WEB    = "https"
const val WEB_HOST      = "apptest.dev"
const val PATTERN_APP_DETAIL_CUSTOM = "apptest://app/{appId}"
const val PATTERN_APP_DETAIL_WEB    = "https://apptest.dev/app/{appId}"
const val PATTERN_TEST_DETAIL       = "apptest://test/{testId}"
const val PATTERN_INVITE            = "apptest://invite"
```

### Builders (returns URI strings)
```kotlin
fun appDetail(appId: String): String
fun appDetailWeb(appId: String): String
fun testDetail(testId: String): String
fun inviteWithRef(ref: String): String
```

### Parser
```kotlin
fun parse(uri: Uri): AppDestination?      // null when no pattern matches
fun extractReferralCode(uri: Uri): String?
```
Use for manual URI handling (push payload). NavHost-bound deep-links use `navDeepLink { uriPattern = AppDeepLink.PATTERN_* }` directly.

## NOT public (internal)

無。本模組純 public contract — 沒有 internal types。
