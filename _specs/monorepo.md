# AppTest — Monorepo Structure & Build Conventions

> **Version:** 0.1 · **Last updated:** 2026-05-19 · **Owner:** TBD
> Folder 結構 + Gradle 規約 + 共用資源策略。Module DAG 見 `modularization.md`，feature 內部結構見 `feature_modules.md`。

---

## 1. Top-level layout

```
AppTest/
├── _specs/                       -- 所有 architecture spec markdown (本檔在此)
│   └── _ai/                      -- AI-friendly entry (manifest/PROMPTS/DEPS/README)
├── app/                          -- :app module (Hilt App + MainActivity + NavHost)
├── core/                         -- core modules folder
│   ├── common/
│   ├── designsystem/
│   ├── ui/
│   ├── domain/
│   ├── data/
│   ├── network/
│   ├── database/
│   └── navigation/
├── feature/                      -- feature modules folder
│   ├── auth/
│   ├── onboarding/
│   ├── home/
│   ├── myapps/
│   ├── appdetail/
│   ├── testing/
│   ├── profile/
│   └── inbox/
├── gradle/
│   ├── libs.versions.toml        -- version catalog (single SoT)
│   └── wrapper/
├── scripts/                      -- dev / CI helper scripts
├── .github/workflows/            -- CI definitions (per cicd.md)
├── build.gradle.kts              -- root
├── settings.gradle.kts           -- module includes + repo config
├── gradle.properties             -- jvm args, AndroidX flags, Compose flags
├── .gitignore
├── CLAUDE.md                     -- project-level Claude context (auto-loaded)
├── AGENT_HANDOVER.md             -- inter-session handover (created when needed)
└── README.md                     -- human-facing project intro
```

無 backend 程式碼（Ktor / Supabase schema）在這個 repo — 若要同 repo 管理，未來開 `backend/` 同層 sibling。

## 2. Module folder convention

每個 module 內部：

```
<module>/
├── build.gradle.kts              -- ≤ 80 lines；違反 = code review reject
├── README.md                     -- 模組做什麼 + 依賴誰 + 誰依賴我 + 如何替換
├── API.md                        -- 公開介面清單 (interface / sealed / Destination / NavGraph fn)
├── FLOW.md                       -- 主要資料/事件流 (Mermaid)
├── DEPENDENCY.md                 -- 詳細依賴表 + 替換策略 + 測試策略
├── src/
│   ├── main/
│   │   ├── AndroidManifest.xml   -- 視類型需要（pure JVM 模組免）
│   │   └── kotlin/com/apptest/<layer>/<name>/...
│   ├── test/                     -- unit tests (純 JVM)
│   ├── androidTest/              -- instrumented tests (Android-only)
│   └── debug/                    -- debug-variant only sources (e.g. FakeModules)
└── (optional) schemas/           -- Room schema dumps，only :core:database
```

4 份 docs 是 **hard rule**（CLAUDE.md），CI lint 會偵測缺檔。

## 3. Gradle convention

### 3.1 Settings (`settings.gradle.kts`)
- `pluginManagement` 限定 google + mavenCentral + gradlePluginPortal
- `dependencyResolutionManagement { repositoriesMode = FAIL_ON_PROJECT_REPOS }` 強制集中
- include 寫成 `include(":path:to:module")`，注意 colon

### 3.2 Plugins (root `build.gradle.kts`)
- 只 `apply false` declare plugin aliases
- 不在 root 寫 `dependencies { }` — repo-wide deps 走 version catalog

### 3.3 Per-module `build.gradle.kts` template

**Pure JVM module (`:core:common`, `:core:domain`):**
```kotlin
plugins { alias(libs.plugins.kotlin.jvm) }
java { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = ... }
kotlin { jvmToolchain(libs.versions.javaTarget.get().toInt()) }
dependencies {
    api(libs.kotlinx.coroutines.core)
    testImplementation(libs.bundles.test.unit)
}
```

**Android library module (`:core:network`, `:feature:*`):**
```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    // + compose if has @Composable
    // + ksp + hilt if has Hilt @Module / @Inject
    // + serialization if has @Serializable
}
android {
    namespace = "com.apptest.<layer>.<name>"
    compileSdk = 36
    defaultConfig { minSdk = 28 }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; ... }
    kotlinOptions { jvmTarget = libs.versions.javaTarget.get() }
    buildFeatures { compose = true }   // only when needed
}
dependencies { ... }
```

