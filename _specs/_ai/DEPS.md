# AppTest — Dependency graphs

> 30-second status read. 4 圖：architecture phase flow / build phase flow / module DAG / owner-blocker fan-out。

---

## 1. Architecture phase flow (2026-05-19 全部完成)

```mermaid
flowchart LR
    A[Phase A<br/>Product arch + reputation +<br/>matchmaking + growth + anti-cheat] --> B[Phase B<br/>DB schema + API]
    A --> D[Phase D<br/>Wireframes + Onboarding +<br/>Design system + Compose]
    B --> C[Phase C<br/>Modularization +<br/>Feature catalog + Monorepo]
    D --> C
    C --> E[Phase E<br/>Navigation + Flows]
    A --> F[Phase F<br/>Backend architecture]
    B --> F
    C --> G[Phase G<br/>CI/CD + Auto-docs]
    A --> H[Phase H<br/>Play Store strategy]
    style A fill:#bef7be
    style B fill:#bef7be
    style C fill:#bef7be
    style D fill:#bef7be
    style E fill:#bef7be
    style F fill:#bef7be
    style G fill:#bef7be
    style H fill:#bef7be
```

24 個 spec 全 `status: done`，全 ≤ 200 行。

## 2. V1 build phase flow (架構後的工程實作)

```mermaid
flowchart LR
    F1[Foundation<br/>R-001~R-010<br/>Gradle + core modules] --> F2[V1 Features<br/>R-020~R-027<br/>8 feature modules]
    F2 --> F3[V1 Integrations<br/>R-040~R-048<br/>FCM/Supabase/WorkManager/<br/>Matching/Reputation/Anti-cheat]
    F3 --> READY[🚀 V1 ready for closed test]
    X[Cross-cutting<br/>X-001~X-005] -.-> F1
    X -.-> F2
    X -.-> F3
    style F1 fill:#fff4d6
    style F2 fill:#fff4d6
    style F3 fill:#fff4d6
    style READY fill:#bef7be,stroke:#2c7a2c
```

部分 R-001~R-010 已在 2026-05-19 動工（Gradle root + `:core:common` + `:core:domain` + 部分 `:core:designsystem`），完整 scaffold 等下一個 session 接手。

## 3. Module DAG (簡化版 — 完整見 `modularization.md §2`)

```mermaid
flowchart TB
    APP[:app] --> FEAT[8× :feature:*]
    FEAT -.-> CORE[8× :core:*]
    APP --> CORE_NAV[:core:navigation]
    APP --> CORE_DS[:core:designsystem]
    CORE_UI[:core:ui] --> CORE_DS
    CORE_DATA[:core:data] --> CORE_NET[:core:network]
    CORE_DATA --> CORE_DB[:core:database]
    CORE_DATA --> CORE_DOM[:core:domain]
```

**規則：** feature 不能 import feature；core 不能 import feature。詳見 [`modularization.md`](../modularization.md) §3。

## 4. Critical path to "first runnable demo APK"

```mermaid
flowchart LR
    R001[R-001<br/>Gradle root ✓] --> R003[R-003<br/>:core:common ✓]
    R003 --> R002[R-002<br/>:core:designsystem]
    R003 --> R005[R-005<br/>:core:domain ✓]
    R002 --> R004[R-004<br/>:core:ui]
    R003 --> R009[R-009<br/>:core:navigation]
    R005 --> R010[R-010<br/>:app + NavHost]
    R004 --> R010
    R009 --> R010
    R002 --> R010
    R010 --> R022[R-022<br/>:feature:home<br/>(FakeRepo)]
    R022 --> RUN[🎯 First demo APK<br/>home feed with mock data]
    style R001 fill:#bef7be
    style R003 fill:#bef7be
    style R005 fill:#bef7be
    style RUN fill:#ffe09e
```

✓ = 2026-05-19 已完成的步驟。剩 ~6 步驟 (R-002 / R-004 / R-009 / R-010 / R-022) 可達第一個 runnable APK。

## 5. Owner-blocker fan-out (剩 6 個未決)

```mermaid
flowchart LR
    A002[APT-A-002<br/>Email login provider]
    A003[APT-A-003<br/>Install verify 強度]
    A004[APT-A-004<br/>V2 ML infra]
    P001[APT-P-001<br/>Credit curve]
    OPS1[APT-OPS-001<br/>Domain + Play account]
    A002 --> R020[R-020<br/>:feature:auth real impl]
    A003 --> R040[R-040<br/>Install detection]
    A004 --> V2[V2 ML stack]
    P001 --> R023[R-023<br/>:feature:myapps credits]
    OPS1 --> RELEASE[release to Play]
```

APT-A-001 (backend platform) **已 resolved** 2026-05-19 → Firebase + Supabase + Ktor。

## 6. Quick-query commands

```bash
# 下一個 unblocked task (architecture done → 工程 R-* tasks)
grep -B1 "status: not_started" _specs/_ai/manifest.yaml | grep "APT-V1-R-" | head -10

# 看 V1 features 進度
grep -E "id: APT-V1-R-" _specs/_ai/manifest.yaml

# 看 owner 待決
grep -A1 "^  - { id: APT-[AP]-" _specs/_ai/manifest.yaml

# 找 spec by topic
grep "topic:" _specs/_ai/manifest.yaml | head -20

# 確認檔案行數合規 (應 ≤ 200)
wc -l _specs/*.md | sort -n | tail -5
```

## 7. 各 phase 文件密度 (供 token budget 預估)

| Phase | Files | Lines | Avg |
|---|---:|---:|---:|
| A (product) | 6 | 769 | 128 |
| B (data/API) | 3 | 443 | 148 |
| C (modules) | 3 | 534 | 178 |
| D (UI) | 6 | 974 | 162 |
| E (flows) | 2 | 390 | 195 |
| F (backend) | 1 | 180 | 180 |
| G (ops) | 2 | 292 | 146 |
| H (launch) | 1 | 165 | 165 |
| AI layer | 4 (incl. manifest) | 616 | 154 |
| **Total** | **28** | **4363** | **156** |

冷啟動載 `_ai/README.md` + `_ai/manifest.yaml` + 1~2 個 spec ≈ 15~25k tokens（vs 載全 28 個 ≈ 110k）。
