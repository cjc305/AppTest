package com.apptest.core.common

/**
 * Central UI string catalog. V1 supports en + zh-TW per `_specs/mvp.md §8`.
 * Pure data class — `:core:common` is kotlin-jvm, no Compose dep. The CompositionLocal
 * wrapper lives in `:core:designsystem/theme/AppLocaleStrings.kt`.
 *
 * **APT-X-005 (2026-05-20):** All keys mirror `app/src/main/res/values/strings.xml` +
 * `values-zh-rTW/strings.xml`. New work should prefer `stringResource(R.string.x)` over
 * `LocalAppStrings.current.x`. This catalog stays for back-compat with existing call sites;
 * future cleanup task = migrate Composables one feature at a time, then delete this file +
 * `LocalAppStrings`.
 *
 * 新增字串：
 * 1. **First** 加 `<string name="newKey">...</string>` 到 `values/strings.xml` (EN)
 * 2. 同步加到 `values-zh-rTW/strings.xml` (繁中)
 * 3. 新 Composable 用 `stringResource(R.string.newKey)` — 不要再加到 AppStrings data class
 * 4. 既有 LocalAppStrings 引用點維持原狀直到逐 feature 遷移
 *
 * 不放這裡的（保留 Kotlin literal）：
 * - 純 debug / log 訊息
 * - FakeRepository 內 mock 資料（demo only）
 * - 內部 enum.name（如 status enum 顯示直接用 enum）
 */
