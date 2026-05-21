# AppTest — Pre-Launch Blocker Checklist

> **Generated:** 2026-05-20 | **Last updated:** 2026-05-22 (FCM wired, backend deployed, Supabase migration written)
> **Status:** FCM (Task #24) + real Supabase repos (Task #25) complete. One user action required: run `supabase/migrations/001_add_missing_columns.sql` in Supabase Dashboard SQL Editor, then upload release AAB to Play Store.

---

## 0. Build sanity check (manual, you run)

```bash
gradle wrapper          # already exists, skip if so
./gradlew :app:assembleDebug
./gradlew spotlessApply   # auto-format pass
./gradlew detekt          # lint pass
./gradlew enforceFileLineLimit
```

If any of the above explode, fix before continuing. The 17-module Hilt graph has never been compiled end-to-end in CI (CI doesn't exist yet — see APT-X-003).

Expected friction:
- KSP + Hilt cold compile = 5–10 min on first build
- Detekt will likely surface ~10–30 violations in existing code (we wrote new code clean but old code wasn't lint-checked). Fix or whitelist as appropriate.
- `enforceFileLineLimit` should pass — `AppStrings.kt` is whitelisted, everything else under 200.

---

## 1. Owner-gated decisions (you must answer)

| ID | Decision | My recommendation | Impact |
|---|---|---|---|
| **APT-A-002** | Email login provider | Supabase magic link (default) | Blocks R-043 implementation choice |
| **APT-A-003** | Install verification 強度 | PackageManager V1 + Play Integrity V2 (default) | Unblocks R-040 |
| **APT-OPS-001** | Domain + Play Console 帳號 | Buy `apptest.dev` + Play Console $25 dev account | Blocks Play submission entirely |
| **APT-OPS-002** | Moderator: 第三方 vs 自有 | V1 自有 1 人 (default) | Soft-blocks scaling beyond launch |

---

## 2. External credentials & accounts you must provision

### 2.1 Supabase project ✅ DONE

- [x] Project **apptest-prod** live at `jefgixmmlqtgbxobukkt` (Singapore / ap-southeast-1, Free tier)
- [x] Schema deployed: profiles, apps, matches, notifications (4 tables + RLS + Realtime enabled)
- [x] Auth magic link enabled; redirect URL `apptest://login-callback` + `https://apptest-prod.web.app/**` configured
- [x] `SUPABASE_URL` + `SUPABASE_ANON_KEY` in `local.properties` + `BuildConfig` fields wired
- [x] Real `SupabaseAuthRepository` live (R-043 ✅), Realtime inbox live (R-044 ✅)
- [ ] **ACTION REQUIRED:** Run `supabase/migrations/001_add_missing_columns.sql` in Supabase Dashboard → SQL Editor (adds streak_days/credits/package_name/proofs table etc.)
- [ ] (Optional V1) Enable Google OAuth provider if you want Google sign-in

### 2.2 Firebase project ✅ DONE

- [x] Firebase project **apptest-7fced** created (Spark free plan)
- [x] Android app registered with package `com.cjc305.apptest` (app_id `1:726162458626:android:8fb65dadc264210106bf1b`)
- [x] `google-services.json` placed at `app/google-services.json`
- [x] Firebase BoM 33.7.0 + Crashlytics + FCM + Analytics wired in `libs.versions.toml` + build files
- [x] `AppTestMessagingService` (@AndroidEntryPoint) registered in Manifest
- [x] FCM wired via topic-based delivery (`user_<uid>`) — ADC on Cloud Run, no Server Key needed; Android subscribes in `MainActivity` on sign-in

### 2.3 Google OAuth client (optional V1, if you want Google sign-in)

- [ ] In Google Cloud Console (Firebase project's parent) → APIs & Services → Credentials → OAuth 2.0 client ID (Android)
- [ ] Provide package `com.cjc305.apptest` + your debug `SHA-1` (`./gradlew :app:signingReport`)
- [ ] Copy the **Web client ID** (not Android) — Supabase needs it to verify Google ID tokens
- [ ] Add to `local.properties`: `google.webClientId=...`
- [ ] Configure Supabase Google provider with same Web client ID

### 2.4 Domain & assetlinks (blocks deep-link verification)

- [ ] Buy `apptest.dev` (or your chosen domain — search/replace `apptest.dev` repo-wide)
- [ ] Generate `assetlinks.json` containing your release signing cert SHA-256
- [ ] Host at `https://apptest.dev/.well-known/assetlinks.json`
- [ ] In `AndroidManifest.xml`, flip the `https` `<intent-filter>` `autoVerify="false"` → `"true"`

### 2.5 Play Console (blocks submission)

- [x] $25 developer account + entity / address / tax / payment forms
- [x] App entry created: package **`com.cjc305.apptest`**, default lang `en-US`
- [ ] Store listing (en + zh-TW): short description, long description, screenshots, feature graphic, icon 512px
- [ ] Content rating questionnaire
- [ ] Data safety form (auth, content, install detection data)
- [ ] Closed testing track set up + tester list email (or Google Group)
- [ ] App signing: use Play App Signing (recommended); generate upload keystore + add CI secret

### 2.6 Signing keystore (release builds)

- [ ] Generate upload keystore: `keytool -genkey -v -keystore upload.keystore -alias upload ...`
- [ ] Store keystore + passwords **outside** repo (1Password / GitHub Actions secret)
- [ ] Wire `signingConfigs { release { ... } }` in `:app/build.gradle.kts` reading from `local.properties` or env

---

## 3. Backend code (separate repo — not in this codebase)

| Task | What |
|---|---|
| **R-045** | Ktor matching service — nightly cron consuming Supabase via service-role key |
| **R-046** | Reputation recompute worker (per `_specs/reputation_system.md`) |
| **R-047** | Anti-cheat heuristics worker (per `_specs/anti_cheat.md`) |
| **R-048** | Proof card generator — signed PNG/SVG endpoint |

These run on Cloud Run (asia-northeast1) per spec. You decide: same repo / separate repo / monorepo with build matrix.

---

## 4. CI/CD (APT-X-003)

Workflows generated ✅ — only `git init` + push remain:

- [x] `.github/workflows/pr.yml` — spotless + detekt + lint + build + test on every PR
- [x] `.github/workflows/release.yml` — bundleRelease + optional Play upload on tag push
- [ ] `git init && git remote add origin git@github.com:cjc305/AppTest.git`
- [ ] Initial commit + push to GitHub
- [ ] Add GitHub Actions secrets: `KEYSTORE_BASE64`, `KEY_ALIAS`, `KEY_PASSWORD`, `STORE_PASSWORD` (after §2.6)

---

## 5. Remaining V1 client tasks waiting on the above

Once §1–§2 are answered/provisioned, the following can be unblocked (in priority order):

| Task | Blocked by |
|---|---|
| **R-043** Supabase Auth real impl | §2.1 + §2.3 (if Google) |
| **R-044** Supabase Realtime subs | R-043 |
| **R-040** Install detection | APT-A-003 |
| **R-041** WorkManager heartbeat | R-043 (needs JWT to call endpoint) |
| **R-042** FCM push | §2.2 |
| **APT-X-004** Crashlytics | §2.2 |
| **APT-X-003** GitHub Actions CI | §4 |
| Real Play Store submission | All of the above + §2.5 + §2.6 |

---

## 6. What's already done (you don't need to touch)

- ✅ All 8 V1 features built with FakeRepo (Home/MyApps/AppDetail/Testing/Profile/Inbox/Auth/Onboarding)
- ✅ MainScaffold with bottom-bar wrapping 4 tabs (Home/MyApps/Testing/Profile)
- ✅ Settings stub with sign-out wired (uses current AuthRepository, swaps to real on R-043 with zero call-site change)
- ✅ 14/14 modules with README/API/FLOW/DEPENDENCY docs
- ✅ Sign-out flow: `MainActivity.signOut()` → `AuthRepository.signOut()` → `SessionStore.clear()` → `AuthState.SignedOut` → NavHost flips to AuthRoot
- ✅ Share invite: `MainActivity.shareInvite(uri)` uses `Intent.createChooser` with `apptest://invite?ref=...`
- ✅ Inbox deep-link parsing wired via `AppDeepLink.parse`
- ✅ Home "next batch ETA" computed from real wall-clock 02:00 UTC instead of placeholder
- ✅ Home skip-match in-memory dismissal (server persistence lands with R-043+)
- ✅ Token attachment layer: `:core:network/AuthInterceptor` + `:core:domain/TokenProvider` + `:core:data/DataStoreSessionStore` — when R-043 writes real Supabase session, every authenticated REST call automatically gains `Authorization: Bearer <jwt>`
- ✅ i18n: `app_name` + tagline localized for en + zh-TW (Play Console listing-ready)
- ✅ Lint stack: Spotless(ktlint) + Detekt + custom 200-line enforcer

---

## 7. Estimated unblock-to-launch timeline

Assuming you sit down with the checklist for one focused session:

| Step | You time | Then I do |
|---|---|---|
| §1 owner decisions | 15 min | nothing |
| §2.1 Supabase + RLS | 1–2 hr (schema is in spec) | R-043 + R-044 (~4 hr code) |
| §2.2 Firebase + `google-services.json` | 20 min | R-042 + APT-X-004 (~3 hr code) |
| §2.4 Domain + assetlinks | 30 min + DNS propagation | flip manifest flag (5 min) |
| §2.5 Play Console + content rating | 1–2 hr (questionnaires) | nothing |
| §2.6 Keystore + signing config | 20 min | wire `signingConfigs` (15 min) |
| §3 Backend repo (R-045~R-048) | depends on scope | substantial — separate session |
| §4 git init + CI | 30 min | `.github/workflows/*.yml` (~1 hr) |

**Realistic time to first closed-test build on Play Console: 1–2 working days of your time + a couple of half-days of mine on top.**

Backend services (§3) are the longest pole — possibly 1–2 weeks of focused work depending on whether matching engine V1 is rule-based MVP or fancy.

---

## Questions? Pick one of:

- **A.** "Let's do §1 — answer your A-002/A-003/OPS-001/OPS-002 questions and you proceed to §2"
- **B.** "I'll set up Supabase + Firebase now, come back to you with credentials"
- **C.** "Help me plan the backend repo (§3) first since it's the longest pole"
- **D.** "Show me how to run the build sanity check (§0) and walk through what to expect"
