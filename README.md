# AppTest

**Android App Testing Exchange Network** — 解 Google Play closed test 12-tester × 14-day 痛點的開發者互測平台。
詳細產品思路見 [`_specs/product_architecture.md`](_specs/product_architecture.md)。

---

## 🚀 Quick start

### 環境
- **Android Studio Ladybug+** (AGP 8.7+)
- **JDK 17**
- **Kotlin 2.1.x**

### 第一次 build

```bash
# 1. 進專案根目錄
cd AppTest

# 2. 產 gradle wrapper (若還沒) — 需本機裝 Gradle 8.10.2+
gradle wrapper

# 3. 全 build (cold ≈ 5 min，下載 Hilt KSP + Compose BOM dependencies)
./gradlew :app:assembleDebug

# 4. 跑在 emulator / 連線裝置
./gradlew :app:installDebug
```

或直接用 Android Studio：**File → Open** 選 `AppTest/` 根目錄，等 Gradle sync 完，按 Run。

### 沒灌 Gradle 時
Android Studio 會自帶 Gradle；只要打開專案就會用 IDE 內的 Gradle 跑。`gradle wrapper` 那步可由 IDE 自動處理（Build → Sync Project with Gradle Files）。

---

## 🎬 Demo journey (V1 全套 client-side mock data)

| Step | 看到什麼 |
|---|---|
| 1. 啟動 | SignIn 畫面 — 「Join 1,238 Android devs...」+ Google / Email 兩 button |
| 2. tap "Continue with Google" | 600ms fake delay → 跳 Onboarding |
| 3. Onboarding step 1 | 選 intent（default "Test others' apps"）→ Continue |
| 4. step 2 | 多選 categories → Continue |
| 5. step 3 | 多選 languages → Done |
| 6. MainRoot (stub) | 4 個 tab 按鈕 |
| 7. → Home | matched feed: NoteFlash 配對卡 + active tests + your apps 三段 |
| 8. → AppDetail (從 Home 點卡) | NoteFlash 詳情 + 3 個 match reasons + sticky Join CTA |
| 9. tap "Join test" | 真的開 Play Store opt-in URL（Intent.ACTION_VIEW） |
| 10. → MyApps | 2 個 mock apps + FAB Create |
| 11. → AppEditor | 6 個欄位 form + 即時 Play URL 驗證 |
| 12. → Testing | filter chips + AtRisk row 含 Heartbeat-now action |
| 13. → Profile | Silver tier 大 badge + 4 sub-score progress bars + activity log |
| 14. → Inbox | 4 個 notification 含 deep-link |

**全部 8 個 features 由 `FakeXxxRepository` in-memory 驅動** — 沒接真後端，重啟 app 狀態清零。

---

## 📁 Project structure

```
AppTest/
├── _specs/         24 architecture markdown — single source of truth for ALL design decisions
│   └── _ai/        AI cold-start layer (manifest / PROMPTS / DEPS / README)
├── app/            :app module — Hilt App + MainActivity + NavHost + 4 docs
├── core/           8 core modules
│   ├── common/         pure Kotlin: Result / AppError / DispatcherProvider / ReputationTier / AuthState
│   ├── designsystem/   M3 Expressive theme + 5 atoms + tokens
│   ├── ui/             molecules + organisms + templates (≤ 200 lines per file)
│   ├── domain/         UseCase base + Repository marker + AuthRepository interface
│   ├── data/           build infra only — populated when real backend wired
│   ├── network/        build infra only — Retrofit/Ktor base coming with R-043
│   ├── database/       build infra only — Room AppDatabase coming with R-040
│   └── navigation/     AppDestination sealed + AppDeepLink + startDestinationFor
└── feature/        8 feature modules (each: nav/ + ui/ + domain/ + data/ + 4 docs)
    ├── auth/           SignIn (Google fake + Email magic) + EmailVerify + AuthRepository impl
    ├── onboarding/     3-step wizard
    ├── home/           matched feed + greeting + active/owned sections
    ├── myapps/         dev side: list + Editor with Play URL validate
    ├── appdetail/      tester journey: detail + Join (opens Play Store)
    ├── testing/        active/completed dashboard + AtRisk + Heartbeat-now
    ├── profile/        tier + credits + reputation breakdown + proofs + activity
    └── inbox/          5 notification types + Mark-all-read
```

---

## 📚 Where to find what

