# AppTest — Reputation System

> **Version:** 0.1 · **Last updated:** 2026-05-19 · **Owner:** TBD · **Pillar:** Trust + Network Effect
> 用最少訊號做出最大區辨力。V1 公式可解釋；V2 加 ML 增強，但 V1 公式永遠保留作為 fallback。

---

## 1. What reputation means

每個 user 一個 **0~1000 整數分**。對外顯示 tier，不顯示精確分（產品決策 APT-P-003）：

| Tier | 分數區間 | 初始族群佔比目標 |
|---|---|---|
| **Platinum** | 800~1000 | 5% |
| **Gold** | 600~799 | 20% |
| **Silver** | 400~599 | 45% |
| **Bronze** | 200~399 | 25% |
| **Newcomer** | 0~199 | 5% (新註冊 7 天內) |

初始分 = **300** (中段 Bronze)。這保證新用戶有點被配對的機會，但不過於有利。

## 2. V1 公式 (rule-based, fully explainable)

```
score = clamp(0, 1000,
    300                              // baseline
  + 40 * completion_rate_score       // 0~40, 見 §3
  + 20 * streak_score                // 0~20
  + 15 * volume_score                // 0~15
  - 50 * abandonment_score           // 0~50 penalty
  - 100 * fraud_flag_score           // 0~100 penalty (heavy)
  + 25 * publish_score               // 0~25 (有出 App 比純 leech 高分)
  + decay_adjustment                 // ±X，見 §4
)
```

權重總和 (positive) = 100，加 baseline 300 → 最高 400 純行為分。剩下 600 分要靠時間累積 (decay 邏輯 §4) — 故意設計成「不能一週衝頂」。

### 3. 各 sub-score 計算

| Sub-score | 公式 | Floor / Ceil | 直覺 |
|---|---|---|---|
| `completion_rate_score` | `min(1.0, completed_tests / max(5, total_tests))` × 40 | 0~40 | 要至少有 5 次測試樣本才滿分 |
| `streak_score` | `min(20, current_completed_streak * 4)` | 0~20 | 連 5 次完成 → 滿 |
| `volume_score` | `log10(1 + total_completed_tests) * 5` | 0~15 | 對數，避免 farm |
| `abandonment_score` | `(abandoned_in_last_90d / max(1, tests_in_last_90d))` × 50 | 0~50 | 近 90 天棄測比例 |
| `fraud_flag_score` | 1 if 過去 180d 有 confirmed fraud else 0 → ×100 | 0/100 | binary; 復原靠 §4 decay |
| `publish_score` | `min(25, published_apps_count * 5)` | 0~25 | 鼓勵雙邊角色 |

## 4. Decay model

訊號**會老**。每天背景 job 跑：
- **Positive signals 衰減：** `streak_score`、`completion_rate_score` 中過去 180 天的測試樣本權重 = `0.5 ^ (age_days / 180)`（半衰期 180 天）。
- **Negative signals 復原：** `fraud_flag_score` 在 180 天後減半，360 天後歸 0（前提期間無新 flag）。
- **No-activity penalty:** 連 60 天 0 測試 → 每天 -1 分，最多扣到 300 baseline，重新活躍即停扣。

決策：**衰減算法版本化** (`reputation_algorithm_version`)，重算可重現；歷史不變更 (write-only event log)。

## 5. Signal source (where each number comes from)

| Signal | 寫入時機 | 寫入者 | 防作弊 |
|---|---|---|---|
| `test_completed` event | TestRequest status `→ completed` | matching service | 14d heartbeat 驗證 (see `anti_cheat.md`) |
| `test_abandoned` event | status `→ abandoned` | heartbeat job (uninstall 24h grace 過) | 同上 |
| `app_published` event | App.status `→ recruiting` 首次 | API on `POST /apps` | dedup by packageName |
| `fraud_flagged` event | 人工或自動判定 | moderation service | append-only |
| `streak_break` event | 連續完成被打斷 | scheduled job | 推導 |

所有訊號 → 寫入 `reputation_events` table → 每日 job recompute → 更新 `users.reputation` 快照欄位。**事件來源不刪**。

## 6. Anti-gaming (V1)

| 攻擊面 | V1 對策 |
|---|---|
| 多帳號互刷完成 | (1) email + Google account dedup (2) packageName 唯一 (3) 同裝置（Android ID 雜湊）24h 內 1 配對上限 |
| Bot 假完成 | (1) WorkManager local heartbeat 必須對應已安裝 package (2) 1 帳號最多並行 5 active TestRequests |
| 故意棄測拉低對手分 | abandonment 只計**自己**分，不影響對方 |
| 賺完棄帳 | reputation 不可轉移；新帳號從 Newcomer 跑起 |
| 共謀環 (collusion ring) | V1 用 graph anomaly heuristic（同小群人重複互測 > 閾值 → flag）；V2 由 ML 接手 |

完整列表見 `anti_cheat.md`。

## 7. API surface (Reputation-related)

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/me/reputation` | 自己看：分數 + tier + 各 sub-score 拆解 |
| `GET` | `/users/:id/reputation` | 公開看：tier only (no number) |
| `GET` | `/me/reputation/history?from=&to=` | 自己看：events timeline |
| `POST` | `/internal/reputation/recompute/:userId` | 服務端 only：強制重算 |

詳細 schema 見 `api_contracts.md` (Phase B)。

## 8. Display rules (UI)

- 任何公開列表（App detail 顯示 owner / matching 候選 / tester roster）只顯示 tier icon。
- 自己的 profile 顯示分數 + 各 sub-score 拆解 + 過去 30d 趨勢。
- **永遠不**對外公開「過去測過哪些 App」清單（hard rule §5 of product_architecture）。
- Tier 升降時觸發 in-app celebration + push（這是 retention hook）。

## 9. V2 ML upgrade path

V1 公式保留作 fallback。V2 加上：
- **ML adjustment layer:** GBDT 看 user 過去行為 → 給出 ±100 校正分（可開關，可審計）。
- **LLM reputation summary:** 用 LLM 總結「這位 tester 通常會：仔細測試 + 留中肯評論」這類自然語言敘述，給 dev 看（不影響數字）。
- **Confidence interval:** 顯示「分數 ± N」表達樣本不足。

## 10. Open decisions

| ID | Decision | Status |
|---|---|---|
| APT-P-002 | V1 baseline 公式 sign-off | draft, owner review needed |
| APT-P-008 | 是否在 V1 公開「過去 30d 完成 N 次」aggregate stat | 默認: 不顯示 |
| APT-P-009 | Newcomer protection: 前 N 次配對給高 priority? | 默認: 前 3 次 +50 配對分 |
