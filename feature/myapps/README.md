# :feature:myapps

> Dev-side：list 我擁有的 Apps + Create/Edit form（含 Play opt-in URL live 驗證）。
> V1: in-memory FakeMyAppsRepository（process 內 mutate；殺掉 app 重啟即重置）。

## Use it when

- `:app/nav/AppNavHost` 想 expose MyApps 與 AppEditor — 呼叫 `myAppsGraph(onNavigateToEditor, onNavigateUp)`
- 替換 Fake 為真 backend — 改 `data/di/MyAppsDataModule.kt` 內 `@Binds`

## Don't use it for

- App detail（看 / Join 流程）— 那是 `:feature:appdetail`
- 統計報表（stats view）— V1 deferred，未來開 `:feature:myapps:stats` 子模組
- Icon 上傳邏輯 — V1 form 沒做（V2 polish）

## Key concepts

- **2 個 destinations** by `AppDestination.MyApps` + `AppDestination.AppEditor(appId?)`
- **AppDraft** 是 create+edit 共用的 form model（id=null 表 create）
- **PlayOptInUrlValidator** 是 pure object（不是 UseCase），給 ViewModel 每 keystroke 驗
- **SaveAppUseCase** 在儲存前做 server-side-equivalent 完整驗證（不信 UI）
- **List 用 Flow 觀察**：Editor save 寫 fake repo → list 自動 refresh（無 manual reload）

## Quick example

```kotlin
// :app/nav/AppNavHost
myAppsGraph(
    onNavigateToEditor = { appId -> navController.navigate(AppDestination.AppEditor(appId)) },
    onNavigateUp = { navController.popBackStack() },
)
```

## V1 stub data

`FakeMyAppsRepository.seed()` 給 2 個 App（MyApp1 8/12、MyApp2 0/12）。Editor save 進 in-memory state，list 同步顯示。

## Related

- spec_ref: [`_specs/wireframes_dev.md`](../../_specs/wireframes_dev.md) §1-2 — list + editor visual specs
- spec_ref: [`_specs/database_schema.md`](../../_specs/database_schema.md) §3 — apps entity
- depends on: `:core:common`, `:core:designsystem`, `:core:ui`, `:core:domain`, `:core:navigation`
- dependents: `:app` only (via `myAppsGraph`)
- 完整 deps + 測試見 [`DEPENDENCY.md`](DEPENDENCY.md)
