# AppTest — Feature Module Catalog (V1)

> **Version:** 0.1 · **Last updated:** 2026-05-19 · **Owner:** TBD
> 每個 V1 feature 的責任 / 公開 API / 依賴 / 替換點。Module DAG 與 hard rules 見 `modularization.md`。

---

## 1. Catalog overview (V1, 8 features)

| Feature | 對應 user job-to-be-done | spec_ref |
|---|---|---|
| `:feature:auth` | 我要登入 (Google or Email) | mvp §4.1 |
| `:feature:onboarding` | 第一次設定我的偏好 | onboarding_ux.md |
| `:feature:home` | 看我今天有什麼配對 / 進度 | wireframes §3 |
| `:feature:myapps` | 管理我自己發出的 App | wireframes_dev §1-2 |
| `:feature:appdetail` | 決定要不要當這 App 的 tester | wireframes §4 |
| `:feature:testing` | 看我正在測什麼、進度多少 | wireframes §5 |
| `:feature:profile` | 看我的 reputation / credits / 歷史 | wireframes_dev §3 |
| `:feature:inbox` | 看通知 / 配對歷史（V1 簡化） | mvp §10 |

## 2. Per-feature contract

每個 feature 對外只 expose 3 件事：
1. **Nav destination** sealed class（在 feature 內 `nav/` 子包）
2. **NavGraphBuilder extension** `fun NavGraphBuilder.<feature>Graph(...)`
3. **Optional** : 結果回傳用 Kotlin Serializable type（type-safe nav results）

任何其他 internal class、ViewModel、UseCase、Composable 都 `internal`，不對外。

## 3. Internal structure (mandatory per feature)

```
feature/<name>/src/main/kotlin/com/apptest/feature/<name>/
├── nav/                        # public to other modules
│   ├── <Name>Destination.kt    # sealed class, Compose-Nav type-safe
│   └── <Name>NavGraph.kt       # fun NavGraphBuilder.<name>Graph(...)
├── ui/                         # internal
│   ├── <Name>Screen.kt         # Stateless Composable
│   ├── <Name>Route.kt          # Stateful wrapper, injects VM
│   ├── <Name>ViewModel.kt      # @HiltViewModel
│   ├── <Name>UiState.kt        # sealed/data class
│   └── components/             # sub-Composables (≤200 lines/file)
├── domain/                     # internal
│   ├── usecase/
│   │   └── <Verb><Noun>UseCase.kt   # 1 file = 1 UseCase, ≤50 lines
│   └── model/                  # feature-local domain types (cross-feature → :core:domain)
└── data/                       # internal, only if feature owns a repo
    ├── <Name>Repository.kt     # interface
    ├── <Name>RepositoryImpl.kt # @Inject impl
    ├── mapper/
    └── source/
        ├── remote/             # uses :core:network
        └── local/              # uses :core:database
```

如該 feature 不擁有 repo（純消費他 feature/core 的 repo），data/ 整個可省。

## 4. Forbidden imports (lint-checked)

| Inside | May NOT import |
|---|---|
| any `ui/*.kt` | `*.data.*`, `*.network.*`, `*.database.*` 直接 |
| any `ui/*ViewModel.kt` | `androidx.compose.*`, `android.view.*`, `android.content.Context` |
| any `domain/*.kt` | `android.*`, `androidx.*` (除 `androidx.annotation`) |
| any `nav/*.kt` | feature 內部 `ui.*` / `data.*` (nav 是 public surface, 應 thin) |

## 5. Per-feature responsibility table

### `:feature:auth`
- **Owns:** sign-in screen, magic-link verify, session bootstrap
- **Public:** `AuthDestination`, `authGraph(navController, onAuthenticated)`
- **Deps:** `:core:ui`, `:core:designsystem`, `:core:domain`, `:core:network` (auth endpoints), `:core:navigation`, `:core:common`
- **Repo owned:** `AuthRepository` (session, sign-in)
- **External:** Supabase Auth SDK, Google Identity Services

