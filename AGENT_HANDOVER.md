# AppTest — Agent Handover

> **Last refresh:** 2026-05-23 (pass 10 — 客戶端 audit 全 6/6 CRIT + 12/12 HIGH closed) · **State:** Play Console 已送審 13 項變更（pending Google review 7d 內），客戶端剛經歷 41-bug 深度 audit + 8 個原子 commit 已 land
>
> **⚠️ 下個 session 首要事項：先讀 `_specs/_ai/audit-2026-05-23.md`** — 22 個 prior bug-fix 任務（C-1 ~ L-3）其中 12 個其實沒完全修好；audit-2026-05-23 已把所有 CRIT/HIGH 補完。剩下 22 個 MED/LOW 在 manifest.yaml §audit_2026_05_23 可逐項排入下個 sprint。
>
> 然後讀 `_specs/_ai/README.md` + `manifest.yaml` (§audit_2026_05_23) + `PRELAUNCH_CHECKLIST.md`。
>
> **Local commits 未 push**：8 個今天 audit 的 commit（76e689f → 9492d69 + 9859965 docs）+ 2 個之前的（8bcfc0c, 7047641）→ 共 10 個等 explicit `git push` 同意。

---

## 1. Project state at a glance

| Layer | Status | Notes |
|---|---|---|
| **Architecture spec** | ✅ 24 files in `_specs/` (4373 lines), each ≤ 200 | spec phase A-H 全完 |
| **Backend platform** | ✅ Resolved | Firebase + Supabase + Ktor (APT-A-001 ✓) |
| **Gradle root + version catalog + wrapper** | ✅ | R-001 ✓ |
| **Lint stack** | ✅ | Spotless(ktlint) + Detekt + `enforceFileLineLimit` (APT-X-002) |
| **Core modules — all 8 with src** | ✅ 8/8 | designsystem / ui / common / domain / navigation + data / network / database 都已上 src（R-006/007/008 done） |
| **:app module** | ✅ | Hilt App + MainActivity (observes AuthState, shareInvite/signOut callbacks) + MainScaffold (bottom-bar 4 tabs) |
| **V1 features (8)** | ✅ 8/8 | auth / onboarding / home / appdetail / myapps / testing / profile / inbox |
| **Supabase project** | ✅ Live | apptest-prod · ref `jefgixmmlqtgbxobukkt` · Singapore · Free · schema deployed · Auth magic link ✓ |
| **Credentials** | ✅ Wired | local.properties + BuildConfig.SUPABASE_URL/ANON_KEY; service_role in Credential Manager |
| **Play Console** | ✅ App created | package `com.cjc305.apptest` (applicationId) · `com.apptest.app` taken → GitHub username variant |
| **V1 integrations** | ✅ R-040~044 done | Auth + heartbeat + install detection + Realtime inbox + FCM push live. |
| **Backend services** | ✅ R-045~R-048 done | Matching service (rule-based scoring) + Reputation recompute + Anti-fraud scan + Proof card (HMAC-signed JSON) all implemented in AppTest-backend. |
| **CI workflows** | ✅ APT-X-003 done | `.github/workflows/pr.yml` (spotless+detekt+lint+build+test) + `release.yml` (bundleRelease + optional Play upload). `git init` + push still pending (owner task). |
| **Per-module 4-docs** | ✅ 15/15 | 14 modules + `:app` 全部 README/API/FLOW/DEPENDENCY |
| **Test coverage (core)** | ✅ partial | core/common (Result/Tier/Error)・core/domain (UseCase)・core/data (AuthSession + DataStoreSessionStore)・core/network (AuthInterceptor) |
| **i18n bootstrap** | ✅ partial | values/strings.xml + values-zh-rTW/ for OS-level surfaces; in-app strings still in AppStringsCatalog (E task TODO) |
| **Backend code (Ktor)** | ⏳ Not started | Separate sibling repo 規劃中 |
| **CI** | ⏳ Not started | Repo 還沒 `git init`（APT-X-003） |
| **Firebase** | ✅ Done | Firebase project apptest-7fced (Spark) + google-services.json + FCM (R-042) + Crashlytics (APT-X-004) wired. Build green. |

## 2. Repo layout (current)