data class AppStrings(
    // ── App-wide ───────────────────────────────────────────────────────
    val appName: String,
    val cta_back: String,
    val cta_retry: String,
    val cta_skip: String,
    val cta_continue: String,
    val cta_save: String,
    val cta_cancel: String,
    val cta_saving: String,
    val state_loading: String,
    val state_loading_default_msg: String,

    // ── Sign in ────────────────────────────────────────────────────────
    val signin_social_proof: String,
    val signin_cta_google: String,
    val signin_cta_email: String,
    val signin_terms: String,
    val signin_email_title: String,
    val signin_email_label: String,
    val signin_cta_send_link: String,
    val signin_signing_in: String,
    val signin_link_sent_title: String,
    val signin_link_sent_body: String,        // %1$s = email
    val signin_change_email: String,
    val signin_failed_title: String,

    // ── Email verify ───────────────────────────────────────────────────
    val verify_progress: String,              // %1$s = email
    val verify_failed_title: String,
    val verify_signed_in_loading: String,

    // ── Onboarding ─────────────────────────────────────────────────────
    val onboarding_step_format: String,       // %1$d, %2$d
    val onboarding_step1_title: String,
    val onboarding_intent_find: String,
    val onboarding_intent_test: String,
    val onboarding_intent_both: String,
    val onboarding_step2_title: String,
    val onboarding_step2_help: String,
    val onboarding_step3_title: String,
    val onboarding_step3_help: String,
    val onboarding_cta_done: String,
    val onboarding_submit_error_prefix: String,

    // ── Home ───────────────────────────────────────────────────────────
    val home_greeting: String,                // %1$s = name
    val home_credits_suffix: String,          // %1$d = count
    val home_section_today: String,
    val home_section_active_tests: String,    // %1$d
    val home_section_your_apps: String,       // %1$d
    val home_new_match_label: String,
    val home_testers_needed: String,          // %1$d
    val home_no_match_today: String,
    val home_day_progress: String,            // %1$d / %2$d
    val home_ping_overdue: String,
    val home_owned_testers: String,           // %1$d / %2$d
    val home_cta_join: String,
    val home_cta_skip_match: String,

    // ── App detail ─────────────────────────────────────────────────────
    val appdetail_default_title: String,
    val appdetail_by_owner: String,           // %1$s = name
    val appdetail_screenshots_placeholder: String,  // %1$d
    val appdetail_requirements_title: String,
    val appdetail_req_days_testers: String,   // %1$d, %2$d
    val appdetail_req_daily: String,          // %1$d minutes
    val appdetail_req_current: String,        // %1$d / %2$d
    val appdetail_explainability_title: String,
    val appdetail_cta_join_credits: String,
    val appdetail_cta_join_opening: String,
    val appdetail_maybe_later: String,

    // ── My apps ────────────────────────────────────────────────────────
    val myapps_title: String,
    val myapps_fab_create: String,
    val myapps_empty_title: String,
    val myapps_empty_desc: String,
    val myapps_empty_cta: String,
    val myapps_row_supporting: String,        // %1$s status, %2$d cur, %3$d req
    val myapps_days_left: String,             // %1$d

    // ── App editor ─────────────────────────────────────────────────────
    val editor_title_create: String,
    val editor_title_edit: String,
    val editor_field_package: String,
    val editor_field_name: String,
    val editor_field_description: String,
    val editor_field_desc_counter: String,    // %1$d/%2$d
    val editor_field_play_url: String,
    val editor_field_play_url_help_empty: String,
    val editor_field_play_url_help_valid: String,
    val editor_field_play_url_help_invalid: String,  // %1$s reason
    val editor_field_required_testers: String,
    val editor_field_required_days: String,
    val editor_cost_label: String,
    val editor_save_error_prefix: String,

    // ── Testing ────────────────────────────────────────────────────────
    val testing_title: String,
    val testing_filter_active: String,
    val testing_filter_done: String,
    val testing_filter_all: String,
    val testing_section_active: String,       // %1$d
    val testing_section_completed: String,    // %1$d
    val testing_empty_title: String,
    val testing_empty_desc: String,           // %1$s eta
    val testing_active_supporting: String,    // %1$d, %2$d
    val testing_active_supporting_atrisk: String,  // %1$d, %2$d
    val testing_cta_heartbeat: String,
    val testing_cta_abandon: String,
    val testing_completed_row: String,        // %1$s, %2$d, %3$d
    val testing_view_proof: String,
    val testing_proof_pending: String,

    // ── Profile ────────────────────────────────────────────────────────
    val profile_title: String,
    val profile_credits: String,              // %1$d
    val profile_stats_title: String,
    val profile_stats_completed: String,
    val profile_stats_days: String,
    val profile_stats_rep: String,
    val profile_stats_streak: String,
    val profile_breakdown_title: String,
    val profile_breakdown_completion: String,
    val profile_breakdown_streak: String,
    val profile_breakdown_volume: String,
    val profile_breakdown_publish: String,
    val profile_breakdown_penalty: String,    // %1$d
    val profile_proofs_title: String,         // %1$d
    val profile_proofs_tap: String,
    val profile_proofs_empty: String,
    val profile_activity_title: String,
    val profile_invite_cta: String,
    val profile_action_inbox: String,
    val profile_action_settings: String,

    // ── Inbox ──────────────────────────────────────────────────────────
    val inbox_title: String,
    val inbox_mark_all_read: String,
    val inbox_empty_title: String,
    val inbox_empty_desc: String,
    val time_just_now: String,
    val time_min_ago: String,                  // %1$d
    val time_hour_ago: String,                 // %1$d
    val time_day_ago: String,                  // %1$d

    // ── Settings ──────────────────────────────────────────────────────
    val settings_title: String,
    val settings_locale_note: String,
    val cta_sign_out: String,

    // ── Nav tabs ──────────────────────────────────────────────────────
    val nav_tab_home: String,
    val nav_tab_my_apps: String,
    val nav_tab_testing: String,
    val nav_tab_profile: String,

    // ── App detail screenshots ─────────────────────────────────────────
    val appdetail_screenshots_count: String,  // {0} screenshots

    // ── Profile streak value ──────────────────────────────────────────
    val profile_stats_streak_value: String,   // {0} 🔥

    // ── Errors (per AppError sealed) ───────────────────────────────────
    val err_network: String,
    val err_http: String,                     // %1$d
    val err_auth: String,
    val err_validation: String,
    val err_not_found: String,
    val err_forbidden: String,
    val err_conflict: String,
    val err_rate_limited: String,
    val err_unknown: String,
)

