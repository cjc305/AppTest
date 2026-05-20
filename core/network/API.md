# :core:network — Public API

> 對外可注入的 Hilt providers。Internal impl 不列。

## `ApiConfig` (object, constants)

```kotlin
object ApiConfig {
    const val KTOR_BASE_URL: String          // https://api.apptest.dev/v1/
    const val SUPABASE_REST_BASE_URL: String // placeholder: replace at R-043
    const val SUPABASE_REALTIME_URL: String  // placeholder
    const val TIMEOUT_SECONDS: Long          // 20
}
```

Hardcoded V1。R-043 將透過 `:app/BuildConfig` + `local.properties` 注入真實 URL，本檔變成讀 BuildConfig 而非 const literal。

## `AppJson` (top-level val)

```kotlin
val AppJson: Json
```

Shared `kotlinx.serialization.Json` instance — 所有 DTO 用同一份規則。Feature 不要自己 `Json { ... }`。

## Hilt qualifiers

```kotlin
@Qualifier annotation class KtorApi
@Qualifier annotation class SupabaseRest
```

注入兩個不同 base URL 的 Retrofit instance。

## Provided injects (via `NetworkModule`)

| 注入點 | 提供 type | 注意 |
|---|---|---|
| `@KtorApi Retrofit` | `retrofit2.Retrofit` | Ktor service |
| `@SupabaseRest Retrofit` | `retrofit2.Retrofit` | Supabase PostgREST |
| `OkHttpClient` | `okhttp3.OkHttpClient` | 共用 — 若 feature 要自訂 interceptor，從這個 newBuilder 再加 |
| `HttpLoggingInterceptor` | `okhttp3.logging.HttpLoggingInterceptor` | BASIC level |
| `AuthInterceptor` | `okhttp3.Interceptor` (concrete class) | Hilt 從 SessionStore 拿 TokenProvider 注此 |

## `AuthInterceptor` (公開 class — provided as singleton)

```kotlin
@Singleton class AuthInterceptor @Inject constructor(
    private val tokenProvider: TokenProvider,
) : Interceptor
```

- `tokenProvider.token()` 走 `runBlocking`（OkHttp 同步合約）— DataStore 讀很快，可接受
- token == null → 不加 header（匿名 request）— Sign-in / 註冊端點允許這條
- 401 重試 / refresh：**不** 在這做；由 `:feature:auth` 監聽後 call SessionStore.save 寫入新 token

## NOT public (internal)

- `NetworkModule` / 內部 helpers — Hilt module，不可被其他 module 引用名稱

## Test surface

| 想測 | 在哪測 |
|---|---|
| AuthInterceptor 加 / 不加 header | `:core:network` 自己（mockk `TokenProvider` + `MockWebServer`） |
| 序列化 round-trip 一致 | 個別 feature 的 DTO test |
| Retrofit base URL 對 | Hilt @HiltAndroidTest in `:app` |