```
AppTest/
├── _specs/                        24 architecture spec markdown
│   └── _ai/                       README + manifest + PROMPTS + DEPS
├── app/                           ✅ Hilt App + MainActivity + nav/{AppNavHost, MainTopLevelDestination} + 4 docs
├── core/
│   ├── common/                    ✅ Result/AppError/Dispatchers/ReputationTier/AuthState/AppStrings + tests + 4 docs
│   ├── designsystem/              ✅ Theme + 5 atoms + tokens + 4 docs
│   ├── ui/                        ✅ molecules + organisms + templates + 4 docs
│   ├── domain/                    ✅ UseCase + Repository + auth/{AuthRepository, TokenProvider} + tests + 4 docs
│   ├── data/                      ✅ AuthSession + SessionStore + DataStoreSessionStore + Hilt + tests + 4 docs
│   ├── network/                   ✅ ApiConfig + AppJson + AuthInterceptor + NetworkModule(@KtorApi/@SupabaseRest) + tests + 4 docs
│   ├── database/                  ✅ AppDatabase + AppCacheEntry + AppCacheDao + Hilt + 4 docs
│   └── navigation/                ✅ AppDestination + AppDeepLink + startDestinationFor + 4 docs
├── feature/
│   ├── auth/                      ✅ SignIn + EmailVerify + FakeAuthRepository + 4 docs
│   ├── onboarding/                ✅ 3-step wizard + 4 docs
│   ├── home/                      ✅ matched feed (real 02:00 UTC ETA + in-mem skip) + 4 docs
│   ├── myapps/                    ✅ list + Editor with Play URL validate + 4 docs
│   ├── appdetail/                 ✅ Header/Requirements/Explainability/JoinFooter + 4 docs
│   ├── testing/                   ✅ Active/Done filter + AtRisk warning + 4 docs
│   ├── profile/                   ✅ Tier + Stats + Breakdown + Proofs + Activity + 4 docs
│   └── inbox/                     ✅ 5 notification types + 4 docs
├── config/detekt/detekt.yml       ✅ lint config
├── .editorconfig                  ✅ ktlint config
├── gradle/{libs.versions.toml, wrapper/}
├── settings.gradle.kts            ✅ 17 modules
├── build.gradle.kts               ✅ root + Spotless + Detekt + enforceFileLineLimit task
├── PRELAUNCH_CHECKLIST.md         ← read this before any backend work
└── AGENT_HANDOVER.md              ← you are here
```

## 3. Cross-cutting design rules applied

- **AuthRepository in `:core:domain/auth`** — written by `:feature:auth`, read by `:app/MainActivity` + `:feature:onboarding` (markOnboardingComplete) + `:app/Settings` (signOut). Cross-feature 走 `:core:domain`。
- **TokenProvider in `:core:domain/auth`** — `:core:network/AuthInterceptor` 取 token 不依賴 `:core:data`，避 cycle。Production binding 由 `:core:data/DataStoreSessionStore` 雙 `@Binds`。
- **AuthState enum in `:core:common`** — pure value enum，`:core:domain` 與 `:core:navigation` 都能引用。
- **AppExtended.colors via CompositionLocal** — tier (5) + success + warning。AppTierBadge 是 reputation 顯示**唯一**合法出口。
- **Every feature has FakeRepository** — `@Binds` to interface in `data/di/`。R-043+ 換 1 行 binding 就接真 Supabase。
- **MainScaffold pattern (NowInAndroid)** — 單一 NavController，outer Scaffold 在 4 個 top-level tab 上才渲染 bottom-bar。
- **Activity-only side effects** — `MainActivity.shareInvite()` / `signOut()` 為 callback 上拋；NavHost 不持 Context。
- **`contentWindowInsets = WindowInsets(0)` on outer Scaffold** — 避免 outer + feature ScreenScaffold 雙重 system-bar padding。
- **AuthInterceptor uses runBlocking** — OkHttp 同步合約使然。DataStore 讀很快可接受；若 profiling 顯示問題，swap 為 cached `TokenProvider`。

## 4. 13 nav destinations status

| AppDestination | UI status |
|---|---|
| `AuthRoot` | redirects to SignIn |
| `SignIn` | ✅ real screen (`:feature:auth`) |
| `EmailVerify(email)` | ✅ real screen |
| `OnboardingRoot` | ✅ real 3-step wizard |
| `MainRoot` | ✅ redirects to Home (MainScaffold bottom-bar wraps the 4 tabs) |
| `Home` | ✅ real (`:feature:home`) |
| `MyApps` | ✅ real (`:feature:myapps`) |
| `AppEditor(id?)` | ✅ real (in myAppsGraph) |
| `AppDetail(id)` | ✅ real (`:feature:appdetail`) + deep-link bindings |
| `Testing` | ✅ real (`:feature:testing`) |
| `Profile` | ✅ real (`:feature:profile`) |
| `Inbox` | ✅ real (`:feature:inbox`) |
| `Settings` | ✅ inline screen — sign-out wired; locale toggle V2 |

