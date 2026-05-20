# AppTest — Atomic prompt templates

> Copy ONE template into a new Claude session. Replace `{{placeholders}}` with task-specific values.
> Each template assumes session already loaded `CLAUDE.md` + `_specs/_ai/README.md` + `_specs/mvp.md` (~15-30k tokens).
> **Never** include the whole spec — point to spec sections by anchor (e.g. `_specs/mvp.md#5`).

---

## impl-core-module

```
請 scaffold `:core:{{module_name}}` 模組，遵守 CLAUDE.md hard rules。

Inputs:
- task id: APT-R-{{NNN}}
- spec: _specs/mvp.md#{{section}}
- 依賴: {{list of :core:* it may depend on, or 'none'}}
- 不可依賴: 任何 :feature:* (core 永遠不依賴 feature)

Deliverables:
1. core/{{module_name}}/build.gradle.kts (android library, namespace com.apptest.core.{{module_name}})
2. core/{{module_name}}/src/main/AndroidManifest.xml (空 manifest, 若無需求)
3. com.apptest.core.{{module_name}} 包下：核心 API（interface 優先）+ default impl
4. core/{{module_name}}/README.md (做什麼)
5. core/{{module_name}}/API.md (公開介面清單)
6. core/{{module_name}}/FLOW.md (Mermaid 主要流程)
7. core/{{module_name}}/DEPENDENCY.md (依賴誰、誰依賴我、如何替換)

驗收：
- 每檔 ≤ 200 行
- 無循環依賴（grep -r "import com.apptest.feature" core/{{module_name}}/ 應為空）
- ./gradlew :core:{{module_name}}:assembleDebug 通過
```

---

## impl-feature-module

```
請 scaffold `:feature:{{feature_name}}` 模組，遵守 Clean Architecture + Atomic Design。

Inputs:
- task id: APT-R-{{NNN}}
- spec / user journey: _specs/mvp.md#{{section}}
- 用到的 use cases: {{list, or 'TBD per spec'}}
- 用到的 entities: {{list, refer mvp.md#5}}

Deliverables (3 layer split):
1. feature/{{feature_name}}/build.gradle.kts — depends on :core:ui, :core:designsystem, :core:domain, :core:navigation, :core:common (+ :core:data only if owning repo)
2. data layer (if any new repo needed): com.apptest.feature.{{feature_name}}.data
   - Repository impl + Mapper + DTO（若無 backend 用 FakeRepository）
3. domain layer: com.apptest.feature.{{feature_name}}.domain
   - UseCase per business action (1 UseCase = 1 class, ≤ 50 lines)
4. ui layer: com.apptest.feature.{{feature_name}}.ui
   - {{Feature}}Screen.kt (Stateless Composable, takes state + callbacks)
   - {{Feature}}ViewModel.kt (Hilt @HiltViewModel, exposes StateFlow<{{Feature}}UiState>)
   - {{Feature}}UiState.kt (sealed/data class, immutable)
   - {{Feature}}Route.kt (Composable that resolves VM + State + wires callbacks)
   - 子 Composables 各自獨立檔案
5. nav graph fn: NavGraphBuilder.{{feature}}Graph(navController) in :core:navigation 或 feature 自己 expose
6. 4 份 docs: README / API / FLOW / DEPENDENCY

硬限制：
- UI Composable 不可直接呼叫 Repository（必須走 UseCase）
- ViewModel 不可有 Context / View / Composable import
- 每檔 ≤ 200 行；任一 Composable 超過 → 拆 sub-Composable
```

---

## impl-usecase

```
請實作 `{{UseCase name}}` 於 `:feature:{{feature}}` (或 `:core:domain` 若跨 feature)。

Spec: _specs/mvp.md#{{section}} ({{quote 1-line user need}})

要求：
- 純 Kotlin class, `class {{Name}}UseCase @Inject constructor(private val repo: {{Repo}})`
- `suspend operator fun invoke({{params}}): Result<{{ReturnType}}>` (用 :core:common 的 Result)
- 無 Android import（Context / View / Composable 都不行）
- 副作用全走 repo，不直接碰 IO / SharedPreferences / 時間源（時間請注入 Clock）
- ≤ 50 行；若超過先檢討是否 1 UseCase 做了 2 件事
- 附 unit test: feature/{{feature}}/src/test/.../...UseCaseTest.kt
  - 用 mockk 或 FakeRepo
  - cover happy path + 至少 1 個 error path
```

