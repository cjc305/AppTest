# AppTest — Database Schema (Audit / Trust / Internal)

> **Version:** 0.1 · **Last updated:** 2026-05-19 · **Owner:** TBD
> 核心 entities 在 `database_schema.md`；本檔放 audit / event / trust / lookup 表。
> Conventions 同 core 檔 §1，不重複。

---

## 1. reputation_events (append-only event log)

```sql
reputation_events {
  id                 uuid PK
  user_id            uuid not null FK→profiles.user_id
  event_type         text not null   -- test_completed|test_abandoned|app_published|fraud_flagged|streak_break|no_activity_decay
  delta              int not null    -- 可正可負
  reason             jsonb           -- breakdown: which sub-score, source ids
  related_id         uuid            -- e.g. test_request_id
  algorithm_version  text not null
  created_at         timestamptz
}
-- Indexes: (user_id, created_at desc), (event_type, created_at desc)
-- Append-only: 無 UPDATE / DELETE 權限給 anyone (不含 service role mass-delete)
```

## 2. match_runs (per batch audit)

```sql
match_runs {
  id                 uuid PK
  run_at             timestamptz not null
  algorithm_version  text not null
  candidates_evaluated int
  pairs_created      int
  avg_score, p50_score, p95_score  numeric
  weights            jsonb           -- snapshot of w1~w7 at run time
  metrics            jsonb           -- fairness audit etc.
}
-- Indexes: (run_at desc)
```

## 3. match_run_pairs (explainability)

```sql
match_run_pairs {
  id                 uuid PK
  match_run_id       uuid not null FK→match_runs.id ON DELETE CASCADE
  app_id             uuid not null
  candidate_user_id  uuid not null
  score              numeric(5,4) not null
  score_breakdown    jsonb not null  -- per-w contribution
  was_assigned       bool not null
}
-- Indexes: (match_run_id), (candidate_user_id, was_assigned)
-- 保留期 180d 後 aggregate 後刪（APT-A-005）
```

## 4. reports + moderation_decisions

```sql
reports {
  id                 uuid PK
  reporter_id        uuid not null FK→profiles.user_id
  target_type        text not null   -- user|app|test_request
  target_id          uuid not null
  reason             text not null   -- enum: fake_app|spam|abuse|inappropriate|other
  notes              text
  status             text not null default 'open'  -- open|reviewing|resolved|dismissed
  created_at         timestamptz
}
moderation_decisions {
  id                 uuid PK
  report_id          uuid FK→reports.id   -- nullable for auto-flag
  moderator_id       uuid FK→profiles.user_id  -- null = system auto
  decision           text not null   -- fraud_confirmed|false_positive|need_more_info|warning
  notes              text
  evidence           jsonb
  created_at         timestamptz
}
-- Indexes: reports(status, created_at), moderation_decisions(report_id), (moderator_id)
```

## 5. device_fingerprints (anti-sybil)

```sql
device_fingerprints {
  id                 uuid PK
  user_id            uuid not null FK→profiles.user_id ON DELETE CASCADE
  android_id_hash    text not null   -- SHA-256(android_id + salt)
  ip_hash            text            -- daily-salted SHA-256, GDPR-safe
  first_seen, last_seen  timestamptz
}
-- UNIQUE (user_id, android_id_hash)
-- Index: (android_id_hash)  -- detect same device across accounts
-- GDPR: 用戶刪帳即刪（APT-A-006）
```

## 6. notification_preferences

```sql
notification_preferences {
  user_id            uuid PK FK→profiles.user_id ON DELETE CASCADE
  fcm_token          text
  types_enabled      jsonb not null default '{"match":true,"heartbeat":true,"reputation":true}'
  updated_at         timestamptz
}
```

## 7. app_categories (lookup)

```sql
app_categories {
  code               text PK            -- 'productivity', 'game_casual', ...
  name_en, name_zh   text not null
  display_order      int
}
```

## 8. RLS for audit tables

| Table | Policy |
|---|---|
| `reputation_events` | self SELECT own; service role INSERT only; **never** UPDATE/DELETE |
| `match_runs` / `match_run_pairs` | service role only (matching engine + admin dashboard) |
| `reports` | reporter SELECT own; mod role SELECT all |
| `moderation_decisions` | mod role only |
| `device_fingerprints` | service role only |
| `notification_preferences` | self SELECT/UPDATE own |
| `app_categories` | public read (no write at runtime; seed via migration) |

## 9. Service role boundaries

只有以下系統可持 `service_role` JWT：
- Ktor matching service (寫 `match_runs`, `match_run_pairs`, `reputation_events`, `test_requests` 狀態)
- Reputation recompute job (寫 `reputation_events`, 更新 `profiles.reputation_*` 快照)
- Anti-cheat heuristics job (寫 `reports`, `moderation_decisions`)
- Supabase Edge Functions (heartbeat ingest, proof generation)

App client **絕不**持 service_role；所有寫入經 RPC / API。

## 10. Indexes summary (audit)

| Index | Table | Purpose |
|---|---|---|
| `idx_rep_events_user_created` | reputation_events | user timeline |
| `idx_match_runs_run_at` | match_runs | scheduled queries |
| `idx_match_pairs_run` | match_run_pairs | run drilldown |
| `idx_reports_open` | reports WHERE status='open' | mod queue |
| `idx_device_android_hash` | device_fingerprints | sybil detection |

## 11. Retention & deletion policy

| Table | Retention | After-action |
|---|---|---|
| `reputation_events` | indefinite (load-bearing for re-computation) | never delete; anonymize user_id on account delete |
| `match_runs` | 365d | summarize → archive bucket |
| `match_run_pairs` | 180d | aggregate stats → delete row-level |
| `reports` / `moderation_decisions` | indefinite | anonymize on account delete |
| `device_fingerprints` | until account delete | hard delete |
| `notification_preferences` | until account delete | hard delete |

GDPR `right to erasure`: 帳號刪除 → user_id 改 anonymized hash，保留行為 aggregate；明示 PII 全刪。
