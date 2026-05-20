# AppTest — Play Store Launch Strategy

> **Version:** 0.1 · **Last updated:** 2026-05-19 · **Owner:** TBD
> Track 路線 + ASO + listing copy + 種子用戶獲取 + 危機 playbook。
> CI/CD 對 Play 軌的整合見 `cicd.md §6`；冷啟戰術見 `growth_and_network.md §6`。

---

## 1. 雞生蛋問題

AppTest 自己就在解 closed test 困境 — 但 AppTest 自己也要走 closed test 才能上架。**啟動時無法用自己**。三條對策：

1. **手動 seed 12 testers** 從 dev 社群（Telegram / Discord / Reddit r/androiddev）
2. 自己團隊 + 朋友湊 5~6 個 Google 帳號 + 找 5~6 個願意幫的開發者
3. 跑 14 天 closed test → 上 Production → 然後**才**對外宣傳

預估時間：seed → closed test 4 週 → 上 Production 第 5 週。

## 2. Track 路線圖

| Stage | Track | Tester pool | 期間 | 目標 |
|---|---|---|---|---|
| **0. Internal** | Internal | 5-10 team + 朋友 | 持續（CI nightly） | 抓嚴重 bug，本身就是 daily eat-your-own-food |
| **1. Closed test (seed)** | Closed | 12 個 dev 朋友 + Telegram seeding | week 1-4 | 完成 Google 要求 12×14d |
| **2. Soft launch** | Production (limited) | 公開但低調 | week 5-8 | 跑 K-factor measurement，調 onboarding |
| **3. Public launch** | Production | 公開 + 主動 marketing | week 9+ | 達 500 註冊 dev (第一個 network density milestone) |

不走 Open track（沒 V1 必要；對外只有 Production 或 invite-only beta）。

## 3. Store listing

### Title (50 chars max)
```
AppTest: Closed Test Exchange for Android Devs
```

### Short description (80 chars max)
```
Get 12 testers in 14 days. The Android dev community testing each other's apps.
```

### Full description (4000 chars max) — outline
1. **Hook** (3 lines)：Stuck waiting for 12 testers? You're not alone.
2. **What it is** (1 段)：AppTest is a network of Android devs who test each other's apps before Play Store launch.
3. **How it works** (3 步驟條列)：Post app → get matched → install + test → both sides benefit
4. **Why it works** (4 條 benefit)：reciprocity, reputation, AI matchmaking (coming), trust
5. **Hard rules** (1 段)：No paid promotion, no fake testers, real Play Console verification
6. **Who it's for** (1 段)：Individual / small-team Android devs preparing first Play Store launch
7. **What's next** (1 段)：V2 AI matchmaking, V3 team plans
8. **Call to action**：Join the network →

### Category
- Primary: `Tools`
- Secondary: `Productivity`

不選 `Developer tools` because Play 對該分類 ranking 競爭極強。

### Tags / keywords (informal, used in description naturally)
- Android closed test
- Play Console 12 testers
- beta tester exchange
- Android developer network
- Pre-launch testing

## 4. Visual assets

| Asset | Size | What |
|---|---|---|
| App icon | 512×512 PNG (~adaptive) | 用 brand logo (待設計, APT-P-015) |
| Feature graphic | 1024×500 | "12 testers. 14 days. Done." 加 hero illustration |
| Phone screenshots × 8 | 1080×1920 | 1=Home matched feed, 2=Onboarding, 3=App detail, 4=Testing dashboard, 5=Profile tier, 6=Proof card, 7=Reputation explainer, 8=Quote-style testimonial |
| Tablet screenshots × 4 | 1920×1200 | Responsive views per `wireframes.md §6` |
| Promo video | 30s YouTube | 1=problem (15s), 2=solution flow (10s), 3=CTA (5s) |
| Localized icons | en + zh-TW | 同設計，差異僅 store listing 文案 |

## 5. ASO (App Store Optimization)

| Lever | Action |
|---|---|
| Title keywords | Include "Closed Test", "Android Devs"（自然嵌入，不堆關鍵字） |
| Short desc | Use main intent keyword (`12 testers`) |
| Long desc | 7~10 個 long-tail 自然嵌入，密度 < 2% |
| Reviews | 主動請 closed test 階段的 tester 留評，目標 4.5+ |
| Update cadence | 雙週 release 一次（Play 算法偏好高活躍）|
| Localization | en + zh-TW（V1）→ V2 加 ja, ko, id |

