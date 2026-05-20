# AppTest — Database Schema (Core Entities)

> **Version:** 0.2 (split) · **Last updated:** 2026-05-19 · **Owner:** TBD
> 核心 V1 entities + conventions + RLS。Audit / 信任 / 通知 / lookup 表見 `database_schema_audit.md`。

---

## 1. Conventions

- DB: **PostgreSQL 15+** (Supabase managed)
- Auth: Supabase Auth 自管 `auth.users` + `auth.sessions`；本 schema 用 `profiles.user_id` 1:1 連結
- PK: `id uuid default gen_random_uuid()`（除 enum lookup 表）
- Timestamps: `created_at timestamptz default now()`；常用 `updated_at` 由 trigger 自動更新
- Soft delete: 加 `deleted_at timestamptz` + view 過濾；**不**真刪
- Audit / event tables: append-only，**永不 UPDATE / DELETE**
- All FK 加 `ON DELETE` 明示行為（cascade vs restrict）
- 所有公開讀寫表 enable RLS（見 §7）
- Migration 命名: `YYYYMMDDHHMMSS_<slug>.sql`（Supabase CLI）

## 2. profiles (= app-side user)

```sql
profiles {
  user_id            uuid PK FK→auth.users.id ON DELETE CASCADE
  display_name       text not null check (length between 2 and 40)
  photo_url          text
  locale             text not null default 'en'      -- 'zh-TW' | 'en'
  reputation_score   int not null default 300        -- snapshot, recomputed nightly
  reputation_tier    text not null default 'Bronze'  -- Newcomer|Bronze|Silver|Gold|Platinum
  credits            int not null default 1          -- 註冊送 1
  primary_category   text                            -- onboarding 選的
  preferred_categories text[]                        -- secondary, ≤ 3
  algorithm_version  text not null default 'v1'      -- 重算錨點
  created_at, last_active_at  timestamptz
}
-- Indexes: (reputation_score desc), (last_active_at desc), unique(user_id)
```

## 3. apps

```sql
apps {
  id                 uuid PK
  owner_id           uuid not null FK→profiles.user_id ON DELETE CASCADE
  package_name       text not null unique             -- e.g. com.example.myapp
  name               text not null check (length ≤ 50)
  description        text not null check (length ≤ 500)
  icon_url           text not null
  category           text not null                   -- FK→app_categories.code
  play_opt_in_url    text not null                   -- host=play.google.com (trigger 驗)
  required_testers   int not null default 12 check (between 1 and 100)
  required_days      int not null default 14 check (between 7 and 30)
  status             text not null default 'recruiting'  -- recruiting|active|completed|paused
  paused_at, completed_at  timestamptz
  created_at, updated_at, deleted_at  timestamptz
}
-- Indexes: (status, created_at desc), (owner_id), (category, status)
```

## 4. test_requests

```sql
test_requests {
  id                 uuid PK
  app_id             uuid not null FK→apps.id ON DELETE CASCADE
  tester_id          uuid not null FK→profiles.user_id ON DELETE CASCADE
  status             text not null default 'matched'   -- matched|installed|active|completed|abandoned
  match_run_id       uuid FK→match_runs.id
  match_score        numeric(5,4)                      -- explainability snapshot
  matched_at         timestamptz default now()
  installed_at, last_heartbeat_at, completed_at, abandoned_at  timestamptz
  days_active        int not null default 0
  abandon_reason     text
}
-- UNIQUE (app_id, tester_id)
-- Indexes: (app_id, status), (tester_id, status), (status, last_heartbeat_at asc)
```

## 5. heartbeats (per day, per request)

```sql
heartbeats {
  id                 uuid PK
  test_request_id    uuid not null FK→test_requests.id ON DELETE CASCADE
  date_utc           date not null
  ping_count         int not null default 1
  package_present    bool not null                    -- PackageManager 檢查結果
  launch_event_count int not null default 0
  client_meta        jsonb                            -- device/os/locale snapshot
  created_at         timestamptz
}
-- UNIQUE (test_request_id, date_utc)  -- 強制每日冪等
-- Indexes: (test_request_id, date_utc desc)
```

## 6. proofs (completion 證明卡)

```sql
proofs {
  id                 uuid PK
  test_request_id    uuid not null unique FK→test_requests.id
  proof_type         text not null     -- auto (system) | screenshot (user-submitted)
  asset_url          text not null     -- Firebase Storage path
  signature          text not null     -- server-side HMAC of canonical payload
  verify_url         text not null     -- public /verify/:id endpoint
  created_at         timestamptz
}
```

## 7. Row-Level Security (RLS) overview

| Table | Policy summary |
|---|---|
| `profiles` | self SELECT/UPDATE own; others SELECT name/photo/tier/locale only (mask `reputation_score`, `credits`) |
| `apps` | authenticated SELECT WHERE status=recruiting; owner SELECT/UPDATE/DELETE own |
| `test_requests` | tester sees own; app owner sees own App's requests; nobody else |
| `heartbeats` | service role only (write via Edge Function with JWT verify) |
| `proofs` | anyone with id SELECT (公開 verify); service role INSERT |
| Audit tables (`database_schema_audit.md`) | service / mod role only — see that file §6 |

Service role = Supabase `service_role` JWT；僅 Ktor 後端與 Edge Functions 持有。**禁止**內嵌 client。

## 8. Migrations (operational)

- 一律 forward-only；rollback 寫對應 down migration
- 不可破壞性變更（drop / rename column）：先 add new → backfill → switch reads → 兩個 release 後 drop
- Production 先 staging 跑 24h smoke 再 apply
- 每次 migration 帶 PR description: `schema delta + RLS impact + estimated lock time`

## 9. Open decisions

| ID | Decision | Status |
|---|---|---|
| APT-A-005 | `match_run_pairs` 保留期 (90d? 180d?) | draft: 180d 後 aggregate 後刪明細 |
| APT-A-006 | `device_fingerprints` 保留 + GDPR 刪除權實作 | draft: 用戶刪帳即刪 |
| APT-P-013 | `reputation_score` 對其他 user 可見否 | default: 否，只曝 tier |
