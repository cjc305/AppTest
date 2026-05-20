# AppTest — Onboarding UX

> **Version:** 0.1 · **Last updated:** 2026-05-19 · **Owner:** TBD
> 從點 launcher 到「我已經是 AppTest 公民」之間的 60 秒。Activation funnel 最大滲漏點 (`growth_and_network.md §4`)。

---

## 1. Activation target

> **註冊後 24h 內接受配對且完成第一次 install ≥ 40%**

不是註冊率（簡單），不是 D1 retention（落後指標），而是**第一次完成有意義動作**。

## 2. Design principles

1. **3 個選擇上限** — onboarding 任何單一畫面 ≤ 3 個選項 / ≤ 3 個輸入欄。
2. **預設都選好** — 每個步驟都有合理 default，使用者「按 next」就走完。
3. **解釋成本即時可見** — 「為什麼問這個」用 1 行說，不講廢話。
4. **權限延遲詢問** — Push permission 延到第一次配對成功後才要，不在 onboarding 內問。
5. **網路效應社會證明** — 註冊頁就秀「N dev 已加入」+ 配對成功率，建立 trust。

## 3. The full first-run flow

```
[Launch] → [Splash 800ms] → [Sign In] → [Welcome moment] →
[Step 1: Intent] → [Step 2: Category] → [Step 3: Language] →
[Done card] → [Home with empty state]
```

詳見 §4–§11。

## 4. Splash (800ms hard cap)

- 只有 logo + spinner（subtle）
- 預載 auth state，若已登入 → 直送 Home
- 永遠 ≤ 800ms（超過視為失敗，跳 fallback 畫面）

## 5. Sign-in screen

見 `wireframes.md §1`。

### Copy
- Headline: `Join 1,238 Android devs testing each other's apps`（數字 live，可從 `/stats/public` 抓 + cache 1h）
- Primary CTA: `Continue with Google`
- Secondary CTA: `Sign in with email`
- Tertiary: `Terms · Privacy`（單行，避免恐慌）

### Friction analysis
| Friction | Mitigation |
|---|---|
| Google account 選擇器卡頓 | 在 button 上加「Google」logo + tooltip「需要 ≥ 1 個 Google 帳號」 |
| Email magic link 等待 inbox | 顯示「mail 通常 ≤ 30s 到，若沒收到看垃圾信」 |
| 不信任 OAuth scope | 寫清楚「我們只讀 email + display name + photo，不讀通訊錄/檔案」 |

### Drop-off mitigation
- 點 Email button → 立即顯示輸入欄（同畫面 expand），不跳新 page
- Email 寄送後**保留同畫面**顯示「magic link 已寄到 X@…，秒數倒數 retry」

## 6. Welcome moment (between sign-in and step 1)

```
        ╔═══════════════╗
        ║   Welcome     ║
        ║   to a        ║
        ║   network of  ║
        ║   1,238 devs  ║
        ╚═══════════════╝
        (You start as Bronze ⭐
         and will earn reputation
         by testing apps)

         (Tap to continue)
```

- 顯示時間 ≥ 2s，使用者點任意處 dismiss
- 用 Spatial Layout 3 layers + subtle motion 強化「你進入了一個 network」感受
- **這 1 個畫面就解釋了 reputation + 起跳 tier**，省去後面教學

## 7. Step 1: Intent (why you're here)

```
○ Find testers for my app
● Test others' apps               ← default ✅
○ Both
```

- Default = `Test others' apps`（最低承諾 → 高完成率）
- 任何選都進得了 step 2
- Copy 在頂部：`This shapes your home feed. Change anytime.`
- 「Both」實際上 = 行為與「Test」一樣，差別只在 onboarding 結束後是否引導建立第一個 App
- **不問** Play Store URL — 等使用者真有 App 要 publish 才在 My Apps 收

## 8. Step 2: Category preference

- 多選 chips（≤ 3 row 不滾動，screen-first 設計）
- 預設選 user’s **most installed category**（從 `/apps` 第一頁推測）；如無資料則 Productivity + Tools 預打勾
- 至少 1 個必選；無法 0 個 next
- 「我什麼都測」？→ 引導點「Tools」+「Productivity」+「Game」三大 catch-all

