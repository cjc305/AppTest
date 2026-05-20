# AppTest — Play Console Store Listing 內容

> 直接複製貼上到 Play Console → 主要商店資訊頁面

---

## 應用程式名稱
```
AppTest
```

---

## 簡短說明（zh-TW，最多 80 字）
```
Android 開發者的 App 互測平台。AI 智慧配對、信用制度、14 天測試計劃。
```

## 簡短說明（en-US，最多 80 chars）
```
Android dev testing network. AI matching, reputation system, 14-day test plans.
```

---

## 完整說明（zh-TW，最多 4000 字）
```
AppTest 是專為 Android 開發者設計的互測網絡。不需要花錢請測試員或買假評分，加入開發者社群，互相幫助讓 App 更好。

■ 運作方式

1. 將你的 Android App 加入 AppTest（Play 商店 closed test 連結）
2. 每天凌晨 2 點（UTC），AI 根據你的信用等級和 App 類別自動配對
3. 在 14 天內每天測試對方的 App 並回報問題
4. 完成測試獲得信用點數，提升聲望等級
5. 用你的聲望招募更多高品質測試員

■ 主要功能

• AI 配對系統
根據信用等級、App 類型、語言偏好自動匹配，避免同質化競爭。

• 聲望制度（Silver / Gold / Platinum）
完成率、連勝天數、測試次數、App 發佈數共同決定你的等級。等級越高，優先獲得配對機會。

• 即時通知
配對成功、測試里程碑、At-Risk 警告即時推播。

• 信任證明
HMAC-SHA256 簽章的測試完成證書，可獨立驗證，防止偽造。

• 防舞弊機制
系統自動偵測多人共謀等異常行為，維護平台公信力。

■ 隱私說明

AppTest 不會公開你測過哪些 App，測試紀錄完全私密。我們使用 Email Magic Link 進行安全的無密碼登入，不儲存密碼。

■ 適合誰用？

獨立 Android 開發者、小型遊戲工作室、還沒有足夠預算做正式測試的開發團隊。

■ 注意事項

• 目前支援 Android 9（API 28）以上
• 所有 App 必須已上架 Google Play closed/open testing track
• 直接傳送 APK 或 AAB 不符合平台規定

加入 AppTest，一起把 Android 生態系做得更好。
```

---

## 完整說明（en-US，最多 4000 chars）
```
AppTest is an AI-powered testing network for Android developers. Instead of spending money on professional testers or fake reviews, join a community where developers help each other ship better apps.

■ How It Works

1. Add your Android app to AppTest using its Play Store closed-test link
2. Every day at 2AM UTC, our AI matches you with developers whose apps complement yours
3. Test each other's apps over 14 days with daily check-ins
4. Earn reputation credits for quality, consistent testing
5. Use your reputation to recruit testers for your own apps

■ Key Features

• AI Matching System
Matched daily based on your reputation tier, app category, and language preference. No same-category conflicts.

• Reputation System (Silver / Gold / Platinum)
Your tier is computed from completion rate, win streak, test count, and apps published. Higher tier = priority matching.

• Real-time Inbox
Instant push notifications when a match is found, milestones are reached, or an at-risk warning fires.

• Signed Proof Cards
HMAC-SHA256 signed test completion certificates — independently verifiable, tamper-proof.

• Anti-fraud Engine
Automated detection of collusion patterns keeps the network trustworthy.

■ Privacy

AppTest does not publicly expose which apps you have tested. Your test history is private. We use email magic links for secure, passwordless authentication — no password stored.

■ Who Is This For?

Independent Android developers, indie game studios, and small teams who want genuine user feedback before launching to the public.

■ Requirements

• Android 9 (API 28) or higher
• Your app must already be on a Play Store closed or open testing track
• Direct APK / AAB sharing is not supported per policy

Join AppTest and help the Android developer community ship better apps together.
```

---

## 上傳圖片清單

| 圖片 | 檔案 | 尺寸 | Play Console 欄位 |
|---|---|---|---|
| App 圖示 | `screenshots/icon_512.png` | 512×512 | 高解析度圖示 |
| Feature Graphic | `screenshots/feature_graphic.png` | 1024×500 | 宣傳圖片 |
| 截圖 01 | `screenshots/01_home.png` | 720×1600 | 手機截圖 |
| 截圖 02 | `screenshots/02_myapps.png` | 720×1600 | 手機截圖 |
| 截圖 03 | `screenshots/03_testing.png` | 720×1600 | 手機截圖 |
| 截圖 04 | `screenshots/04_profile.png` | 720×1600 | 手機截圖 |
| 截圖 05 | `screenshots/05_appdetail.png` | 720×1600 | 手機截圖 |

---

## Play Console 操作步驟

1. 開 https://play.google.com/console → 選 AppTest → 左側 **主要商店資訊**
2. **應用程式名稱**：AppTest
3. **簡短說明**：貼上對應語言的簡短說明
4. **完整說明**：貼上對應語言的完整說明
5. **圖形**→ 上傳 `icon_512.png`（高解析度圖示）+ `feature_graphic.png`（宣傳圖片）
6. **手機截圖**→ 上傳 01~05 五張截圖
7. **應用程式類別**：Tools 或 Productivity
8. **電子郵件地址**：你的聯絡 email
9. 按 **儲存**

---

## Firebase Server Key（給 Ktor backend 用）

舊版 Server Key 已被 Firebase 停用（2024/6/20）。請改用 FCM HTTP v1 API：

**Service Account email:**
```
firebase-adminsdk-fbsvc@apptest-7fced.iam.gserviceaccount.com
```

**取得方式：**
1. Firebase Console → apptest-7fced → 專案設定 → 服務帳戶
2. 點「產生新的私密金鑰」→ 下載 JSON
3. 放到 Ktor backend `/etc/secrets/firebase-adminsdk.json`
4. **絕對不要 commit 這個 JSON 到 git**

---

## GitHub Actions Secrets（CI 啟用後需要設定）

| Secret 名稱 | 值 | 取得方式 |
|---|---|---|
| `KEYSTORE_BASE64` | `base64 upload.keystore` 的輸出 | `base64 -w 0 upload.keystore` |
| `KEY_ALIAS` | `upload` | 固定值 |
| `STORE_PASSWORD` | Windows Credential Manager: AppTest_STORE_PASSWORD | |
| `KEY_PASSWORD` | 同 STORE_PASSWORD（PKCS12 格式） | |

執行：
```bash
base64 -w 0 upload.keystore  # 複製輸出 → GitHub → Settings → Secrets → New secret
```