object AppStringsCatalog {

    val EN = AppStrings(
        appName = "AppTest",
        cta_back = "Back",
        cta_retry = "Retry",
        cta_skip = "Skip",
        cta_continue = "Continue →",
        cta_save = "Save",
        cta_cancel = "Cancel",
        cta_saving = "Saving…",
        state_loading = "Loading…",
        state_loading_default_msg = "",

        signin_social_proof = "Join 1,238 Android devs\ntesting each other's apps",
        signin_cta_google = "Continue with Google",
        signin_cta_email = "Sign in with email",
        signin_terms = "By continuing, you accept Terms · Privacy",
        signin_email_title = "Sign in with email",
        signin_email_label = "Email",
        signin_cta_send_link = "Send magic link",
        signin_signing_in = "Signing in…",
        signin_link_sent_title = "✓ Magic link sent",
        signin_link_sent_body = "Check %1\$s for a sign-in link (≤ 30s).\nTap the link from any device to continue.",
        signin_change_email = "Change email",
        signin_failed_title = "Sign-in failed",

        verify_progress = "Verifying %1\$s…",
        verify_failed_title = "Verification failed",
        verify_signed_in_loading = "Signed in! Loading…",

        onboarding_step_format = "Step %1\$d of %2\$d",
        onboarding_step1_title = "Why are you here?",
        onboarding_intent_find = "Find testers for my app",
        onboarding_intent_test = "Test others' apps",
        onboarding_intent_both = "Both",
        onboarding_step2_title = "Pick categories you'll test",
        onboarding_step2_help = "At least 1 — shapes your home feed.",
        onboarding_step3_title = "Language preferences",
        onboarding_step3_help = "Influences matching only, not UI.",
        onboarding_cta_done = "Done",
        onboarding_submit_error_prefix = "Couldn't continue: ",

        home_greeting = "Hi, %1\$s",
        home_credits_suffix = " · %1\$d cr",
        home_section_today = "Today",
        home_section_active_tests = "Active tests (%1\$d)",
        home_section_your_apps = "Your apps (%1\$d)",
        home_new_match_label = "★ New match",
        home_testers_needed = "%1\$d testers needed",
        home_no_match_today = "No new match today — next batch at 02:00 UTC",
        home_day_progress = "Day %1\$d/%2\$d",
        home_ping_overdue = " · ⚠ ping overdue",
        home_owned_testers = "%1\$d/%2\$d testers",
        home_cta_join = "Join",
        home_cta_skip_match = "Skip",

        appdetail_default_title = "App detail",
        appdetail_by_owner = "by %1\$s",
        appdetail_screenshots_placeholder = "%1\$d screenshots (TODO carousel — V2)",
        appdetail_requirements_title = "Requirements",
        appdetail_req_days_testers = "%1\$d days · %2\$d testers needed",
        appdetail_req_daily = "Daily: 1 launch + ~%1\$d min",
        appdetail_req_current = "Currently: %1\$d/%2\$d testers",
        appdetail_explainability_title = "Why you got this match",
        appdetail_cta_join_credits = "Join test (1 credit)",
        appdetail_cta_join_opening = "Opening Play Store…",
        appdetail_maybe_later = "Maybe later — back to feed",

        myapps_title = "My apps",
        myapps_fab_create = "Create",
        myapps_empty_title = "No apps yet",
        myapps_empty_desc = "First one is free — create it to start finding testers.",
        myapps_empty_cta = "Create app",
        myapps_row_supporting = "%1\$s · %2\$d/%3\$d testers",
        myapps_days_left = " · %1\$dd left",

        editor_title_create = "Create app",
        editor_title_edit = "Edit app",
        editor_field_package = "Package name *",
        editor_field_name = "App name * (2–50)",
        editor_field_description = "Description (0/500)",
        editor_field_desc_counter = "%1\$d/%2\$d",
        editor_field_play_url = "Play Console opt-in URL *",
        editor_field_play_url_help_empty = "Required (https://play.google.com/...)",
        editor_field_play_url_help_valid = "✓ Valid",
        editor_field_play_url_help_invalid = "✗ %1\$s",
        editor_field_required_testers = "Required testers (1–100)",
        editor_field_required_days = "Required days (7–30)",
        editor_cost_label = "Cost: 1 credit (your first app is free)",
        editor_save_error_prefix = "Save failed: ",

        testing_title = "My tests",
        testing_filter_active = "Active",
        testing_filter_done = "Done",
        testing_filter_all = "All",
        testing_section_active = "Active (%1\$d)",
        testing_section_completed = "Completed (%1\$d)",
        testing_empty_title = "No active tests yet",
        testing_empty_desc = "Next batch run: in %1\$s.",
        testing_active_supporting = "Day %1\$d/%2\$d",
        testing_active_supporting_atrisk = "Day %1\$d/%2\$d · ⚠ ping overdue",
        testing_cta_heartbeat = "Heartbeat now",
        testing_cta_abandon = "Abandon",
        testing_completed_row = "%1\$s  ✓ %2\$d days · +%3\$d rep",
        testing_view_proof = "View proof card →",
        testing_proof_pending = "Proof pending",

        profile_title = "Profile",
        profile_credits = "%1\$d credits",
        profile_stats_title = "Stats (last 30 days)",
        profile_stats_completed = "Completed",
        profile_stats_days = "Days",
        profile_stats_rep = "Rep Δ",
        profile_stats_streak = "Streak",
        profile_breakdown_title = "Reputation breakdown",
        profile_breakdown_completion = "Completion rate",
        profile_breakdown_streak = "Streak",
        profile_breakdown_volume = "Volume",
        profile_breakdown_publish = "Publish",
        profile_breakdown_penalty = "(penalties: -%1\$d)",
        profile_proofs_title = "Proof cards (%1\$d)",
        profile_proofs_tap = "Tap to view the latest proof card",
        profile_proofs_empty = "Complete a test to earn a proof card",
        profile_activity_title = "Activity history",
        profile_invite_cta = "Invite a developer friend",
        profile_action_inbox = "Inbox",
        profile_action_settings = "Settings",

        inbox_title = "Inbox",
        inbox_mark_all_read = "Mark all read",
        inbox_empty_title = "Inbox is empty",
        inbox_empty_desc = "Match alerts + heartbeat reminders show up here.",
        time_just_now = "just now",
        time_min_ago = "%1\$dm ago",
        time_hour_ago = "%1\$dh ago",
        time_day_ago = "%1\$dd ago",

        settings_title = "Settings",
        settings_locale_note = "Locale follows system (toggle ships in V2).",
        cta_sign_out = "Sign out",
        nav_tab_home = "Home",
        nav_tab_my_apps = "My Apps",
        nav_tab_testing = "Testing",
        nav_tab_profile = "Profile",
        appdetail_screenshots_count = "%1\$d screenshots",
        profile_stats_streak_value = "%1\$d 🔥",

        err_network = "Connection problem",
        err_http = "Server error (%1\$d)",
        err_auth = "Sign-in needed",
        err_validation = "Invalid input",
        err_not_found = "Not found",
        err_forbidden = "Not allowed",
        err_conflict = "Conflict",
        err_rate_limited = "Too many requests",
        err_unknown = "Something went wrong",
    )