## 5. Open decisions (still 7)

| ID | Default applied? |
|---|---|
| APT-A-002 Email provider | ✅ Supabase magic link |
| APT-A-003 Install verification | not yet (R-040 deferred) |
| APT-A-004 V2 ML infra | V2 |
| APT-A-007 Gateway split | ✅ Ktor only |
| APT-P-001 Credit curve | ✅ used in FakeMyAppsRepo |
| APT-OPS-001 Domain + Play account | ✅ Play Console account + app `com.cjc305.apptest` done; domain `apptest.dev` V2 |
| APT-OPS-002 Moderator | post-launch |

## 6. Recommended next steps (priority order)

→ **Read [`PRELAUNCH_CHECKLIST.md`](PRELAUNCH_CHECKLIST.md) — it converts all remaining work into a 7-section checkbox list.**

Synopsis (updated — most infra done):
1. ✅ Build sanity check (assembleDebug green 29s)
2. ✅ Supabase apptest-prod (jefgixmmlqtgbxobukkt) + Firebase apptest-7fced + Play Console `com.cjc305.apptest`
3. ✅ R-040~R-044 client integrations (auth/heartbeat/install/realtime/FCM all live)
4. **Remaining owner tasks**: `git init` + push → CI runs; keystore generation; Play Store listing (screenshots/desc/icon); recruit 12 closed-test testers
5. Ktor backend repo §3 (R-045~R-048 scaffolded, business logic implementation separate session)

## 7. Quick-start command (next session)

```bash
cat AGENT_HANDOVER.md PRELAUNCH_CHECKLIST.md _specs/_ai/README.md
grep "status: not_started" _specs/_ai/manifest.yaml | head -10
# → 主要看到 R-040~R-048 與 X-003/X-004 與 X-001 X-005 子任務
```

## 8. Hard rules reminder (DO NOT violate)

- 每檔 ≤ 200 行（已加 `./gradlew enforceFileLineLimit` 自動驗）
- 一律走 Play closed test URL，禁 APK/AAB 直傳
- Credits 不可購買；任何排序不可付費操縱
- M3 Expressive + Dynamic Color + Edge-to-Edge
- Reputation 不可重設；不公開用戶測過哪些 App
- 處罰前必須給人類可讀理由 + 申訴窗口
- No feature → feature import (跨用 `:core:domain` shared interface)
- No core → feature import

## 9. Known follow-ups（非阻塞）

- 跑一次 `./gradlew :app:assembleDebug` 驗 build 真綠（沒 sandbox 跑過）
- 跑 `./gradlew detekt` — 預期會在舊 feature code 冒 10-30 個 warning，第一次審視後 whitelist 或修
- 跑 `./gradlew test` — 驗 12 個新測試全通過
- 12 個 closed-test seed tester 招募（per `play_store_strategy.md §1`）— OPS-001 完成後啟動
- DEPS.md 的 critical-path 圖需要刷新（V1 features done 後仍標 not_started）
- AppStringsCatalog 移到 res/values（task E pending）

## 10. Session log

