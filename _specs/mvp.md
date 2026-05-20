# AppTest — Product Scope (V1 / V2 / V3)

> **Version:** 0.2 · **Last updated:** 2026-05-19 · **Owner:** TBD
> 三階段 roadmap 的單一真實來源。manifest.yaml 以本檔節錨參照，不重複內容。
> 產品架構深入解析見 `product_architecture.md` 與其下游 pillar 檔。

---

## 1. Problem

Google Play 個人開發者首發必須完成 closed test：**≥ 12 testers × ≥ 14 days**。獨立開發者卡死在「找夠人 + 撐 14 天」這道閘。

AppTest 不是工具，是 **AI Native Testing Network** — 互測 + Reputation + AI 配對 + 信任機制，把單次互助升級成可長期 compound 的網路效應。

## 2. Target user

- **Primary:** 即將首發的個人 / 小團隊 Android 開發者。
- **Secondary:** 已上架但想擴大早期 tester 池的開發者。
- **Anti-persona:** iOS only 開發者、純使用者（非 dev）、企業帳號（不受 12/14 限制）。

絕大多數使用者**同時**是 dev + tester。沒有「純 tester」角色 — 連 onboarding 都不分。

## 3. Three-tier roadmap

| Tier | 定位 | 上線時點 | 關鍵新增 |
|---|---|---|---|
| **V1 (MVP)** | 自動互測網路啟動 | 0~3 月 | Google + Email login / 建立任務 / 規則式自動配對 / Push / 基本 Reputation |
| **V2** | AI + 信任升級 | 3~6 月 | AI Matchmaking / AI Fraud Detection / AI Feedback Summary / Tester 評分 |
| **V3** | 規模化 + 變現 | 6~12 月 | 團隊測試 / Subscription / AI Beta Analysis / Global Tester Network |

---

## 4. V1 (MVP) — scope

### 4.1 V1 features
- **Auth:** Google Sign-In + Email Magic Link（兩者擇一即可登入）
- **App 建立:** 開發者貼上 package name + Play closed-test opt-in URL + 中英描述 + 1 icon + 期望 tester 數（預設 12）+ 期望天數（預設 14）
- **自動配對 (rule-based):** 系統用 6 個訊號（類別 / 語言 / 信任分 / 完成率 / 剩餘配額 / 時區）做 scoring，每天 02:00 UTC 跑一次配對（cron）；deep-dive 見 `ai_matchmaking.md` §V1
- **配對通知:** Push (FCM) + in-app inbox
- **測試確認:** 使用者點 Push → 跳 Play Store opt-in URL → 安裝 → 回 App 確認 → 進 14 天計時
- **基本 Reputation:** completion_rate / installs_count / days_contributed / abandonment_count 四維分數；deep-dive 見 `reputation_system.md`
- **Heartbeat:** WorkManager 每日檢查 `PackageManager.getPackageInfo()`；發 ping 給後端
- **完成證明:** 達標時生成可截圖的 "Play Console-ready" 卡片

### 4.2 V1 explicit OUT
- ❌ iOS / TestFlight / web
- ❌ APK / AAB 直傳（一律走 Play closed test URL）
- ❌ 站內聊天 / DM
- ❌ Tester 評分 App（V2）
- ❌ AI 配對（V2，V1 用規則）
- ❌ Open test / Production track 支援
- ❌ 付費 Credits（V3 才開放訂閱）
- ❌ 影片 trailer / 多語介面（除 zh-TW + en）
- ❌ Web admin（CLI script 足夠）
- ❌ Wear / TV / Auto

## 5. V2 — AI + Trust 升級

| Feature | 取代/升級 V1 什麼 |
|---|---|
| **AI Matchmaking** | 取代 V1 規則 scoring；用 user embeddings + ranking model |
| **AI Fraud Detection** | 在 V1 heuristic 反作弊上加 anomaly detection（見 `anti_cheat.md` §V2） |
| **Tester 評分 App** | 開發者可給 tester 評分 → 反向影響該 tester reputation |
| **自動提醒 (smart)** | LLM 生成提醒文案，依 tester 過往 engagement 客製化時點 |
| **AI Feedback Summary** | tester 留 comment / bug 報告 → LLM 摘要給開發者 |

