# :feature:appdetail

> Tester journey 的 conversion 點：看 App 詳情、看「為什麼配對到我」、決定要不要 Join。
> V1: FakeAppDetailRepository 給 mock；Join CTA 開 Play Store closed-test opt-in URL via Intent。

## Use it when

- `:app/nav/AppNavHost` 想 expose AppDetail destination — 呼叫 `appDetailGraph(onNavigateUp)`
- 替換 Fake 為真 backend — 改 `data/di/AppDetailDataModule.kt` 內 `@Binds`
- 深層連結（push payload / outside intent）回到 App 詳情 — 預設綁定 `apptest://app/{id}` 與 `https://apptest.dev/app/{id}`，無需 caller 額外設定

## Don't use it for

- App 編輯 / 建立 — 那是 `:feature:myapps`
- 真正的「Join → 14d 測試」business logic — V1 只 open Play Store；TestRequest 建立 + heartbeat 等 APT-V1-R-040 接上
- App 列表 — Home / MyApps 各有專屬視圖

## Key concepts

- **Single screen with three-state matrix** (Loading / Error / Loaded) + sticky bottom CTA
- **`AppDetailHeader`** (sub-comp): icon placeholder + name + category + owner tier
- **`RequirementsSection`** (sub-comp): 14d × 12 testers 等需求 + currentTesters 透明度
- **`ExplainabilityCard`** (sub-comp): top-3 match reasons per `ai_matchmaking.md §10`；不渲染空集合
- **Side effect (open Play Store) via Channel event** — VM 不引入 Context；Route observe channel + LocalContext.startActivity
- **Deep link bindings owned by `appDetailGraph`** — 不在 :app level 重複設定

## Quick example

```kotlin
// :app/nav/AppNavHost
appDetailGraph(
    onNavigateUp = { navController.popBackStack() },
)
// custom deepLinks 可覆寫；default 已綁好 apptest:// + apptest.dev
```

## V1 mock

`FakeAppDetailRepository.mockFor(appId)`:
- NoteFlash / Productivity / by Alex (Gold tier)
- 14 days × 12 testers, 5/12 current, daily 10 min
- 3 match reasons (category / tier / timezone)
- Play opt-in URL → real-ish https://play.google.com/apps/testing/com.example.noteflash

Empty appId → `AppError.NotFound("app")` to exercise error path.

## Related

- spec_ref: [`_specs/wireframes.md`](../../_specs/wireframes.md) §4 — App Detail visual spec
- spec_ref: [`_specs/ai_matchmaking.md`](../../_specs/ai_matchmaking.md) §10 APT-P-010 — explainability requirement
- spec_ref: [`_specs/product_architecture.md`](../../_specs/product_architecture.md) §5 #4 — anonymity floor (owner display tier only)
- depends on: `:core:common`, `:core:designsystem`, `:core:ui`, `:core:domain`, `:core:navigation`
- dependents: `:app` only
- 完整 deps + 測試見 [`DEPENDENCY.md`](DEPENDENCY.md)
