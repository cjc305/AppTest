package com.apptest.feature.home.domain.model

import com.apptest.core.common.ReputationTier

/**
 * Feature-local domain types for Home. Per `_specs/feature_modules.md §3`, types used only
 * within this feature live here. Types used by ≥ 2 features should move up to `:core:domain`.
 *
 * Field semantics align with `_specs/database_schema.md` entities but flatten what the Home
 * screen actually needs (no need to carry every DB column into UI).
 */

/** Greeting header model. */
data class HomeUser(
    val displayName: String,
    val tier: ReputationTier,
    val credits: Int,
)

/** Today's newly-matched App (the hero card). One at most per Home load. */
data class MatchedApp(
    val id: String,                 // App.id
    val name: String,
    val category: String,           // human-readable label
    val description: String,        // truncated to 3 lines client-side
    val testersNeeded: Int,         // remaining slots (required - current)
)

/** Active TestRequest entry. */
data class ActiveTest(
    val appId: String,
    val appName: String,
    val day: Int,                   // days_active (1..totalDays)
    val totalDays: Int,             // required_days
    val pingStatusOk: Boolean,      // last_heartbeat_at within 24h
)

/** App owned by current user (developer side). */
data class OwnedApp(
    val id: String,
    val name: String,
    val currentTesters: Int,
    val requiredTesters: Int,
)

/** Aggregate. Repository returns this; ViewModel maps to HomeUiState. */
data class HomeData(
    val user: HomeUser,
    val newMatch: MatchedApp?,
    val activeTests: List<ActiveTest>,
    val myApps: List<OwnedApp>,
)
