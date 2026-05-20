# AppTest — API Contracts (REST + WebSocket)

> **Version:** 0.1 · **Last updated:** 2026-05-19 · **Owner:** TBD
> 客戶端與後端的合約。所有 entity 名稱對齊 `database_schema.md`。

---

## 1. Conventions

- **Base URL:** `https://api.apptest.dev/v1` (Ktor) · `wss://<project>.supabase.co/realtime/v1` (Supabase Realtime)
- **Auth:** Supabase Auth JWT in `Authorization: Bearer <jwt>`；service-role JWT 僅後端持有
- **Content-Type:** `application/json; charset=utf-8`
- **Envelope (success):** `{ "data": ..., "meta": { "request_id": "...", "now": "..." } }`
- **Envelope (error):** `{ "error": { "code": "...", "message": "...", "hint": "...", "field": "..." } }`
- **Pagination:** `?page=1&size=20` → response `data + meta.pagination: { page, size, total, has_next }`
- **Idempotency:** `Idempotency-Key: <uuid>` header on `POST /tests/:id/heartbeat` (其他 POST 視需要)
- **Versioning:** path-based `/v1/...`；breaking change = `/v2/...`，舊版 SLA ≥ 6 個月
- **Rate limit:** 60 req/min/user (default)，超過 429 + `Retry-After`
- **Timestamps:** ISO 8601 UTC，無時區後綴一律當 UTC

## 2. Error codes (canonical)

| `code` | HTTP | 何時 |
|---|---:|---|
| `auth.unauthenticated` | 401 | 無 JWT 或過期 |
| `auth.forbidden` | 403 | JWT 有但無權 |
| `validation.failed` | 400 | 欄位驗證錯（`field` 標哪欄） |
| `resource.not_found` | 404 | 找不到 |
| `resource.conflict` | 409 | unique 衝突 / 狀態不合 |
| `quota.exhausted` | 402 | credits 不足 |
| `rate.limited` | 429 | 過頻 |
| `server.error` | 500 | 未分類錯 |

## 3. REST — Auth

| Method | Path | Auth | Purpose |
|---|---|---|---|
| `POST` | `/auth/google` | none | 接 Google ID token → 換 Supabase session |
| `POST` | `/auth/email/magic-link/request` | none | 觸發 email 寄送 |
| `POST` | `/auth/email/magic-link/verify` | none | 用 token 換 session |
| `POST` | `/auth/refresh` | refresh JWT | 換新 access JWT |
| `DELETE` | `/auth/session` | yes | 登出（撤銷 refresh） |

## 4. REST — Profile (me + others)

| Method | Path | Auth | Purpose |
|---|---|---|---|
| `GET` | `/me` | yes | 自己完整 profile + credits + reputation_score + tier |
| `PATCH` | `/me` | yes | display_name / locale / preferred_categories |
| `DELETE` | `/me` | yes | 帳號刪除（連動刪 profile 但 events 保 anonymized） |
| `GET` | `/users/:id` | yes | 公開資料：name / photo / tier / locale only |
| `GET` | `/me/reputation` | yes | 含 sub-score 拆解 + algorithm_version |
| `GET` | `/me/reputation/history?from=&to=` | yes | events timeline |
| `PATCH` | `/me/notifications` | yes | fcm_token / types_enabled |

## 5. REST — Apps (developer side)

| Method | Path | Auth | Purpose |
|---|---|---|---|
| `GET` | `/apps?category=&page=&size=` | yes | recruiting App 公開列表（**不**用於配對；瀏覽用） |
| `GET` | `/apps/:id` | yes | App detail + 統計（current_testers, completion_rate） |
| `POST` | `/apps` | yes | 建立（扣 1 credit；首 App 免費）→ `{ data: app }` 或 `402 quota.exhausted` |
| `PATCH` | `/apps/:id` | owner | 更新 description / icon / required_*（status=recruiting 才可改門檻） |
| `POST` | `/apps/:id/pause` | owner | recruiting→paused |
| `POST` | `/apps/:id/resume` | owner | paused→recruiting |
| `DELETE` | `/apps/:id` | owner | soft-delete |
| `GET` | `/me/apps` | yes | 自己擁有的 Apps（含 draft / paused） |
| `GET` | `/apps/:id/testers` | owner | 目前在測 tester roster（含進度） |

## 6. REST — TestRequests (tester side)