## 9. Step 3: Language

- 預設 = system locale；只給 zh-TW / en 兩選（V1 範圍）
- 多選但只影響配對偏好，不影響介面語言（介面語言走 system）
- 1 秒可過

## 10. Done card

```
        ╔══════════════════╗
        ║  ✓ You're in!    ║
        ║                  ║
        ║  Next match in:  ║
        ║   4h 23m         ║   ← live countdown to next batch run
        ║                  ║
        ║  We'll notify    ║
        ║  you when ready  ║
        ╚══════════════════╝
        (Notify me) (Skip)     ← permission ask
```

- 倒數 = next 02:00 UTC（規則式 batch）
- **這裡才問 push permission** — 因為「等通知」是承上的自然動作
- 「Skip」會記下 user 拒絕，3 次配對後**不**再 ask（避免騷擾）

## 11. Home empty state (post-onboarding first visit)

```
"No matches yet.
 Your first match arrives in 4h 23m.

 Meanwhile, try browsing recruiting apps →"
                   [Browse apps]
```

- Browse 點下去 = 看 `/apps?category=user.primary_category`
- **不**做 fake matches 給 first-time user（破壞信任）
- 在 home 頁底部顯示「7d 內 N% 的新人在第 1 個配對內完成首測」社會證明

## 12. Permission asks (timing)

| Permission | When asked | Decline path |
|---|---|---|
| Push notification | 在 onboarding done card（§10）| 配對來時 in-app banner，不靠 push |
| `QUERY_ALL_PACKAGES`* | 第一次「我要安裝這個 App」流程 | fallback：手動 confirm install |
| `POST_NOTIFICATIONS` (Android 13+) | 同 push | 同上 |

*已預先 declare 在 manifest，runtime 拒絕後降級到「請手動 confirm install」

## 13. Copy guidelines

- **Always second-person.** "You" not "the user"
- **Active voice.** "Earn 1 credit" not "1 credit will be earned"
- **No marketing fluff.** 沒有「revolutionary / amazing / discover」這類詞
- **No jargon.** 沒有「matchmaking / algorithm / reputation」首次出現時都用解釋附帶（「我們會幫你配對 (matching) ...」）
- **Numbers > adjectives.** 「14 days, 12 testers」勝過「a few weeks, several testers」

## 14. Measurement (onboarding-only metrics)

| Metric | Target | Tool |
|---|---|---|
| Splash → Sign-in screen reached | ≥ 95% | Firebase Analytics screen_view |
| Sign-in attempted (any provider) | ≥ 80% | custom event |
| Sign-in succeeded | ≥ 90% of attempts | custom event |
| Onboarding all 3 steps complete | ≥ 85% of signed-in | custom event funnel |
| Done card → push permission granted | ≥ 60% | Android `POST_NOTIFICATIONS` event |
| First match within 24h after onboarding | ≥ 90% | derived (matching service log) |
| Activation (= first match installed) | ≥ 40% | derived |

每週 review 漏斗最大 drop 點 → 該畫面進迭代隊列。

## 15. A/B test backlog (post-V1 launch)

| Test | Hypothesis |
|---|---|
| Sign-in social proof number live vs static | live 提升 sign-in rate +5% |
| Welcome moment 文案：reputation 提一次 vs 不提 | 提一次提升 step 1 完成率 +3% |
| Permission ask 在 done card vs 第 1 次配對 | 配對後 grant rate +20%（trade-off：第一次配對前無法 push） |
| Step 2 default chip 數量（1 vs 3） | 3 個預打勾提升完成率 +5% |

## 16. Open decisions

| ID | Decision | Status |
|---|---|---|
| APT-P-020 | Onboarding 是否強制 (skip 鍵存在 vs 不存在) | default: skip 存在但選最少 default 也 ≤ 10s 完成 |
| APT-P-021 | Email magic link 在 Continue with Google 之上 vs 之下 | default: 下（Google 是主要 audience） |
| APT-P-022 | Welcome moment 顯示時間下限 | default: 2s soft，可 tap dismiss |
