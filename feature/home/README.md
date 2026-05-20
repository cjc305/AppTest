# :feature:home

> 第一個 real feature。Greeting + 今日新配對 + 進行中測試 + 自己 Apps 四段 LazyColumn。
> V1: 後端走 [`FakeHomeRepository`](src/main/kotlin/com/apptest/feature/home/data/FakeHomeRepository.kt)。

## Use it when

- 你在 `:app/nav/AppNavHost` 想 expose Home destination — 呼叫 `homeGraph(onAppClick)` 即可
- 你要替換 Fake 為真 backend — 改 `data/di/HomeDataModule.kt` 內的 `@Binds` 指向新 impl

## Don't use it for

- 任何不屬於 Home screen 內部的事 — 別把 logic 從別處塞進來
- 直接 import `:feature:home.*` 從其他 feature — 跨 feature 跳轉走 `:core:navigation` 的 `AppDestination`
- 改 tier 顏色 / spacing / typography — 那是 `:core:designsystem` 的事

## Key concepts (3 層 Clean Architecture)

- **`ui/`** (internal): `HomeRoute`（注入 VM）/ `HomeScreen`（stateless）/ `HomeViewModel`（`@HiltViewModel`）/ `HomeUiState`（sealed: Loading/Error/Empty/Loaded）
- **`domain/`** (internal): `GetHomeDataUseCase`（NoParam，套用 dispatchers.io）+ `model/HomeModels.kt`（HomeUser / MatchedApp / ActiveTest / OwnedApp / HomeData）
- **`data/`** (internal): `HomeRepository` interface + `FakeHomeRepository` impl + `HomeDataModule`（Hilt `@Binds`）
- **`nav/`** (public): `fun NavGraphBuilder.homeGraph(onAppClick)` — 唯一對外 API

## Quick example (consumer side)

```kotlin
// :app/nav/AppNavHost.kt
NavHost(navController, startDestination) {
    // ...
    homeGraph(
        onAppClick = { appId -> navController.navigate(AppDestination.AppDetail(appId)) }
    )
    // ...
}
```

任何 `Route` 內回呼，都把 navigation 決策外推 — feature 只負責「使用者要去某 App」，不關心 NavController 是哪個。

## Stub data (V1 demo)

`FakeHomeRepository` 回傳 deterministic mock：
- User: Alice, Silver tier, 4 credits
- New match: NoteFlash · Productivity
- Active tests: 3 個（一個 ping overdue 觸發 warning）
- My apps: 2 個（一個 0/12 一個 8/12）

改 mock 內容 → 直接編輯 `FakeHomeRepository.MOCK_HOME_DATA`。

## Related

- spec_ref: [`_specs/wireframes.md`](../../_specs/wireframes.md) §3 Home — visual spec
- spec_ref: [`_specs/database_schema.md`](../../_specs/database_schema.md) — entity field semantics
- depends on: `:core:common`, `:core:designsystem`, `:core:ui`, `:core:domain`, `:core:navigation`
- dependents: `:app` only (`homeGraph` consumed via NavHost)
- 完整依賴 / 替換 / 測試見 [`DEPENDENCY.md`](DEPENDENCY.md)