    val ZH_TW = AppStrings(
        appName = "AppTest",
        cta_back = "返回",
        cta_retry = "重試",
        cta_skip = "略過",
        cta_continue = "繼續 →",
        cta_save = "儲存",
        cta_cancel = "取消",
        cta_saving = "儲存中…",
        state_loading = "載入中…",
        state_loading_default_msg = "",

        signin_social_proof = "加入 1,238 位 Android 開發者\n互測彼此的 App",
        signin_cta_google = "用 Google 帳號繼續",
        signin_cta_email = "用 Email 登入",
        signin_terms = "繼續即表示同意 服務條款 · 隱私權",
        signin_email_title = "用 Email 登入",
        signin_email_label = "Email",
        signin_cta_send_link = "寄出登入連結",
        signin_signing_in = "登入中…",
        signin_link_sent_title = "✓ 登入連結已寄出",
        signin_link_sent_body = "請查收 %1\$s 的登入連結（≤ 30 秒）。\n從任何裝置點開連結即可繼續。",
        signin_change_email = "改用其他 Email",
        signin_failed_title = "登入失敗",

        verify_progress = "正在驗證 %1\$s…",
        verify_failed_title = "驗證失敗",
        verify_signed_in_loading = "登入成功！載入中…",

        onboarding_step_format = "第 %1\$d 步（共 %2\$d 步）",
        onboarding_step1_title = "你為什麼來這？",
        onboarding_intent_find = "為我的 App 找 tester",
        onboarding_intent_test = "去測別人的 App",
        onboarding_intent_both = "兩者都要",
        onboarding_step2_title = "選你願意測的類別",
        onboarding_step2_help = "至少選 1 個 — 決定首頁配對。",
        onboarding_step3_title = "語言偏好",
        onboarding_step3_help = "只影響配對，不影響介面語言。",
        onboarding_cta_done = "完成",
        onboarding_submit_error_prefix = "無法繼續：",

        home_greeting = "嗨, %1\$s",
        home_credits_suffix = " · %1\$d 點",
        home_section_today = "今日",
        home_section_active_tests = "進行中的測試 (%1\$d)",
        home_section_your_apps = "你的 App (%1\$d)",
        home_new_match_label = "★ 新配對",
        home_testers_needed = "還需 %1\$d 位 tester",
        home_no_match_today = "今日無新配對 — 下次批次配對 02:00 UTC",
        home_day_progress = "第 %1\$d / %2\$d 天",
        home_ping_overdue = " · ⚠ 今日尚未回報",
        home_owned_testers = "%1\$d / %2\$d 位 tester",
        home_cta_join = "加入",
        home_cta_skip_match = "略過",

        appdetail_default_title = "App 詳情",
        appdetail_by_owner = "by %1\$s",
        appdetail_screenshots_placeholder = "%1\$d 張截圖（carousel V2 補）",
        appdetail_requirements_title = "需求",
        appdetail_req_days_testers = "%1\$d 天 · 需 %2\$d 位 tester",
        appdetail_req_daily = "每日：開啟 1 次 + 約 %1\$d 分鐘",
        appdetail_req_current = "目前：%1\$d / %2\$d 位 tester",
        appdetail_explainability_title = "為什麼配對到你",
        appdetail_cta_join_credits = "加入測試（1 點）",
        appdetail_cta_join_opening = "正在開 Play Store…",
        appdetail_maybe_later = "之後再說 — 返回首頁",

        myapps_title = "我的 Apps",
        myapps_fab_create = "新增",
        myapps_empty_title = "還沒有任何 App",
        myapps_empty_desc = "第一個 App 免費 — 建立後即可找 tester。",
        myapps_empty_cta = "建立 App",
        myapps_row_supporting = "%1\$s · %2\$d / %3\$d 位 tester",
        myapps_days_left = " · 剩 %1\$d 天",

        editor_title_create = "建立 App",
        editor_title_edit = "編輯 App",
        editor_field_package = "Package name（必填）",
        editor_field_name = "App 名稱（必填，2–50 字）",
        editor_field_description = "描述（0/500）",
        editor_field_desc_counter = "%1\$d / %2\$d",
        editor_field_play_url = "Play Console 加入測試 URL（必填）",
        editor_field_play_url_help_empty = "必填（https://play.google.com/...）",
        editor_field_play_url_help_valid = "✓ 有效",
        editor_field_play_url_help_invalid = "✗ %1\$s",
        editor_field_required_testers = "需要 tester 數（1–100）",
        editor_field_required_days = "需要天數（7–30）",
        editor_cost_label = "費用：1 點（首個 App 免費）",
        editor_save_error_prefix = "儲存失敗：",

        testing_title = "我的測試",
        testing_filter_active = "進行中",
        testing_filter_done = "已完成",
        testing_filter_all = "全部",
        testing_section_active = "進行中 (%1\$d)",
        testing_section_completed = "已完成 (%1\$d)",
        testing_empty_title = "目前沒有進行中的測試",
        testing_empty_desc = "下次配對批次：%1\$s 後。",
        testing_active_supporting = "第 %1\$d / %2\$d 天",
        testing_active_supporting_atrisk = "第 %1\$d / %2\$d 天 · ⚠ 今日尚未回報",
        testing_cta_heartbeat = "立即回報",
        testing_cta_abandon = "放棄",
        testing_completed_row = "%1\$s  ✓ %2\$d 天 · +%3\$d 信用",
        testing_view_proof = "查看完成證明 →",
        testing_proof_pending = "證明處理中",

        profile_title = "個人",
        profile_credits = "%1\$d 點",
        profile_stats_title = "近 30 天統計",
        profile_stats_completed = "完成",
        profile_stats_days = "天數",
        profile_stats_rep = "信用 Δ",
        profile_stats_streak = "連勝",
        profile_breakdown_title = "信用分數拆解",
        profile_breakdown_completion = "完成率",
        profile_breakdown_streak = "連勝",
        profile_breakdown_volume = "次數",
        profile_breakdown_publish = "發佈",
        profile_breakdown_penalty = "（扣分：-%1\$d）",
        profile_proofs_title = "完成證明 (%1\$d)",
        profile_proofs_tap = "點選查看最新證明卡",
        profile_proofs_empty = "完成一次測試即可獲得證明卡",
        profile_activity_title = "活動歷史",
        profile_invite_cta = "邀請開發者朋友",
        profile_action_inbox = "通知",
        profile_action_settings = "設定",

        inbox_title = "通知",
        inbox_mark_all_read = "全部標為已讀",
        inbox_empty_title = "通知箱是空的",
        inbox_empty_desc = "配對提醒與每日 heartbeat 提醒會出現在這裡。",
        time_just_now = "剛剛",
        time_min_ago = "%1\$d 分鐘前",
        time_hour_ago = "%1\$d 小時前",
        time_day_ago = "%1\$d 天前",

        settings_title = "設定",
        settings_locale_note = "語系切換 V2 開放，目前跟隨系統。",
        cta_sign_out = "登出",
        nav_tab_home = "首頁",
        nav_tab_my_apps = "我的 Apps",
        nav_tab_testing = "測試",
        nav_tab_profile = "個人",
        appdetail_screenshots_count = "%1\$d 張截圖",
        profile_stats_streak_value = "%1\$d 🔥",

        err_network = "網路有問題",
        err_http = "伺服器錯誤 (%1\$d)",
        err_auth = "需要登入",
        err_validation = "輸入有誤",
        err_not_found = "找不到資料",
        err_forbidden = "權限不足",
        err_conflict = "狀態衝突",
        err_rate_limited = "請求太頻繁",
        err_unknown = "出了點問題",
    )

    /**
     * Pick locale by language tag. Defaults to EN for anything other than zh-TW / zh-Hant.
     * Use IETF language tag form (e.g. "zh-TW", "en-US"). Compare on the language + region
     * to match Android's `Configuration.locales[0].toLanguageTag()`.
     */
    fun pick(languageTag: String): AppStrings {
        val lower = languageTag.lowercase()
        return when {
            lower.startsWith("zh") -> ZH_TW  // zh, zh-TW, zh-Hant, zh-CN 等都先給 ZH_TW（V1 簡化；V2 區分簡繁）
            else -> EN
        }
    }
}
