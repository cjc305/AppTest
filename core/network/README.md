# :core:network

> HTTP client base: OkHttp + Retrofit + kotlinx.serialization Json + Auth interceptor.
> Two Retrofit instances pre-wired — Ktor matching service (`@KtorApi`) + Supabase PostgREST (`@SupabaseRest`).

## Use it when

- Feature 要呼叫 Ktor backend → 注 `@KtorApi Retrofit`，`.create(YourApi::class.java)`
- Feature 要呼叫 Supabase REST → 注 `@SupabaseRest Retrofit`
- 寫 DTO → 用 `@Serializable` + `AppJson` 序列化規則
- 自訂 OkHttp client（如加自己的 interceptor）→ 在 feature 的 di module 從 `OkHttpClient` 拿 base 再 `.newBuilder().addInterceptor(...).build()`

## Don't use it for

- WebSocket / Supabase Realtime — 走 Supabase Kotlin SDK，**不** 經 Retrofit（R-044 提供）
- 業務層 Repository — 介面屬於 `:core:domain` 或 feature；本 module 只提供 transport
- Error mapping — Retrofit response → `AppResult` 的轉換在 Repository 端（每 feature 可選擇要不要 wrap）

## Key concepts

- **`ApiConfig`** — base URL + timeout 常數。Supabase URL 目前是 placeholder（`YOUR_PROJECT.supabase.co`），R-043 一起替換成 BuildConfig 注入。
- **`AppJson`** — `kotlinx.serialization.Json` 設定（ignoreUnknownKeys / explicitNulls=false / coerceInputValues）。
- **`AuthInterceptor`** — 自 `TokenProvider`（impl in `:core:data`）取 token，加 Bearer header；無 token 時送匿名 request。
- **`@KtorApi` / `@SupabaseRest`** — Hilt qualifier，分辨兩個 Retrofit instance。
- **HttpLoggingInterceptor BASIC** — production-safe；不印 body / header（PII 風險）。

## Quick example

```kotlin
// In a feature's :feature:home/data
interface HomeApi {
    @GET("matches/me")
    suspend fun matches(): MatchResponseDto
}

@Module @InstallIn(SingletonComponent::class)
object HomeApiModule {
    @Provides @Singleton
    fun provideHomeApi(@KtorApi retrofit: Retrofit): HomeApi =
        retrofit.create(HomeApi::class.java)
}
```

## Related

- spec_ref: [`_specs/api_contracts.md`](../../_specs/api_contracts.md) §1 — envelope / auth / timeouts
- spec_ref: [`_specs/backend_architecture.md`](../../_specs/backend_architecture.md) §1
- depends on: `:core:common` / `:core:domain` (TokenProvider) / retrofit / okhttp / kotlinx.serialization / Hilt
- dependents: 任何要打 REST 的 feature（V1 多為 Fake；real-wired 從 R-043 開始）
- 完整 [`DEPENDENCY.md`](DEPENDENCY.md)