| Method | Path | Auth | Purpose |
|---|---|---|---|
| `GET` | `/me/tests?status=` | yes | 我的 active / completed / abandoned |
| `GET` | `/me/tests/:id` | participant | 單筆 + heartbeat 進度 + proof |
| `POST` | `/me/tests/:id/confirm-install` | tester | 從 Play Store 回來後標記已安裝 → matched→installed→active |
| `POST` | `/me/tests/:id/heartbeat` | tester | 客戶端每日呼叫（**`Idempotency-Key` 必填**） |
| `POST` | `/me/tests/:id/abandon` | tester | 主動退出（不扣分但記 reason） |
| `POST` | `/me/tests/:id/proof` | tester | 上傳 screenshot proof（可選，自動 proof 已 default） |

## 7. REST — Matching (mostly internal)

| Method | Path | Auth | Purpose |
|---|---|---|---|
| `GET` | `/me/matches/recent` | yes | 我近 30d 收到的配對及理由 (top-3 contributing factors) |
| `GET` | `/internal/match/runs?from=` | service | runs 列表 |
| `GET` | `/internal/match/runs/:id` | service | run 詳情 + metrics |
| `POST` | `/internal/match/runs/dry-run` | service | 試跑 |
| `PATCH` | `/internal/match/config` | service | 調權重 (audit log 自動) |

## 8. REST — Reports & Moderation

| Method | Path | Auth | Purpose |
|---|---|---|---|
| `POST` | `/me/reports` | yes | 檢舉 user / App / TestRequest |
| `GET` | `/me/reports` | yes | 我送過的檢舉 + 處理狀態 |
| `GET` | `/internal/mod/queue?status=open` | mod | 待審 |
| `POST` | `/internal/mod/decisions` | mod | 給判決 |
| `POST` | `/me/appeals` | flagged user | 提申訴 |
| `GET` | `/me/appeals/:id` | flagged user | 申訴狀態 |

## 9. REST — Public proof verify

| Method | Path | Auth | Purpose |
|---|---|---|---|
| `GET` | `/verify/:proof_id` | none | 公開驗證（給 Play Console 審查員或他人看） |

## 10. WebSocket / Realtime channels (Supabase Realtime)

訂閱透過 Supabase JS / Kotlin SDK；客戶端用 JWT 訂閱以套 RLS。

| Channel | Filter | Events | Purpose |
|---|---|---|---|
| `profiles:me` | `user_id=eq.<my>` | UPDATE | 即時看到 reputation / credits / tier 變動 |
| `test_requests:me` | `tester_id=eq.<my>` | INSERT/UPDATE | 收新配對 + 狀態變動 |
| `apps:owned` | `owner_id=eq.<my>` | UPDATE | 自己 App 的 current_testers / status 變化 |
| `notifications:me` | (broadcast channel) | broadcast | in-app inbox 即時 push（FCM 之外的補強）|

WebSocket failure mode: 自動 fallback poll `/me/tests` 每 60s。

## 11. Push Notification (FCM topics)

不走 user-level token-by-token，用 topic + filter：
- Topic `match_<user_id_hash>` — 給該 user 的新配對
- Topic `heartbeat_<user_id_hash>` — 每日提醒未 ping
- Topic `reputation_<user_id_hash>` — tier 升級慶祝
- Payload: `{ "type": "...", "deep_link": "apptest://...", "title": "...", "body": "..." }`

## 12. Idempotency 保證

| Operation | 保證機制 |
|---|---|
| `POST /me/tests/:id/heartbeat` | `Idempotency-Key` + DB unique(test_request_id, date_utc) |
| `POST /apps` | `Idempotency-Key` 建議；DB unique(package_name) 兜底 |
| `POST /me/tests/:id/confirm-install` | DB check status from `matched` only；重複呼叫返 409 + 既有狀態 |
| `POST /me/reports` | `Idempotency-Key`；同 reporter+target+reason in 24h 視同重複 |

## 13. Open decisions

| ID | Decision | Status |
|---|---|---|
| APT-A-007 | API gateway: Ktor only vs +Supabase Edge Functions for thin endpoints | draft: Ktor only，避免兩處維護 |
| APT-A-008 | WebSocket fallback poll 頻率 (60s vs 30s) | default 60s |
| APT-A-009 | Verify URL 是否需 captcha 防爬 | open |
| APT-P-014 | `/users/:id` 是否曝 `created_at`（影響「資深度」訊號） | default: 曝（增加信任）|
