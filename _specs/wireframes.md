# AppTest — V1 Wireframes (Tester Journey)

> **Version:** 0.2 (split) · **Last updated:** 2026-05-19 · **Owner:** TBD
> ASCII low-fi wireframes，標 layout / 內容意圖 / 互動。Visual style 由 `design_system.md` 決定。
> Dev-side (My Apps / Editor / Profile / Proof Card) 見 `wireframes_dev.md`。
> Onboarding flow 細節在 `onboarding_ux.md`。

---

## Legend

```
[X] = icon · (X) = button · {X} = chip · <X> = input · ▾ = collapsible
…   = content overflow indicator  ◐ = loading state placeholder
```

---

## 1. Sign In

```
┌────────────────────────────────────────┐
│                                        │
│        ╔═════════════╗                 │  Hero illustration (spatial layer 0)
│        ║  AppTest    ║                 │  app logo + tagline
│        ╚═════════════╝                 │
│                                        │
│   Join 1,238 Android devs              │  social proof (real-time counter)
│   testing each other's apps            │
│                                        │
│   ┌──────────────────────────────────┐ │
│   │ [G]  Continue with Google        │ │  Primary CTA (filled, expressive)
│   └──────────────────────────────────┘ │
│   ┌──────────────────────────────────┐ │
│   │ [✉]  Sign in with email          │ │  Secondary CTA (tonal)
│   └──────────────────────────────────┘ │
│                                        │
│   By continuing, you accept            │  small print
│   Terms · Privacy                      │
└────────────────────────────────────────┘
```

Email flow → email field → magic-link 已寄出 confirmation 畫面。

---

## 2. Onboarding (3-step wizard, see `onboarding_ux.md` for copy)

```
┌─ Step 1 of 3 ───────── (Skip) ─┐   progress + skip allowed
│                                │
│  Why are you here?             │   headlineMedium
│  ○ Find testers for my app     │
│  ● Test others' apps           │   default
│  ○ Both                        │
│  ────────────────────────────  │
│                                │
│  Pick categories you'll test:  │   Step 2 (multi-select chips)
│  {Productivity} {Game} {Tools} │
│  {Health} {Finance} {Photo}…   │
│  ◯ at least 1 required         │
│  ────────────────────────────  │
│                                │
│  Language preferences:         │   Step 3
│  ☑ zh-TW  ☐ en  ☐ ja           │
│                                │
│         (Continue →)           │
└────────────────────────────────┘
```

實作為 3 separate screens with shared `OnboardingScaffold` (progress bar top, CTA bottom)。

---

## 3. Home (matched feed — daily entry)

```
┌─ AppTest ──────────────────[👤]┐   AppTopBar Large (collapsible)
│                                │
│ Hi, Alice  · Silver⭐· 4 cr   │   greeting + tier + credits
│                                │
│ ───────────── Today ───────── │
│ ┌─ ★ New match ─────────────┐  │   Hero card (elevation 2)
│ │ NoteFlash · Productivity  │  │
│ │ "Test this Pomodoro app"  │  │
│ │ 7 testers needed · D+0    │  │
│ │           [Skip]  (Join)  │  │
│ └────────────────────────────┘  │
│                                │
│ ▾ Active tests (3)             │   collapsible section
│  ┌ App A    Day 5/14   ●●●○○ ┐ │   mini progress
│  ┌ App B    Day 12/14  ●●●●○ ┐ │
│  ┌ App C    Day 1/14   ●○○○○ ┐ │
│                                │
│ ▾ Your apps (2)                │   shortcut to dev side
│  ┌ MyApp1  8/12 testers ●●●● ┐ │
│  ┌ MyApp2  recruiting…       ┐ │
│                                │
└───────[Home][Apps][Tests][Me]──┘   AppBottomBar (glass surface)
```

Pull-to-refresh; empty state: `"No matches yet — next batch at 02:00 UTC"`。

---

## 4. App Detail (conversion point)

```
┌─ ← ─────────────────── [Share]┐
│  ┌──────┐  NoteFlash           │   icon + name + category
│  │ icon │  Productivity        │
│  └──────┘  by Alex · Gold⭐    │   owner display (tier only, name link disabled)
│  ────────────────────────────  │
│  ┌───┐ ┌───┐ ┌───┐ ┌───┐       │   screenshots carousel
│  └───┘ └───┘ └───┘ └───┘       │
│  ────────────────────────────  │
│  "A Pomodoro timer that…"      │   3-line + Read more
│                                │
│  Requirements                  │
│  • 14 days · 12 testers needed │
│  • Daily: 1 launch + 10 min    │
│  • Currently: 5/12 testers     │   transparency (current_testers)
│                                │
│  Why you got this match        │   V1 explainability (top-3)
│  • Category match (Productivity)│
│  • Your tier: Silver (high)    │
│  • Similar timezone            │
│                                │
│  ┌──────────────────────────┐  │
│  │   Join test (1 credit)   │  │   Primary CTA, sticky bottom
│  └──────────────────────────┘  │   → opens Play Store opt-in URL
│  (Maybe later)                 │
└────────────────────────────────┘
```

---

## 5. Testing Dashboard (tester progress)

```
┌─ My tests ─{Active}{Done}{All}┐   filter chip row
│                                │
│ Active (3)                     │
│ ┌ App A · Day 5/14 ●●●○○      ┐│
│ │ Last ping: 2h ago  ✓        │
│ │ [Open app] [Mark abandoned]  │
│ └─────────────────────────────┘
│ ┌ App B · Day 12/14 ●●●●○ 🔥  ┐│   streak badge near completion
│ │ Last ping: 1d ago  ⚠        │   warning: needs ping today
│ │ [Heartbeat now] [Open app]   │
│ └─────────────────────────────┘
│                                │
│ Completed this month (5)       │
│ ┌ App X · ✓ 14 days · +12 rep ┐│
│ │ View proof card →           │
│ └─────────────────────────────┘
│                                │
│ Empty state:                   │
│ "No active tests yet.          │   AppEmptyState component
│  Next batch run: in 4h 23m."   │
└────────────────────────────────┘
```

---

## 6. Responsive (Medium+ window size class)

- 2-column layout: list left (1fr) + detail right (1.5fr)
- AppBottomBar → NavigationRail
- Home: hero card width 60%, sidebar shows credits + tier + leaderboard preview

## 7. Out of V1 wireframes (deferred / skeleton-only)

- Inbox / notification center (V1 用 system notification + 簡單 in-app banner，沒專屬畫面)
- Settings (V1 只有 minimal: locale switch + sign out)
- Reports + appeals flow (V1 walking skeleton：簡單 form 即可)
- Onboarding skip path 不畫 — 強制流程，skip = 用 default