---

## impl-screen

```
請實作 `{{ScreenName}}Screen` 於 `:feature:{{feature}}`，符合 2026 H2 Material 3 Expressive。

Spec: _specs/mvp.md#{{section}}
ViewModel state shape: {{paste {{Feature}}UiState definition or 'TBD'}}

要求：
1. `{{ScreenName}}Screen(state, on{{Event}}: () -> Unit, …)` — Stateless Composable
2. State hoisting：所有 mutable state 在 ViewModel
3. Edge-to-edge: 使用 Scaffold + WindowInsets.systemBars
4. Dynamic Color: 顏色取 MaterialTheme.colorScheme.*，禁止 hard-code Color(0x…)
5. Motion: 進場 AnimatedVisibility(fadeIn() + slideIn()); list 滾動用 LazyColumn + animateItemPlacement()
6. 大螢幕: WindowSizeClass 判斷，≥ Medium 改 2-column / NavigationRail
7. Loading / Error / Empty 三態用 :core:ui 對應元件
8. Accessibility: 所有可點擊元素有 contentDescription
9. @Preview: 至少 3 個 (default / loading / error / empty)，搭配 @PreviewLightDark + @PreviewScreenSizes

驗收：
- 檔案 ≤ 200 行；若超過拆成 {{ScreenName}}Header / {{ScreenName}}Content / {{ScreenName}}FAB 等子 Composable
- 無 ViewModel / Repository / Hilt 注入 (那是 {{ScreenName}}Route 的事)
- ./gradlew :feature:{{feature}}:assembleDebug 通過
```

---

## impl-api-endpoint

```
請實作 `{{METHOD}} {{path}}` 後端端點 (per APT-API-{{NNN}})。

Spec: _specs/mvp.md#6
Backend: {{Firebase | Cloudflare Workers | Supabase | Ktor}} (per APT-A-001 決議)

要求：
1. Request DTO + Response DTO（兩者皆 immutable data class）
2. Validation: 拒 400 + `{ code, message, hint }` 包含欄位錯誤
3. Auth: 中介層驗 JWT (除 POST /auth/google)
4. Idempotency: heartbeat 類 endpoint 用 (testId, dateUTC) 為 unique key
5. Error envelope 一律 `{ data?, error?, meta? }` per spec §6
6. 寫整合測試（真 DB，不 mock — 用 SQLite testcontainers 或 D1 wrangler test）
7. 更新 manifest.yaml: APT-API-{{NNN}} status → likely_resolved + verify: <curl 指令>
```

---

## verify-task

```
請驗證 task APT-{{full_id}} 是否真的完成。

驗證步驟：
1. 找 task 的 `verify:` 欄位（若無，根據 task title 推斷可執行的驗證命令）
2. 跑該命令 (build / test / curl / grep)
3. 對照該 task 的「驗收標準」（通常在 PROMPTS.md 對應模板的「驗收」段）
4. 若全 PASS → manifest.yaml status 改 done + 補一行 verified_at: 2026-MM-DD
5. 若部分 PASS → status 留 likely_resolved + 寫 known_issues 條列
6. 若 FAIL → status 改 in_progress + 開新子 task 或回報 blocker
7. 永遠不要為了通過驗證而修改 verify 命令本身
```

---

## refactor-200line

```
檔案 `{{path/to/File.kt}}` 已超過 200 行 (現 {{N}} 行)，請拆分。

拆分原則：
1. 先列 file 內所有 top-level / nested 函式 + classes，標每個的 LoC
2. 找最大的單一責任邊界（通常是「外層 UI 結構 / 子區塊 / state mapping / event 處理」其中一個）
3. 拆成 ≤ 200 行的多個檔案，命名清楚（{{Feature}}Header.kt / {{Feature}}Content.kt / {{Feature}}State.kt 等）
4. import 全部修正，跑 `./gradlew :{{module}}:assembleDebug` 確認綠
5. 若 ViewModel 超過 200 行 → 通常是 UseCase 抽不夠細，回頭看是否該拆 UseCase 而非拆 VM
6. 拆完更新該模組的 API.md / FLOW.md（公開 API 形狀有改的話）
```