完整實例：見 `core/common/build.gradle.kts`、`core/designsystem/build.gradle.kts` (scaffolded 2026-05-19)。

### 3.4 Build script hard rules
- ≤ 80 lines/檔
- 不寫 inline configuration hack（如 `tasks.withType` 改設定）— 必要時抽 convention plugin 並文件化
- 不直接寫版本字串 — 一律走 `libs.versions.toml`

## 4. Version catalog usage

`gradle/libs.versions.toml` 是**唯一**版本 SoT。

- `[versions]` 段：純 number / string
- `[libraries]` 段：每個依賴一行，名稱 kebab-case，匹配 group/artifact
- `[plugins]` 段：plugin id + version ref
- `[bundles]` 段：常用組合（如 `compose-ui`, `retrofit-stack`）

**不允許**任何 `build.gradle.kts` 寫 `"androidx.foo:bar:1.2.3"` 字串字面值。

## 5. Shared resources strategy

| Resource type | Where it lives | 注意 |
|---|---|---|
| Brand colors / theme | `:core:designsystem/src/main/res/values/` | 用 design tokens，不對外 expose XML |
| Icons (custom SVG) | `:core:designsystem/src/main/res/drawable/` | 統一 32dp viewport |
| Strings (i18n) | `:core:common/src/main/res/values/strings.xml` + `values-zh-rTW/` | 跨 feature 共享；feature-specific strings 留 feature 內 res/ |
| Fonts | `:core:designsystem/src/main/res/font/` | system default 為主，brand font V2 才考慮 |
| Test fixtures | `:core:testing/src/main/.../fixtures/` | V1 內聯，V2 切獨立模組 |

`:core:common/res` 加 prefix `app_` (如 `app_common_error_network`) 避免跨模組 R class 衝突。

## 6. Build performance settings

`gradle.properties` 已 scaffold 2026-05-19，包含：parallel / caching / configuration-cache / nonTransitiveRClass / useKSP2。
- `nonTransitiveRClass=true` — 模組多時 R class 不傳遞，build 顯著加速
- `useKSP2=true` — KSP 2.x 比 1.x 快 ~30%
- `configuration-cache=true` — configuration phase 跳過，re-invoke 秒級

## 7. Gradle wrapper

- 版本鎖在 `gradle/wrapper/gradle-wrapper.properties` (2026-05-19 鎖 8.10.2)
- 升級 wrapper 走獨立 PR，title `[chore] bump gradle wrapper to X.Y.Z`
- `gradlew` / `gradlew.bat` 不手寫，跑 `gradle wrapper` 生成

## 8. Local dev tooling

| Tool | Purpose |
|---|---|
| **Android Studio Ladybug+** | IDE (對應 AGP 8.7+) |
| **JDK 17** | required |
| **Kotlin 2.1** | bundled via Studio |
| **Supabase CLI** | local DB schema + migration |
| **direnv** | `.envrc` 載入本地環境變數（不進 git） |
| **`./gradlew :app:assembleDebug`** | 最快 sanity check |
| **`./gradlew check`** | 跑所有 unit test + lint |

## 9. Secrets handling

| Secret | Storage |
|---|---|
| Supabase anon key | `local.properties` (not in git) → `BuildConfig` |
| Supabase service key | **never** in client; only server side |
| Firebase config | `app/google-services.json` (gitignored) |
| Signing keystore | `local.properties` ref path → signed config in `:app/build.gradle.kts` |
| API keys (third-party) | 同 Supabase anon |

Onboarding 新開發者要有 `_specs/dev_setup.md` (Phase G 內含？)，本檔不展開。

## 10. Open decisions

| ID | Decision | Status |
|---|---|---|
| APT-A-014 | 是否同 repo 含 backend (Ktor) 程式碼 | default: V1 否（client 與 backend 各自 repo），V2 看 |
| APT-A-015 | 是否引入 Gradle convention plugin | default: V1 否，每模組獨立 build script |
| APT-OPS-003 | Renovate / Dependabot 哪個管 deps 升級 | default: Renovate（更可控） |
