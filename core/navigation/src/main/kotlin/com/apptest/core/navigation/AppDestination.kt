package com.apptest.core.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe Compose Navigation 2.8+ destination contract per `_specs/navigation.md §3`.
 *
 * Only **cross-feature public** destinations live here. Feature-internal nav targets stay
 * inside each feature's `nav/` subpackage (per `feature_modules.md §3`).
 *
 * Hard rules:
 * - Every entry is `@Serializable` so `composable<T>(...)` / `navigation<T>(...)` work
 *   without string routes (per modularization.md §11 forbidden-pattern: no string nav).
 * - `data object` for parameterless destinations; `data class` for those with args.
 * - Args are primitive or @Serializable types only — never Parcelable / Bundle.
 */
sealed interface AppDestination {

    // ── Top-level subgraphs ────────────────────────────────────────────────
    @Serializable data object AuthRoot       : AppDestination
    @Serializable data object OnboardingRoot : AppDestination
    @Serializable data object MainRoot       : AppDestination

    // ── Main subgraph children (bottom-bar tabs) ────────────────────────────
    @Serializable data object Home    : AppDestination
    @Serializable data object MyApps  : AppDestination
    @Serializable data object Testing : AppDestination
    @Serializable data object Profile : AppDestination

    // ── Pushed-on-top of main subgraph ─────────────────────────────────────
    @Serializable data class  AppDetail(val appId: String)               : AppDestination
    @Serializable data class  AppEditor(val appId: String? = null)       : AppDestination  // null = create
    @Serializable data object Inbox    : AppDestination
    @Serializable data object Settings : AppDestination
    @Serializable data class  ProofViewer(val proofId: String) : AppDestination

    // ── Auth subgraph children ─────────────────────────────────────────────
    @Serializable data object SignIn      : AppDestination
    @Serializable data class  EmailVerify(val email: String) : AppDestination
}
