# :core:network — Dependencies

> Module DAG + 替換 + 測試。

## I depend on

| Dep | Why |
|---|---|
| `:core:common` (implementation) | `AppError`（mapping 在 Repository 端用） |
| `:core:domain` (implementation) | `TokenProvider` interface |
| `com.squareup.retrofit2:retrofit` (api) | HTTP client |
| `com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter` (api) | DTO 轉換 |
| `com.squareup.okhttp3:okhttp` (api) | underlying client |
| `com.squareup.okhttp3:logging-interceptor` (api) | logging |
| `org.jetbrains.kotlinx:kotlinx-serialization-json` (api) | `AppJson` |
| `com.google.dagger:hilt-android` | DI |

`api` 對 Retrofit / OkHttp / serialization：feature 注 Retrofit 時直接拿到 ser/de + okhttp class，不必各自重複宣告。

**No** `:core:data` dep — TokenProvider 來自 `:core:domain` interface，production binding 由 `:core:data` 提供（避免 cycle）。

## Modules depending on me

| Module | Uses what |
|---|---|
| `:core:data` (build dep — reserved) | 預留未來 shared aggregate repo |
| 每個 `:feature:*` (R-043+ 後逐個替 Fake) | `@KtorApi` / `@SupabaseRest` Retrofit + `AppJson` |
| 也透過 transitive 拿到 retrofit / okhttp / kotlinx.serialization |

## How to replace

### Add an API endpoint
1. 在 feature `data/api/` 加 `@Serializable data class XxxDto(...)`
2. 在同包加 `interface XxxApi { @GET("...") suspend fun fetch(...): XxxDto }`
3. Hilt `@Provides` 從 `@KtorApi Retrofit` 或 `@SupabaseRest Retrofit` 注一個 `XxxApi`
4. Feature Repository inject `XxxApi`，包 `runCatchingApp { api.fetch() }`

### Swap HTTP client（如 Ktor client 取代 OkHttp）
1. 寫新 `KtorEngineModule` 提供 `HttpClient`
2. **保留 `Retrofit` provider 的 public signature** — 內部換成 `ktor-client` engine
3. 或：全砍重寫 feature `api` interface 為 Ktor `client.get<...>()` — 大 refactor，V1 不做

### 改 base URL（必做：R-043 接 Supabase 時）
1. `:app/build.gradle.kts` 加 `buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")`
2. `ApiConfig` 改讀 `com.apptest.app.BuildConfig.SUPABASE_URL`
3. `local.properties` 放 `supabase.url=...`（已 gitignore）
4. `NetworkModule` 不變

## How to test

| Test type | Tool | Scope |
|---|---|---|
| **AuthInterceptor 加 header** | mockk `TokenProvider` + `MockWebServer` | token=jwt → 看 recorded request 帶 Authorization；token=null → 無 header |
| **`AppJson` 配置** | pure JUnit | ignoreUnknownKeys / explicitNulls=false / coerceInputValues 行為 |
| **Retrofit 串接** | `:app` Hilt @HiltAndroidTest | 真注 + 對 MockWebServer 跑一輪 |

## File budget

| File | Lines | Notes |
|---|---:|---|
| `ApiConfig.kt` | ~25 | constants only |
| `AppJson.kt` | ~16 | single Json val |
| `interceptor/AuthInterceptor.kt` | ~33 | runBlocking + header attach |
| `di/NetworkModule.kt` | ~80 | 2 modules + 2 qualifiers + providers |

每檔 ≤ 200 ✓。

## Deferred / not in scope

- **TLS pinning** — V1 不做（管理成本）
- **HTTP/3 (QUIC)** — OkHttp 5.x 才支援，等升級
- **Per-feature timeout 覆寫** — 真有 long-running upload 才考慮（V2 圖片上傳 ≥ 30s 可特別給）
- **Offline queue** — V2；目前 feature 自處理 retry，無 global 佇列
- **Real Supabase BuildConfig URLs** — 開放至 R-043 配 credentials 一起接
