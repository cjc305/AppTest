# :core:common — Dependencies

> Module DAG view + 替換策略 + 測試策略。

## I depend on

| Dep | Why |
|---|---|
| `org.jetbrains.kotlinx:kotlinx-coroutines-core` | `CoroutineDispatcher` for `DispatcherProvider` |

**僅此一個。** 刻意純 kotlin-jvm：no Android、no Compose、no DI、no Retrofit。Kotlin Multiplatform-ready（雖然 V1 沒打算上 KMP）。

```kotlin
// core/common/build.gradle.kts
plugins { alias(libs.plugins.kotlin.jvm) }
dependencies { api(libs.kotlinx.coroutines.core) }
```

`kotlin.jvm`（非 `android.library`）= 沒 manifest、沒 R class、沒 resource — compile/test 飛快。

## Modules depending on me

| Module | Uses what |
|---|---|
| `:core:domain` | `AppResult` / `AppError` / `DispatcherProvider` / `AuthState` |
| `:core:navigation` | `AuthState` |
| `:core:ui` | `ReputationTier` (AppTierBadge) / `AppStrings`（透過 designsystem 包裝） |
| `:core:designsystem` | `AppStrings`（CompositionLocal 包裝） |
| `:core:data` | `AppResult` / `AppError` / `runCatchingApp` |
| `:core:network` | `AppError`（HTTP / network 錯誤 mapping） |
| `:core:database` | `AppResult`（DAO 包裝邊界） |
| 每個 `:feature:*` | 基本全用：`AppResult` / `AppError` / `AppStrings` / `ReputationTier`（若顯示 tier） |
| `:app` | `AuthState`（讀 startDestination） |

幾乎是 universal dep — 跨層下游中樞。**任何 module 都不應該 fork** 自己的 Result / Error 型別。

## How to replace

### 加新 `AppError` subtype
1. 在 `AppError.kt` 加 `data class XXX(...) : AppError(...)`
2. 若有對應 throwable，更新 `AppError.fromThrowable` 的 `when`
3. 更新 `API.md` 表 + `FLOW.md` mapping cheatsheet
4. Caller 端按需 pattern-match

### 加新 `ReputationTier` 階
1. 加 enum case
2. 改 `fromScore` 邊界
3. `:core:designsystem/AppExtended.colors` 加對應 tier color
4. 確認 `_specs/reputation_system.md` 與此一致

### 替換 `AppResult` 為 Kotlin stdlib `Result`
不建議。`AppResult.Failure` 承載 `AppError`，stdlib `Result` 只能承載 `Throwable`，會強迫每個 caller 反序列化錯誤類型。`AppResult` 是有意保留的型別。

## How to test

| Test type | Tool | Scope |
|---|---|---|
| **Unit (Result)** | JUnit + Truth | `map` / `flatMap` / `onSuccess` / `onFailure` / `runCatchingApp` 對成功/失敗/cancellation 三種情境正確 |
| **Unit (Error)** | JUnit + Truth | `fromThrowable` 對 IOException 與其他 throwable 的分支正確 |
| **Unit (Tier)** | JUnit + Truth | `fromScore` 對 0/199/200/399/400/599/600/799/800/1000 邊界值正確 |
| **Unit (Dispatcher)** | JUnit | inject `UnconfinedTestDispatcher`，驗證 `withContext(io)` 被呼叫；測試本身不 own dispatcher provider 邏輯，由 UseCase / Repository test 驗證 |

純 JVM 跑 — `:core:common` 自己無 androidTest source set。

## File budget

| File | Lines | Notes |
|---|---:|---|
| `Result.kt` | ~46 | AppResult + extensions + runCatchingApp |
| `AppError.kt` | ~31 | 9 subtypes + AuthReason + fromThrowable |
| `DispatcherProvider.kt` | ~24 | interface + DefaultDispatcherProvider |
| `AuthState.kt` | ~16 | 3-case enum + KDoc |
| `ReputationTier.kt` | ~31 | 5-case enum + fromScore |
| `AppStrings.kt` | ~240 | i18n catalog (EN + ZH_TW) — 超 200 唯一例外，本質是 data table 而非邏輯 |

`AppStrings.kt` 是 hard rule（200 line）的合理例外：**純資料表**、無分支、無邏輯。若拆分反而傷可讀性。後續若超 400 行考慮拆 per-feature catalog。

## Deferred / not in scope

- **KMP 移植** — V1 不需要 iOS / web 共享；DispatcherProvider 已是 KMP-ready 結構，未來移植成本低
- **Localization fallback chain** — V1 binary EN / ZH_TW，缺翻譯 fallback 邏輯放 `:core:designsystem` 提供方
- **Time / Clock abstraction** — 目前 epoch 由 caller 自管；若有測試需求再加 `TimeProvider` 介面
