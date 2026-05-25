# Play Console 自動同步設定 / Play Console Auto-Sync Setup

> 一次設定,**所有 AppTest 配對到的測試者自動進你 Play Console 白名單**。
> One-time setup; AppTest auto-adds matched testers to your Play Console allowlist forever.

---

## 為什麼需要這個？/ Why this exists

Google Play 對 2023 年後註冊的開發者帳號要求：
- **12 個獨立測試者 × 14 天連續活躍** 才能解鎖 Production 上架。
- Closed Testing 必須維護「允許測試的 email 名單」。

AppTest 配對測試者給你之後,**他們的 Gmail 不會自動進你的 Play Console**。預設情況：
- 配對成功 → 對方點 opt-in 連結 → Play Store 顯示「你不是測試者」❌

設好這個自動同步,就變成：
- 配對成功 → AppTest 後端自動把對方 Gmail 加到你的 Google Group → Play Console 接受 → 對方直接安裝 ✅

---

## 設定步驟 (5 步,~5 分鐘)

### 1. 建立 Google Group

到 [groups.google.com](https://groups.google.com/) → 點「**建立群組**」

| 欄位 | 建議值 |
|---|---|
| **群組名稱** | `MyApp Testers` (隨便) |
| **群組電子郵件地址** | `myapp-testers` → 完整變成 `myapp-testers@googlegroups.com` |
| **群組類型** | Email list (預設) |
| **加入群組權限** | 「**只有受邀者**」(防止 random 加入) |
| **誰可以查看成員** | 「群組擁有者」 |

按「**建立群組**」。

---

### 2. 把 AppTest service account 加為 Manager

進你剛建的 Group → **成員 → 新增成員**

```
電子郵件地址: 726162458626-compute@developer.gserviceaccount.com
角色:        管理員 (Manager)
直接加入:    勾選 (跳過邀請信)
```

按「**新增成員**」。

> 為何要 Manager？AppTest 後端用這個 service account 透過 Admin SDK Directory API
> 自動 `members.insert` 新增測試者。Manager 才有這個權限。

---

### 3. 把 Group 加到 Play Console 封閉測試

到 Play Console → 你的 App → **測試 → 封閉測試 → 你的 track (例如 Alpha)**

切到 **測試者 (Testers)** tab:

| 欄位 | 值 |
|---|---|
| **電子郵件群組** | 貼你的 Group email (`myapp-testers@googlegroups.com`) |
| **加入網址** | (Play Console 自動產生,複製起來給測試者用) |

按「**儲存變更**」。

---

### 4. 在 AppTest 編輯器貼 Group email

打開 AppTest app → My Apps → 點你的 App → 編輯 → 滑到下方 **「Play Console 自動同步 (進階・選用)」** → 展開

貼入你的 Group email (`myapp-testers@googlegroups.com`) → **儲存**。

---

### 5. 完成 — 配對之後會發生什麼

1. AppTest 配對 Tester X 給你的 app
2. AppTest 後端讀到你 App 設定了 `testing_group_email`
3. 後端呼叫 Admin SDK Directory API → 加 Tester X 的 Gmail 到你的 Group
4. Google 自動 propagate → Play Console 把 Tester X 加進測試者白名單
5. Tester X 收到 FCM「您被配對到新 App」→ 點 Join → Play Store 顯示「允許安裝」✅

---

## 疑難排解 / Troubleshooting

### Cloud Run log 出現 `group_sync HTTP 403`
- service account 還沒被加為 Group Manager
- 重檢查 Step 2: `726162458626-compute@developer.gserviceaccount.com` 是否真的在 Members 列表 + Role=Manager

### Cloud Run log 出現 `group_sync HTTP 404`
- Group email 拼錯
- 重檢查 Step 4: `xxx@googlegroups.com` 是否與 Step 1 建立的 Group 完全一致

### Tester 點 opt-in 連結仍顯示「你不是測試者」
- Google 同步約需 15 分鐘到 1 小時
- 確認 Group **Direct Membership** (Step 2 勾選「直接加入」)
- 不是 Step 3 的 Closed Testing 而是 Internal Testing? Internal Testing 沒這個 group 機制

### 我想停用自動同步
- AppTest 編輯器 → 進階區塊 → 把 Group email 欄位清空 → 儲存

---

## 安全性說明

- AppTest service account **只能** 對你**手動指定**的 Group 操作 (Step 2 你授權的)
- 收回授權: Group → 成員 → 把 service account 移除 (Group 還在,只是 AppTest 沒辦法再加 member)
- AppTest 不會讀你的 email、不會 access 其他 Group、不會發信
- 程式碼: [AppTest-backend/src/main/kotlin/com/apptest/backend/google/GoogleGroupsService.kt](https://github.com/cjc305/AppTest-backend/blob/master/src/main/kotlin/com/apptest/backend/google/GoogleGroupsService.kt)
