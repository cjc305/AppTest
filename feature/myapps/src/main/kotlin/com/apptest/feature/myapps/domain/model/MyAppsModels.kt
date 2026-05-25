package com.apptest.feature.myapps.domain.model

/**
 * Feature-local types per `_specs/feature_modules.md §3`. Reflect canonical entities from
 * `_specs/database_schema.md §3 apps` but flatten what dev list + editor actually need.
 *
 * If any of these grow shared by ≥ 2 features, lift up to `:core:domain`.
 */

enum class OwnedAppStatus { Recruiting, Active, Completed, Paused }

/**
 * Row item for My apps list AND editor source-of-truth on edit mode.
 *
 * HIGH-005 (audit 2026-05-23): `description` + `playOptInUrl` added. Previously the editor
 * built its AppDraft from this row and defaulted both fields to "" → user opened edit and
 * saw their saved description/URL replaced with blanks; PlayUrlValidation.Empty then made
 * the Save button non-clickable.
 */
data class OwnedAppRow(
    val id: String,
    val name: String,
    val packageName: String,
    val description: String,            // HIGH-005: was dropped on edit
    val playOptInUrl: String,           // HIGH-005: was dropped on edit
    val status: OwnedAppStatus,
    val currentTesters: Int,
    val requiredTesters: Int,
    val requiredDays: Int,
    val daysLeft: Int,                  // 0 when not active yet
    /** Plan C: Google Group used for Play Console auto-sync. Blank = sync disabled. */
    val testingGroupEmail: String = "",
)

/** Editor form draft. Used by both create (id=null) and edit (id=existing). */
data class AppDraft(
    val id: String? = null,
    val packageName: String = "",
    val name: String = "",
    val description: String = "",
    val playOptInUrl: String = "",
    val requiredTesters: Int = 12,
    val requiredDays: Int = 14,
    /** Plan C: optional Google Group for Play Console auto-sync. Blank = disabled. */
    val testingGroupEmail: String = "",
)

/** Validation result for the [AppDraft.playOptInUrl] field. */
sealed interface PlayUrlValidation {
    data object Empty : PlayUrlValidation
    data object Valid : PlayUrlValidation
    data class Invalid(val reason: String) : PlayUrlValidation
}

/**
 * Result of [com.apptest.feature.myapps.data.MyAppsRepository]'s initial network load.
 * Used to distinguish "loaded empty list" (show Empty state) from "load failed" (show Error).
 */
sealed interface MyAppsLoadStatus {
    data object Idle : MyAppsLoadStatus       // never tried (no subscriber yet)
    data object Loading : MyAppsLoadStatus    // request in flight
    data object Loaded : MyAppsLoadStatus     // success — observe() emits real data
    data class Failed(val error: com.apptest.core.common.AppError) : MyAppsLoadStatus
}
