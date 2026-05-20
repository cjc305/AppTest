# `_specs/_ai/` — AI-friendly entry point for AppTest

> **Read this first.** 為冷啟 session 設計：3 個檔 (`README.md` + `manifest.yaml` + 1 個 spec) → 可開工。
> Token entry cost: ≤ 25k typical session（全 28 specs ≈ 110k，不要全載）。

---

## What this folder is

| File | Purpose |
|------|---------|
| `README.md` (you are here) | Entry pointer + cheat-sheet |
| `manifest.yaml` | Task index + owner blockers + SDK whitelist (single SoT) |
| `PROMPTS.md` | 7 atomic prompt templates — copy into new sessions |
| `DEPS.md` | Mermaid 圖：architecture / build / module DAG / owner-blocker fan-out |

24 個 spec 文件在 `_specs/*.md`，每個 ≤ 200 行。本資料夾**不**重複它們的內容，只指向。

---

## Status (2026-05-19)

- ✅ **Architecture phases A~H complete** (28 specs, 4373 lines, all ≤ 200)
- 🟡 **V1 build foundation partial** (Gradle root + core/common + core/domain + 部分 designsystem 已 scaffold)
- ⏳ **V1 features 8 個未開始** — 等下一個 session
- ⏳ **6 個 owner-gated decision 待決** — 見 `manifest.yaml` `owner_blockers`

---

## Standard cold-start (≤ 30 秒)

```bash
# 1. Workspace + project context
cat ../../CLAUDE.md ../CLAUDE.md 2>/dev/null  # workspace 與 project（若有）

# 2. AI entry pointer
cat _specs/_ai/README.md           # 你在這

# 3. Status overview + next unblocked task
grep -B1 "status: not_started" _specs/_ai/manifest.yaml | grep "APT-V1-R-" | head -10

# 4. Matching prompt template
grep -A30 "^## impl-feature-module" _specs/_ai/PROMPTS.md  # 或 impl-screen / impl-usecase / impl-core-module / impl-api-endpoint / verify-task / refactor-200line

# 5. Open relevant spec
grep "ref:" _specs/_ai/manifest.yaml | grep -i "<keyword>"  # 找對應 spec
```

---

## "What's next?" recipes

### Find lowest-id not_started V1 build task

```bash
grep -E "id: APT-V1-R-[0-9]+, status: not_started" _specs/_ai/manifest.yaml | head -1
```

### Architecture phase status (all done now)

```bash
grep -E "id: APT-DOC-[A-H]-" _specs/_ai/manifest.yaml
```

### Critical-path progress to first demo APK

```bash
for id in APT-V1-R-001 APT-V1-R-003 APT-V1-R-002 APT-V1-R-005 APT-V1-R-004 APT-V1-R-009 APT-V1-R-010 APT-V1-R-022; do
  grep "id: $id," _specs/_ai/manifest.yaml | sed 's/.*status: \([a-z_]*\).*/  &/'
done
```

### Show owner-gated decisions still open

```bash
grep -E "^\s+- \{ id: APT-[AP]-" _specs/_ai/manifest.yaml
```

---

## How to update manifest

完成 task 時：
1. `grep "id: APT-V1-R-NNN" _specs/_ai/manifest.yaml`
2. `status: not_started` → `status: done`
3. 更新 `meta.last_updated`
4. 若發現新工作，新增 task with 下一個 free id

Owner 決議落地時：
1. 從 `owner_blockers:` 移到 `resolved_decisions:`
2. 找下游 `blocked_by:` 包含該 id 的 task → 拿掉
3. 把那些 task 從 `blocked` → `not_started`

---

## ID conventions

| Prefix | Meaning |
|---|---|
| `APT-S-NNN` | Spec index (`mvp.md` etc.) |
| `APT-DOC-X-NNN` | Architecture document task (Phase X = A~H) |
| `APT-V1-R-NNN` | V1 build / implementation task |
| `APT-V2-R-NNN` | V2 build task (post V1 GA) |
| `APT-V3-R-NNN` | V3 build task |
| `APT-X-NNN` | Cross-cutting (lint / CI / docs) |
| `APT-P-NNN` | Product decision (owner-gated) |
| `APT-A-NNN` | Architecture decision (owner-gated) |
| `APT-OPS-NNN` | Operational task |
| `APT-Q-NNN` | Hard rule / promise (immutable) |
| `APT-API-NNN` | (V2+) API endpoint (1:1 with spec §6) |

Number sparse on purpose — never re-number after retired。

---

## Spec map (24 files)

| Group | Files | See |
|---|---|---|
| **Product** | mvp / product_architecture / reputation_system / ai_matchmaking / growth_and_network / anti_cheat | `manifest.yaml` APT-S-001~006 |
| **Data + API** | database_schema / database_schema_audit / api_contracts | APT-S-007~008 |
| **Modules** | modularization / feature_modules / monorepo | APT-S-009~011 |
| **UI** | wireframes / wireframes_dev / onboarding_ux / design_system / compose_components / compose_organisms_templates | APT-S-012~015 |
| **Flows** | navigation / testing_exchange_flow | APT-S-016~017 |
| **Backend** | backend_architecture | APT-S-018 |
| **Ops** | cicd / auto_docs | APT-S-019~020 |
| **GTM** | play_store_strategy | APT-S-021 |

---

## When NOT to use this folder

- Tiny one-shot changes (typo, single import) — 直接編輯。
- Spec-only edits — 更新 `_specs/*.md` 同時 bump manifest `last_updated`。
- 一次性實驗 / 廢棄 prototype — 不要污染 manifest。

---

## Provenance

- 2026-05-19 bootstrap (greenfield) + Phase A 完成
- 2026-05-19 Phase B+D 並行完成 (3 個檔拆分以遵 200-line 規)
- 2026-05-19 Phase C+E+F+G+H 一次完成 → architecture 全部就緒
- 4-file pattern adapted from QiFlux convention (`~/AndroidStudioProjects/CLAUDE.md`)
