# AppTest — V1 Wireframes (Developer & Profile)

> **Version:** 0.1 · **Last updated:** 2026-05-19 · **Owner:** TBD
> Dev-side + 個人頁 + 證明卡 wireframes。Tester-side 與 legend 見 `wireframes.md`。

---

## 1. My Apps (list)

```
┌─ My apps ─────────────────[+]─┐
│ ┌ MyApp1 recruiting           ┐│   list item
│ │ 8/12 testers · 6 days left  │
│ │ [pause] [edit] [view stats] │
│ └─────────────────────────────┘
│ ┌ MyApp2 completed   ✓        ┐│   with proof card link
│ │ 12 testers · proof card→    │
│ └─────────────────────────────┘
│ ┌ MyApp3 paused                ┐│
│ │ 3/12 testers · (resume)     │
│ └─────────────────────────────┘
│                                │
│ Empty state:                   │
│ "You haven't published any     │   AppEmptyState
│  app yet. First one is free!"  │
│              [Create app]       │
└────────────────────────────────┘
```

---

## 2. App Editor (create / edit)

```
┌─ ← Create app ──────────[Save]─┐
│ Package name *                 │   text input
│ < com.example.myapp          > │
│                                │
│ App name *                     │
│ < NoteFlash                  > │
│                                │
│ Description * (0/500)          │
│ ┌──────────────────────────┐   │   multiline
│ │                          │   │
│ └──────────────────────────┘   │
│                                │
│ Icon * (PNG/JPG, ≤ 1MB)        │
│ [📤 Upload]                    │
│                                │
│ Category *                     │
│ ▾ Productivity                 │   dropdown
│                                │
│ Play Console opt-in URL *      │
│ < play.google.com/apps/test… > │   live validation badge ✓/✗
│                                │
│ Required testers (1-100)       │
│ <─────●──────> 12              │   slider with number
│ Required days (7-30)           │
│ <───────●──> 14                │
│                                │
│ Cost: 1 credit (you have 4)    │   cost preview
└────────────────────────────────┘
```

Validation:
- `package_name` 不可重複 (server check on blur)
- `play_opt_in_url` host = `play.google.com` (client + server)
- Save button disabled until all `*` valid

---

## 3. Profile

```
┌─ Profile ──────────────[⚙]────┐
│                                │
│        (large photo)           │
│       Alice Chen               │
│       Silver ⭐ · 4 credits   │   tier badge (large)
│                                │
│ ▾ Stats (last 30 days)         │
│  Completed tests:    8         │
│  Days contributed:   84        │
│  Reputation Δ:      +24        │
│  Streak:             5 🔥      │
│                                │
│ ▾ Reputation breakdown          │   bar chart per sub-score
│  Completion rate  ███████░ 35  │
│  Streak           █████░░░ 16  │
│  Volume           ███░░░░░ 8   │
│  Publish          █░░░░░░░ 5   │
│  (penalties: 0)               │
│                                │
│ ▾ Proof cards (3)              │   scrollable thumbnails
│                                │
│ ▾ Activity history             │   event log
│  · +6 rep · completed App A    │
│  · -1 cr · published App B     │
│                                │
│ (Invite a developer friend)    │   growth loop CTA
└────────────────────────────────┘
```

---

## 4. Completion Proof Card (viral asset, full-screen)

```
┌────────────────────────────────┐   full-screen
│                                │   1080×1920 export (story format)
│   ✓ Test complete              │   spatial layer 2 (front)
│                                │
│   ┌─────────────────────────┐  │   spatial layer 1
│   │   NoteFlash             │  │
│   │   Productivity          │  │
│   │   14 days · 12 testers ✓│  │
│   │                         │  │
│   │   Verified by AppTest   │  │
│   │   apptest.dev/v/abc123  │  │   public verify URL
│   └─────────────────────────┘  │
│                                │
│         (gradient layer 0)     │
│                                │
│   [Share image]  [Copy link]   │
└────────────────────────────────┘
```

- Auto-save to Photos gallery
- Share triggers system share sheet (image + text "Just finished testing X on @AppTest")
- 公開 verify URL 任何人都能打開看驗證頁
- 點 verify URL → 簡單 web 頁顯示 "✓ Test ID xyz verified on YYYY-MM-DD"