| 想了解 | 看哪 |
|---|---|
| 產品願景 / 四支柱 / Growth loop | [`_specs/product_architecture.md`](_specs/product_architecture.md) |
| V1/V2/V3 範圍 + entities | [`_specs/mvp.md`](_specs/mvp.md) |
| Reputation 公式 | [`_specs/reputation_system.md`](_specs/reputation_system.md) |
| AI 配對 (V1 規則 / V2 ML) | [`_specs/ai_matchmaking.md`](_specs/ai_matchmaking.md) |
| 反作弊 | [`_specs/anti_cheat.md`](_specs/anti_cheat.md) |
| DB schema (PostgreSQL) | [`_specs/database_schema.md`](_specs/database_schema.md) + `_audit.md` |
| REST + WebSocket API | [`_specs/api_contracts.md`](_specs/api_contracts.md) |
| Module DAG + hard rules | [`_specs/modularization.md`](_specs/modularization.md) |
| 每 feature 責任 | [`_specs/feature_modules.md`](_specs/feature_modules.md) |
| Compose Navigation + deep links | [`_specs/navigation.md`](_specs/navigation.md) |
| End-to-end flow (Mermaid sequenceDiagrams) | [`_specs/testing_exchange_flow.md`](_specs/testing_exchange_flow.md) |
| Design tokens (M3 Expressive + Glass + Floating) | [`_specs/design_system.md`](_specs/design_system.md) |
| UI 畫面 wireframes | [`_specs/wireframes.md`](_specs/wireframes.md) + `_dev.md` |
| Onboarding UX 流程 | [`_specs/onboarding_ux.md`](_specs/onboarding_ux.md) |
| 後端架構 (Firebase + Supabase + Ktor) | [`_specs/backend_architecture.md`](_specs/backend_architecture.md) |
| CI/CD + Play track 策略 | [`_specs/cicd.md`](_specs/cicd.md) |
| Play Store 上架計畫 | [`_specs/play_store_strategy.md`](_specs/play_store_strategy.md) |
| 目前 state + 下一步 | [`AGENT_HANDOVER.md`](AGENT_HANDOVER.md) |
| 找下一個 not_started task | `grep -B1 "status: not_started" _specs/_ai/manifest.yaml` |
| 怎麼跑 AI session 接手 | [`_specs/_ai/README.md`](_specs/_ai/README.md) |

每個 module 內部也有自己的 4 份 docs：`README.md` (做什麼) / `API.md` (公開介面) / `FLOW.md` (內部流程 Mermaid) / `DEPENDENCY.md` (依賴 + 替換 + 測試)。

---

## 🛠 怎麼加新功能

### 加 feature 新 destination
1. 在 [`core/navigation/.../AppDestination.kt`](core/navigation/src/main/kotlin/com/apptest/core/navigation/AppDestination.kt) 加新 `@Serializable data object/class`
2. 在對應 `:feature:*` 加 `composable<AppDestination.New> {...}` 到自家 `<feat>NavGraph.kt`
3. `:app/.../AppNavHost.kt` 呼叫該 graph fn

### 加新 :feature 模組
最快路徑：複製 `:feature:inbox` (最簡單模板) → 改名 → 改 `:core:navigation` 加 destination → wire 進 `:app`。詳細 5 步驟見 [`app/README.md`](app/README.md) §Quick example。

### 換 Fake → 真 backend
1. 找 `feature/<name>/data/di/<Name>DataModule.kt` 的 `@Binds`
2. 寫新 impl class 取代 `Fake<Name>Repository`
3. 改 `@Binds` 指新 class
4. UI / VM / UseCase / Models 全不動（Clean Architecture 紅利）

---

## ⚠️ Hard rules (PR review 會 reject)

- **每檔 ≤ 200 行**（拆 sub-Composable / sub-spec 若接近）
- **No feature → feature import**（跨 feature 共享走 `:core:domain` interface）
- **No core → feature import**
- **No string nav routes**（用 `@Serializable AppDestination`）
- **No hard-code colors** — 走 `MaterialTheme.colorScheme.*` 或 `AppExtended.colors.*`
- **No hard-code dp 非 8 倍數** — 用 `AppSpacing.*`
- **List screen 缺三態（Loading/Error/Empty）= reject**
- **UI 不可直接呼叫 Repository** — 必走 UseCase
- **ViewModel 不可 import Context / View / Composable**
- **一律走 Play Store closed test URL**，禁 APK 直傳
- **Credits 不可購買**（V3 訂閱例外但不可買排序）
- **Reputation 不可重設**，不公開「過去測過哪些 App」

完整 hard rules 見 [`_specs/_ai/manifest.yaml`](_specs/_ai/manifest.yaml) `hard_rules:` 段。

---

## 🤖 想用 AI 接手繼續開發

讀 [`_specs/_ai/README.md`](_specs/_ai/README.md) 看冷啟流程。30 秒就能上手：

```bash
cat AGENT_HANDOVER.md _specs/_ai/README.md
grep -B1 "status: not_started" _specs/_ai/manifest.yaml | grep "APT-V1-R-" | head -5
```

Prompt 範本在 [`_specs/_ai/PROMPTS.md`](_specs/_ai/PROMPTS.md) — copy 一個進新 session 即可。

---

## 📌 目前狀態 (2026-05-19)

- ✅ **24 architecture specs** 全完
- ✅ **5/8 core modules** 有 src
- ✅ **8/8 V1 features** 上線（全 FakeRepo 驅動）
- ✅ **`:app` NavHost** wire 完整
- 🟡 **3 core modules** (data / network / database) 只 build infra，等真 backend
- ⏳ **V1 integrations** (R-040~R-048) 等實作（install detection / WorkManager / FCM / Supabase / Ktor）
- ⏳ **CI / Spotless / Crashlytics / i18n** 等 ops 階段

下一個建議步驟：跑 build sanity check → 開 Ktor backend repo → 接 Supabase 真 Auth。

詳細 status / 6 個 open decisions / 下一步路線見 [`AGENT_HANDOVER.md`](AGENT_HANDOVER.md)。