避免：標題堆關鍵字（Play 會降權）、買假評論（自殺）。

## 6. Launch day comms

| Channel | Content | Timing |
|---|---|---|
| Product Hunt | Launch post + 24h response | Tuesday 00:01 PST |
| Hacker News Show HN | `Show HN: AppTest – swap closed-test installs with other Android devs` | 同日 09:00 EST |
| Reddit r/androiddev | 1 post + AMA | 同週稍後（避免同日 burnout） |
| Twitter / X | thread + GIF demo | launch day |
| Telegram / Discord (Android dev 群) | 私訊 admin 求預先合作 | T-1 week |
| Personal network | LinkedIn post + email beta tester | launch day |
| Blog post | 1 long-form "why we built this" | T+3 days |

**不**走付費廣告（V1 沒 budget + 也想驗 organic 是否能跑）。

## 7. Day-1 metrics dashboard

- DAU / WAU realtime
- Signup conversion (visit → install → signup) — Play Console
- Funnel: signup → first match → first install → D1 retention — Firebase
- K-factor (per 100 signups, how many invited new sign-ups) — custom
- Crash-free rate ≥ 99.5% — Crashlytics
- Report rate (per 1k users) — backend DB

If any metric red on day 1：直接 holds + 開戰情室。

## 8. 危機 playbook

| Crisis | First response (< 1h) | Mitigation |
|---|---|---|
| **Google takedown** (e.g., policy 違反) | 內部 review takedown letter | appeal Play Console + reduce surface to comply |
| **Mass refund / lawsuit** | freeze new sign-ups + 公告 | 退費 (V3 才有付費，V1 N/A) + 法務 |
| **Schema/migration broken in prod** | rollback per `monorepo.md §migrations` | hotfix path |
| **Mass fraud event** | freeze matching + manual moderation | 動用 `anti_cheat.md` V2 ML 緊急上線 |
| **Backend down** (Supabase outage) | client 降級 read-only | Status page + 公告，等 Supabase 恢復 |
| **Bad press / viral negative thread** | 1 個官方回應在原 thread + blog post | 高層親自 reply、不刪文 |

## 9. 法規 / 政策合規 checklist

| Requirement | Status / Action |
|---|---|
| Privacy Policy | `_specs/privacy_policy.md` (TBD, Phase H+) — hosted on apptest.dev/privacy |
| ToS | `_specs/tos.md` (TBD) — hosted on apptest.dev/terms |
| Data Safety form (Play Console) | accurate per actual collection (email + name + photo + analytics) |
| Permission justification | `QUERY_ALL_PACKAGES` 需 in-app rationale + Play Console explanation |
| Age rating | 12+ (assumes Android dev = ≥ 12 yr old) |
| GDPR right to erasure | implemented per `database_schema.md §11` |
| App Links assetlinks | hosted at apptest.dev/.well-known/assetlinks.json (per `navigation.md §6`) |

## 10. Pre-launch checklist (≤ 7 days before)

- [ ] 12 closed-test slots filled
- [ ] All 8 screenshots final + 1 promo video
- [ ] Privacy policy + ToS live on apptest.dev
- [ ] Backend production deployed + load test pass
- [ ] Crashlytics + Analytics 接通
- [ ] 媒體連絡列（Telegram / Reddit / HN account warmed up）
- [ ] Customer support channel (email support@apptest.dev) live
- [ ] On-call rotation 排定
- [ ] Status page (status.apptest.dev) up
- [ ] App signed with release keystore + Play App Signing 啟用

## 11. Post-launch review cadence

| Cadence | Review |
|---|---|
| Day 1 | War room: any P0 ⇒ 即修 |
| Day 7 | Funnel review，調整 onboarding 文案/順序 |
| Day 30 | K-factor + retention 第一次月度 review |
| Day 90 | V1 PMF 評估 → V2 開工決策 |

## 12. Open decisions

| ID | Decision | Status |
|---|---|---|
| APT-OPS-009 | Launch 地域 (zh-TW only first vs en+zh-TW 同步) | default: en+zh-TW 同步 |
| APT-OPS-010 | 首發版本是否引入 referral code system | default: V1 後加，先驗 organic |
| APT-OPS-011 | 是否申請 Google Play Indie Games / Best of Play 之類獎項 | default: V2 達 PMF 後評估 |
