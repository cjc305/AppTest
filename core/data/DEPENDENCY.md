# :core:data — Dependencies

> Module DAG + 替換 + 測試。

## I depend on

| Dep | Why |
|---|---|
| `:core:common` (api) | `AppResult` / `AppError` |
| `:core:domain` (api) | `Repository` marker / `TokenProvider` interface (我們實作它) |
| `:core:network` (implementation) | 預留（未來放 shared aggregate repo 會用 Retrofit api） |
| `:core:database` (implementation) | 預留（未來會讀 Room cache） |
| `androidx.datastore:datastore-preferences` | Preferences DataStore |
| `org.jetbrains.kotlinx:kotlinx-coroutines-android` | DataStore + Hilt 用 |
| `com.google.dagger:hilt-android` | DI |

**No** Retrofit / Room 直接依賴（透過 `:core:network` / `:core:database` 拿）。

## Modules depending on me

| Module | Uses what |
|---|---|
| `:feature:auth` (R-043) | `SessionStore.save/clear`（real impl）→ V1 FakeAuthRepository 不用此 module |
| `:app` | `SessionStore.session`（observe → AuthState） |
| 未來 `:feature:profile` | 若搬 ProfileRepository 過來 |
| `:core:network` 透過 `:core:domain.TokenProvider` interface — 不直接依賴本 module（避免 cycle） |

## Cycle 注意

```
:core:data → :core:network    (build dep)
:core:network ⇏ :core:data    (絕對禁止)
:core:network → :core:domain  (TokenProvider interface)
:core:data → :core:domain     (also)
```

`:core:network` 拿 `TokenProvider` interface 來自 `:core:domain`；production binding 由 `:core:data` 提供。`:core:network` 永遠不可 import `:core:data`。

## How to replace

### 改用其他 KV 儲存（如 EncryptedSharedPreferences）
1. 新 impl 實作 `SessionStore` + `TokenProvider`
2. `SessionBindingsModule` 改 `@Binds` 到新 impl
3. 一條測試覆寫 round-trip 確認 schema
4. 移除舊 DataStore 檔案的 migration（檔名 `auth_session.preferences_pb`）

### 加密 jwt
1. `DataStoreSessionStore` 改用 `androidx.security.crypto.EncryptedSharedPreferences`（或 Tink AEAD 寫到 DataStore raw bytes）
2. Key 走 Android Keystore alias
3. **不**為了加密引入額外抽象 — 直接改 impl 即可

### 改 cross-feature aggregate repo（如把 ProfileRepository 從 feature 搬過來）
1. 新介面在 `:core:domain/profile/ProfileRepository.kt`
2. impl 在 `:core:data/profile/`
3. Module: `@Binds` 在新 ProfileDataModule
4. Feature 端把原 impl 移除 + 改注新 interface

## How to test

| Test type | Tool | Scope |
|---|---|---|
| **DataStoreSessionStore** | Robolectric + JUnit | save → emit → clear → emit null；isExpired 邏輯；token() 對 expired session 回 null |
| **AuthSession** | Pure JUnit | `isExpired(now)` 對 now < / == / > exp 三條 |
| **SessionModule** | Hilt @HiltAndroidTest | 確認 SessionStore 與 TokenProvider 注的是同一 instance（identity check） |

DataStore 測試用 `PreferenceDataStoreFactory.create(produceFile = { tempFile })` 注 in-memory 檔案。

## File budget

| File | Lines | Notes |
|---|---:|---|
| `AuthSession.kt` | ~20 | data + isExpired |
| `SessionStore.kt` | ~25 | interface only |
| `DataStoreSessionStore.kt` | ~55 | impl + keys + companion |
| `di/SessionModule.kt` | ~50 | 2 modules (bindings + provides) |
| 4 docs | ≤ ~100 each | |

Source code ≤ 200/檔 ✓。

## Deferred / not in scope

- **Refresh token rotation** — `:feature:auth/RealAuthRepository` 負責偵測 401 + call refresh + write back。本 module 不放 refresh 邏輯（職責是儲存而非 auth flow）。
- **Multi-account 支援** — V3 才考慮；目前單一 jwt entry。
- **Migration from key-value to typed Proto DataStore** — 預設不換；Proto 對單表 5 fields 過度設計。
- **Biometric-gated unlock** — V2 安全功能；只要把 `dataStore.data` 包一層 BiometricPrompt gate 即可，不破現有 API。
