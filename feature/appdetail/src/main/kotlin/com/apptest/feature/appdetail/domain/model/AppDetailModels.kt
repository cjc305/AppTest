package com.apptest.feature.appdetail.domain.model

import com.apptest.core.common.ReputationTier

/**
 * Feature-local domain types. App detail aggregate flattens `apps` + `profiles(owner)` +
 * `match_run_pairs(score_breakdown)` per `_specs/database_schema.md` into one read model.
 */

/** Owner display per anonymity floor (hard rule §9): tier only, no historical apps. */
data class OwnerInfo(
    val displayName: String,
    val tier: ReputationTier,
)

/** App requirements + current participation snapshot. */
data class Requirements(
    val requiredDays: Int,
    val requiredTesters: Int,
    val currentTesters: Int,
    val dailyMinutesEstimated: Int,
)

/** One human-readable contributing factor to the match. V1: top-3 from rule-based scoring. */
data class MatchReason(val label: String)

/** Aggregate. Repository returns this; ViewModel maps to UiState. */
data class AppDetailData(
    val id: String,
    val packageName: String,
    val name: String,
    val category: String,
    val description: String,
    val iconUrl: String?,
    val screenshotCount: Int,           // V1: just count, no images
    val screenshotUrls: List<String> = emptyList(), // V1: carousel images (FakeRepo provides picsum placeholders)
    val owner: OwnerInfo,
    val requirements: Requirements,
    val matchReasons: List<MatchReason>, // 0..3 entries
    val playOptInUrl: String,
)