## 6. V3 — Scale + Monetize

| Feature | 說明 |
|---|---|
| **團隊測試** | 多人共同 own 一個 App / shared 配額池 |
| **Subscription** | Pro 方案：優先配對 / 無限 App / AI Beta Analysis / 早期版本 trial |
| **AI Beta Analysis** | 跨 tester 行為彙整 → 給開發者 retention / crash hotspot 報告 |
| **Global Tester Network** | 跨區域配對 + 多語言 / 地區 / 時區優化 |

## 7. Entities (V1 only — V2/V3 增補另載)

| Entity | V1 fields |
|---|---|
| **User** | `id`, `email`, `displayName`, `photoUrl?`, `authProvider` (google/email), `locale`, `reputation` (computed, see `reputation_system.md`), `credits` (int, see `growth_and_network.md` §credits), `createdAt` |
| **App** | `id`, `ownerId`, `packageName` (unique), `name`, `description`, `iconUrl`, `category`, `playOptInUrl`, `requiredTesters` (default 12), `requiredDays` (default 14), `status` (`recruiting`/`active`/`completed`/`paused`), `currentTesters` (computed), `createdAt` |
| **TestRequest** | `id`, `appId`, `testerId`, `status` (`matched`/`installed`/`active`/`completed`/`abandoned`), `matchedAt`, `installedAt?`, `lastHeartbeatAt?`, `daysActive` |
| **MatchRun** | `id`, `runAt`, `algorithmVersion`, `pairsCreated`, `metricsJson` (audit log for V1 rule, becomes ML inference log in V2) |
| **ReputationEvent** | `id`, `userId`, `type` (`test_completed`/`abandoned`/`app_published`/`fraud_flagged`/...), `delta`, `createdAt` |

詳細欄位 / index / RLS 見 `_specs/database_schema.md`（Phase B 產出）。

## 8. V1 success metrics

| Metric | Definition | Target |
|---|---|---|
| **Activation** | 註冊後 24h 內接受配對且完成第一次 install | ≥ 40% |
| **Completion** | TestRequest 進 `completed` ratio | ≥ 65% |
| **Time-to-12** | App 從 `recruiting` 到 12 個 completed testers 中位數 | ≤ 18 days |
| **K-factor** | 每 active user 帶來新註冊數 | ≥ 0.6 (V1) / ≥ 1.0 (V2) |
| **Reputation health** | 平均 reputation score 漂移 ≤ ±5%/月 | 穩定 |

## 9. Open decisions (significantly reduced after 2026-05-19)

| ID | Decision | Status |
|---|---|---|
| ~~APT-A-001~~ | Backend stack | **resolved** → Firebase (Auth/FCM/Storage) + Supabase (PG + Realtime + RLS) + Ktor (custom matching service) |
| APT-A-002 | Email login provider | open: Supabase Auth magic link vs Firebase Email link |
| APT-A-003 | Install verification 強度 | open: PackageManager local only vs +Play Integrity in V1 vs defer to V2 |
| APT-P-001 | Credit curve | draft: 1 cr / app slot, +1 cr / completed test, 首 App 免費 — 詳 `growth_and_network.md` |
| APT-P-002 | Reputation 起跳分 / 公式 | draft in `reputation_system.md` — owner sign-off needed |
| APT-OPS-001 | Domain + Play Console 帳號 | open |

## 10. Module mapping (V1)

`:feature:auth` / `:feature:onboarding` / `:feature:home` / `:feature:myapps` / `:feature:appdetail` / `:feature:testing` / `:feature:profile` / `:feature:inbox` (push center)

詳細模組 DAG + 替換策略見 `_specs/modularization.md`（Phase C 產出）。
