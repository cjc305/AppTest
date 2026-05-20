# AppTest — Auto-Documentation Architecture

> **Version:** 0.1 · **Last updated:** 2026-05-19 · **Owner:** TBD
> 每個 module 的 4-doc 系統 (README / API / FLOW / DEPENDENCY) 的 template + lint + 更新策略。
> CI 整合在 `cicd.md §2 docs-lint`，module 結構在 `monorepo.md §2`。

---

## 1. Why per-module docs

AI 友善的根本：**一個檔案內就能解釋這個模組**。讀 CLAUDE 或一個新 dev 進來時，不必開 20 個 .kt 才知道這 module 在做什麼。

4 份 docs 分擔不同問題：

| Doc | 答案 | 變更頻率 |
|---|---|---|
| `README.md` | 「這個 module **做什麼**？什麼時候用它？」 | 低 |
| `API.md` | 「外面能呼叫**什麼**？簽名長怎樣？」 | 中 |
| `FLOW.md` | 「內部資料 / 事件**怎麼流**？」 | 中 |
| `DEPENDENCY.md` | 「**誰**依賴它？它依賴**誰**？如何替換？」 | 變更模組 dep 時 |

## 2. Doc structure templates

### README.md
```
# :<layer>:<name>
> 1 段話：這個模組存在的理由。

## Use it when / Don't use it for / Key concepts / Quick example / Related
- bullet 列舉，spec_ref 指 canonical 來源
```

### API.md
```
# :<layer>:<name> — Public API
> 只列**對外**型別（internal 不寫）。
## Types
- 每個 public class/interface/sealed 一段，附 thread safety / lifecycle / implementations
## Functions
- 公開 fn 簽名 + pure? + throws?
## NavGraph extensions (feature only)
- destination + params + callbacks
```

### FLOW.md
```
# :<layer>:<name> — Internal Flow
> 主流程，Mermaid 優先（flowchart / sequenceDiagram / stateDiagram）
## Flow 1: <name>  → 1 圖 + 步驟 1 行說明
## State machine (若有)
```

### DEPENDENCY.md
```
# :<layer>:<name> — Dependencies
## I depend on  → table: Module | Why
## Modules depending on me  → table: Module | Uses what (CI lint warns stale)
## How to replace  → 3 步驟：implement interface → TestInstallIn override → assembleDebug 驗
## How to test  → unit / integration / 對應 cicd.md §9
```

## 3. Generation strategy

V1 走「**模板 + 人工填**」路線，**不**寫 generator script。理由：
- Generator 容易產出 generic 內容，沒實際資訊量
- AI session 本身就能根據 template 產生（更貼近實際 code）

未來 V2/V3 考慮：
- KDoc → API.md 自動 sync（用 Dokka + custom Markdown formatter）
- Mermaid CI render check (是否 syntax valid)
- DEPENDENCY.md「I depend on」段自動從 `build.gradle.kts` 提取

## 4. CI lint (in `cicd.md §2 docs-lint`)

```bash
# script/lint-docs.sh
for mod in $(find . -name "build.gradle.kts" -not -path "./build/*" | xargs dirname); do
  for doc in README.md API.md FLOW.md DEPENDENCY.md; do
    if [ ! -f "$mod/$doc" ]; then
      echo "[FAIL] $mod missing $doc"; exit 1
    fi
    if [ $(wc -l < "$mod/$doc") -gt 200 ]; then
      echo "[FAIL] $mod/$doc exceeds 200 lines"; exit 1
    fi
  done
done
```

PR check 失敗 = 新 module 缺檔 / 文件過長。

## 5. Cross-reference convention

文件之間互連時用 markdown link：

```markdown
詳見 [`reputation_system.md §2`](../_specs/reputation_system.md#2)
```

不可寫 `(see other doc)` 模糊指向；必須具體 section。

## 6. Update timing

| Trigger | Who updates which doc |
|---|---|
| 加新 public type | API.md (developer 自己) |
| 改 module deps | DEPENDENCY.md (developer 自己) |
| 改主流程 / 加新 use case | FLOW.md (developer 自己) |
| 改模組整體定位 | README.md (要 review) |

**Doc 更新與 code 在同一 PR**；分開 PR 視為 incomplete。

## 7. Spec ↔ docs + Discoverability

**邊界：** 跨 module 設計 → `_specs/*.md`；module 內部設計 → 該 module 4 docs。`spec_ref` 單向指；4 docs 不 inline copy spec 內容。

**新人 5 分鐘上手路徑：** `_specs/_ai/README.md` → `manifest.yaml` 找 task → spec → 對應 module 4 docs → code。每層 ≤ 30s cold-read。

## 9. Anti-patterns

| Anti-pattern | Why |
|---|---|
| README 寫一頁 prose 沒 bullet | 沒 scannable，AI 載入浪費 token |
| API.md 列 internal class | API doc 只該列公開 surface |
| FLOW.md 沒 diagram 只有 text | 流程用圖比文字易懂 |
| DEPENDENCY.md「我依賴 androidx.core」沒理由 | 該列 **why**，不只列 what（`build.gradle.kts` 已是 what） |
| 4 doc 沒對齊（如 API 新增但 README 沒提） | code review reject |

## 10. Open decisions

| ID | Decision | Status |
|---|---|---|
| APT-OPS-007 | 是否引入 Dokka 自動 sync KDoc → API.md | default: V1 否，手動寫 |
| APT-OPS-008 | DEPENDENCY.md 「dependents」是否強制即時更新 | default: best-effort，季度 audit |