### `:feature:onboarding`
- **Owns:** 3-step wizard (intent, category, language) + welcome moment + permission ask
- **Public:** `OnboardingDestination`, `onboardingGraph(navController, onComplete)`
- **Deps:** `:core:ui`, `:core:designsystem`, `:core:domain`, `:core:navigation`, `:core:common`
- **Repo owned:** none (writes to `:feature:profile`'s repo via shared interface in `:core:domain`)

### `:feature:home`
- **Owns:** matched-feed, active-tests strip, your-apps strip, pull-to-refresh
- **Public:** `HomeDestination`, `homeGraph(navController)`
- **Deps:** all core + reads from MatchRepository, TestRequestRepository, AppRepository
- **Repo owned:** none (consumer)

### `:feature:myapps`
- **Owns:** list, create form (App Editor), edit, pause/resume, view stats
- **Public:** `MyAppsDestination` (List, Editor(appId?), Stats(appId))
- **Deps:** all core + AppRepository, CreditsRepository
- **Repo owned:** none (uses shared AppRepository in `:core:data` or `:feature:myapps:data`)

### `:feature:appdetail`
- **Owns:** App detail with screenshots, description, explainability, Join CTA
- **Public:** `AppDetailDestination(appId)`, deep-link `apptest://app/<id>`
- **Deps:** all core + AppRepository, TestRequestRepository
- **Repo owned:** none

### `:feature:testing`
- **Owns:** active/completed tests dashboard, manual heartbeat trigger, abandon flow
- **Public:** `TestingDestination`
- **Deps:** all core + TestRequestRepository, HeartbeatService (from `:core:data` or feature-local)
- **Repo owned:** `TestingRepository` (heartbeat-specific logic)

### `:feature:profile`
- **Owns:** profile screen + settings + proof cards + activity history + invite CTA
- **Public:** `ProfileDestination`
- **Deps:** all core + UserRepository, ReputationRepository, ProofRepository
- **Repo owned:** `ProfileRepository` (orchestrates above)

### `:feature:inbox`
- **Owns:** in-app notification list + match history (V1 minimal)
- **Public:** `InboxDestination`
- **Deps:** all core + NotificationRepository, MatchRepository
- **Repo owned:** `NotificationRepository` (FCM token storage + types_enabled)

## 6. Cross-feature communication patterns

不允許直接 import。能用的 3 種模式：

### a) Navigation contract
跳轉用 `:core:navigation` 的 sealed `AppDestination`：
```kotlin
navController.navigate(AppDestination.AppDetail(appId = "..."))
```

### b) Shared core repository
共用業務狀態（如 reputation）放 `:core:data` 的 repo；多 feature 注入同一 instance。

### c) Shared event bus (defer to V2)
V1 不引入 event bus；用 reactive Flow 從 shared repo 監聽變化即可。

## 7. Adding a new feature (template)

```bash
# 1. mkdir
mkdir -p feature/<name>/src/main/kotlin/com/apptest/feature/<name>/{nav,ui,domain/usecase,data}

# 2. build.gradle.kts (copy template — see monorepo.md §5)

# 3. settings.gradle.kts → 加 include(":feature:<name>")

# 4. 4 個 docs (README/API/FLOW/DEPENDENCY) — auto_docs.md §3 templates

# 5. AppDestination 新增 entry in :core:navigation

# 6. :app 的 NavHost 呼叫 <name>Graph(navController, ...)
```

## 8. V2 / V3 forecast features

| Feature (planned) | Tier | New cores needed? |
|---|---|---|
| `:feature:rating` | V2 | none |
| `:feature:fraud_review` | V2 (mod tool) | optional `:core:moderation` |
| `:feature:teams` | V3 | possibly `:core:billing` |
| `:feature:subscription` | V3 | `:core:billing` (Play Billing) |
| `:feature:beta_analysis` | V3 | possibly `:core:ml` (on-device LLM?) |
| `:feature:global_network` | V3 | none |

## 9. Open decisions

| ID | Decision | Status |
|---|---|---|
| APT-A-012 | 「shared core repository」放 `:core:data` 還是 per-feature data | default: V1 放 :core:data，V2 必要時切出 |
| APT-A-013 | `:feature:rating` 是新 module 還是併入 `:feature:appdetail` | default: V2 新建（避免 V1 module 改動） |