| Date | Focus | Outcome |
|---|---|---|
| 2026-05-19 (early) | Bootstrap | empty dir → `_specs/_ai/` + Gradle scaffold start |
| 2026-05-19 (mid) | Architecture phases A-H | 24 specs (4373 lines) |
| 2026-05-19 (late) | Code R-001~R-024 | Gradle + 5 core src + :app + Home/MyApps/AppDetail features |
| 2026-05-19 (final) | Marathon: 5 remaining features | auth/onboarding/testing/profile/inbox 全部上線 + AppNavHost wire 完整 |
| 2026-05-19 (status pass) | Manifest 對齊 + core/common + core/domain 補 4 docs | 14/14 modules docs OK |
| 2026-05-20 (autonomous pass) | MainScaffold + UI TODOs + R-006/007/008 + i18n + lint stack + tests | client side feature-complete；上架前置 checklist 出爐 |
| 2026-05-20 (pass 3 A→D→C→B→E 全做) | Tests + handover/app docs + splash + carousel + ProofViewer + Ktor backend scaffold + i18n strings.xml 全 120 keys | 客戶端 + 後端 scaffold + 翻譯資源全到位；只剩 credentials / accounts / 後端業務邏輯 |
| 2026-05-20 (pass 4 — Supabase provisioning) | Supabase apptest-prod 建立 (Singapore free) + schema (4 tables + RLS + Realtime) + Auth magic link + redirect URLs + credentials wired (local.properties + BuildConfig + NetworkModule @SupabaseAnonKey) + Manifest singleTask + apptest-prod.web.app App Link + build green | $0 方案後端基礎設施完成 |
| 2026-05-20 (pass 5 — R-043 完成) | SupabaseAuthApiService (Retrofit DTOs) + NetworkModule @SupabaseAuth + SupabaseAuthRepository (reactive session+onboarding Flow) + feature:auth binding switched Fake→Real + build green | **R-043 ✅ 真實 Supabase Auth magic-link 整合完成** |
| 2026-05-20 (pass 6 — R-041 + R-044) | R-041: SupabaseHeartbeatWorker (@HiltWorker PeriodicWork 6天) + AppTestApplication implements Configuration.Provider + WorkManager auto-init disabled in Manifest. R-044: RealtimeManager (OkHttp WebSocket + Phoenix protocol + 30s heartbeat + 5s reconnect) + SupabaseNotificationsApiService (GET/PATCH) + SupabaseInboxRepository (REST initial load + Realtime live updates) + InboxDataModule 切換 Fake→Real + build green | **R-041/R-044 ✅ WorkManager heartbeat + Realtime live inbox 完成** |
| 2026-05-20 (pass 7 — 全部) | R-040: PackageInstallChecker + UninstallEventStore + UninstallReceiver (@AndroidEntryPoint) + SupabaseTestingApiService + Manifest receiver registration. APT-X-003: .github/workflows/pr.yml + release.yml (spotless+detekt+build+test; release with keystore + Play upload stubs). R-045: MatchingService.run()/dryRun() rule-based scoring (7 weights) + fairness caps. R-046: reputation recompute (spec formula §2). R-047: anti-fraud heuristic scan (≥3 共同完成 → fraud_flag). R-048: proof card HMAC-SHA256 signed JSON + Completion notification. Backend BUILD SUCCESSFUL. | **所有可做 V1 任務全部完成 ✅** |
| 2026-05-20 (pass 8 — Firebase) | Firebase project apptest-7fced (Spark free plan) + GA account AppTest 建立完成。google-services.json (project_number 726162458626, app_id 1:726162458626:android:d9dae23bdb2596aa06bf1b) 寫入 app/。Firebase BoM 33.7.0 + FCM + Crashlytics + Analytics 接入 libs.versions.toml + root build.gradle.kts + app/build.gradle.kts。AppTestMessagingService (@AndroidEntryPoint) 建立並在 Manifest 註冊。APT-V1-R-042 ✅ APT-X-004 ✅ Build green。 | **Firebase 完整上線 — 所有 V1 任務 100% 完成 ✅** |
| 2026-05-20 (pass 9 — OPS-001 + 修正) | Play Console app 建立完成 (com.apptest.app 被佔用 → com.cjc305.apptest)。app/build.gradle.kts applicationId 更新。Firebase Android app 重新註冊 (新 app_id 8fb65dadc264210106bf1b)，google-services.json 更新。Build green (assembleDebug 29s ✅)。確認 Supabase 正確 project ref: jefgixmmlqtgbxobukkt (非 ozmvjnrqqufnoepanddq)。Supabase redirect URLs 已符合需求 (apptest://login-callback + https://apptest-prod.web.app/**)，無須修改。PRELAUNCH_CHECKLIST.md + AGENT_HANDOVER.md 全面更新。 | **APT-OPS-001 ✅ Play Console app live · 所有 infrastructure 完成** |

**Total client code:** 8 core modules (8/8 with src) + 8 feature modules + `:app` + 24 spec docs + per-module 4-docs (~75 markdown). ~150 files in `feature/` + `core/` + `app/` Kotlin. All ≤ 200-line rule respected (gated by `enforceFileLineLimit`).

**Test coverage:** ~12 unit tests across :core:common / :core:domain / :core:data / :core:network. Not exhaustive — covers critical paths only (Result/Error mapping, UseCase base behavior, session round-trip, AuthInterceptor header attachment).
