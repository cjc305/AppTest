# AppTest — AI Matchmaking

> **Version:** 0.1 · **Last updated:** 2026-05-19 · **Owner:** TBD · **Pillar:** AI Matchmaking + Network Effect
> 從可解釋規則 (V1) 走到可學習模型 (V2/V3)。任何 ML 版本都保留 V1 規則作 fallback + ground truth。

---

## 1. Problem definition

雙邊配對：把「Apps with open slots」與「Available testers」配成 pair。
- **App 期望：** 在 ≤ 18 天內補滿 12 testers，且 ≥ 65% 完成率。
- **Tester 期望：** 拿到有興趣 / 不重複 / 不太重的 App 安裝任務。
- **平台期望：** 最大化 platform-wide completion rate × distribution fairness。

不是「最快配對」是「最好配對」 — 容許 24h 延遲換品質。

## 2. V1 — rule-based scoring (cron-driven batch)

### 2.1 Run cadence
每天 02:00 UTC 跑一次 batch matching job (cron via Supabase scheduled function 或 Ktor scheduler)。

### 2.2 Candidate set
對每個 status=`recruiting` 的 App：
- Pool = 所有 7d 內 active 的 user，**且** 該 user 對該 App 沒過 active/completed TestRequest。
- 排除：該 App owner 自己 / 已 active 並行 ≥ 5 個 TestRequest 的 user / 被 fraud_flag 的 user。

### 2.3 Compatibility score (per candidate × App)
```
score(user, app) =
    w1 * category_affinity(user, app)        // 0~1
  + w2 * language_overlap(user, app)         // 0~1
  + w3 * reputation_weight(user)             // 0~1, normalized from §reputation_system
  + w4 * past_completion_rate(user)          // 0~1
  - w5 * fatigue_penalty(user)               // 0~1, 近 7d 接過 N 個任務的衰減
  + w6 * timezone_match(user, app.owner)     // 0~1
  + w7 * newcomer_boost(user)                // 0~1, 前 3 次配對 +0.5
```

V1 起始權重 (sum = 1.0)：
```yaml
w1: 0.25  # category
w2: 0.15  # language
w3: 0.20  # reputation
w4: 0.20  # past completion
w5: 0.10  # fatigue penalty
w6: 0.05  # timezone
w7: 0.05  # newcomer boost
```

權重存 `match_algorithm_config` table，可不重 deploy 動態調。

### 2.4 Assignment algorithm
- 對每個 App 取 top-K (K = remaining_slots × 2) 候選。
- 用 greedy + tie-break (隨機種子 = `runId`) 把 candidates 分配 → 避免單一 high-reputation tester 被多 App 搶。
- Hard constraint: **單一 tester 每 batch run 最多被指派 1 個新任務**（避免一次塞爆）。

### 2.5 Output
- 寫 N 筆 TestRequest (status=`matched`) + 觸發 FCM push notification。
- 寫 1 筆 MatchRun audit 列：`{ runId, runAt, algorithmVersion, pairsCreated, candidatesEvaluated, avgScore, p50/p95 score }`。

## 3. Fairness constraints (V1 hard rules)

- **No-rich-get-richer:** 每個 App 在單個 batch 最多收到 `min(remaining_slots, 4)` 新 matches → 強制要連多日才補滿，避免 1 天爆紅就吃光池子。
- **New-user safety net:** Newcomer tier 用戶的 newcomer_boost 確保前 3 次配對成功率 ≥ 80%。
- **Anti-monoculture:** 連 3 batch 都 match 到同一 owner 的 user → 強制 cool-down 1 batch。

## 4. Cold-start

- **新 user：** category preference 從 onboarding 採集（必選 1 個 primary + 0~3 個 secondary）。
- **新 App：** 第一個 batch 給予 +0.2 score boost（鼓勵嘗試），第 2 個 batch 後恢復。
- **新平台：** 啟動期手動 seed 20~50 個 dev（見 `growth_and_network.md` §acquisition）。

## 5. V2 — ML upgrade

V1 公式不刪，併存：
- **Embeddings:** 訓練 user embeddings (testing history) + app embeddings (category / description text)。
- **Ranker model:** GBDT (CatBoost / LightGBM) — input: V1 feature + embedding cosine + LLM-rated description quality。Output: 預測 completion probability。
- **Hybrid:** `final_score = α * v1_score + (1-α) * ml_score`，α 從 1.0 開始 ramp down → A/B 測試。
- **Online learning:** 每天 retrain，weekly 評估 hold-out test completion lift。

**Inference cost budget:** 每 batch run ≤ 60s wall clock，≤ 1k DB queries。Embeddings 用 nightly precompute + cache。

## 6. V3 — pairwise RL + global

- 跨地域 / 語言矩陣優化（明確 fairness：每 timezone 區段 completion rate gap ≤ 10%）。
- 加 contextual bandit：對「沒 ground truth 的新配對」做受控探索。
- 引入 hard / soft 政策：如「個人 dev 優先配個人 dev，避免被企業 dev 吃光池子」。

## 7. Observability (mandatory from V1)

每次 MatchRun 必須能回答：
1. 為什麼 user X 沒被配對到 App Y？(score 各維度拆解可查)
2. 平均 / p95 / p99 score 分布走勢
3. Fairness：各 tier 分到的配對數比例
4. 假設替換權重 (w1=0.30, w2=0.10 ...) 重跑 dry-run 結果差多少 (counterfactual)

`MatchRun.metricsJson` 必填以上 4 類。

## 8. Failure modes

| Failure | 偵測 | 復原 |
|---|---|---|
| 某 App 連 7 batch 0 candidate | metrics alert | 自動降權重 / 通知 owner 調整門檻 |
| ML model 預測準度跌破 V1 baseline | A/B + offline eval | 自動 fallback α=1.0 (pure V1) |
| Batch job 失敗 | retry 3 次 + on-call alert | 手動 trigger 補跑 |
| 配對偏差 (某 tier 拿不到任務) | weekly fairness audit | 調 newcomer_boost / 加常數補償 |

## 9. API surface

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/internal/match/runs?from=&to=` | 列 MatchRun |
| `GET` | `/internal/match/runs/:runId` | 該 run 詳情 + metrics |
| `POST` | `/internal/match/runs/dry-run` | 試跑（不寫資料） |
| `GET` | `/me/matches/recent` | 我最近的配對及理由 |
| `PATCH` | `/internal/match/config` | 動態調權重（audit log） |

## 10. Open decisions

| ID | Decision | Status |
|---|---|---|
| APT-A-004 | V2 ML training infra: hosted (Vertex AI) vs self (Modal/Cog) | open |
| APT-P-010 | 是否對 tester 顯示「配對理由」（可解釋性） | draft: 顯示 top 3 contributing factors |
| APT-P-011 | Batch 頻率 (1×/day vs 2×/day) | default 1×/day; 監控延遲再加 |
