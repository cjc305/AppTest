package com.apptest.core.common

/**
 * Reputation tier value enum per `_specs/reputation_system.md` §1.
 *
 * Lives in `:core:common` (not `:core:domain`) because **both** UI (`AppTierBadge`) and
 * domain (`Reputation` computation) need it. `:core:domain` doesn't depend on `:core:common`'s
 * superset, just shares this enum without creating ui→domain coupling.
 *
 * Tier breakpoints (inclusive):
 * - Newcomer: 0-199   (first 7 days only)
 * - Bronze:   200-399
 * - Silver:   400-599
 * - Gold:     600-799
 * - Platinum: 800-1000
 */
enum class ReputationTier {
    Newcomer, Bronze, Silver, Gold, Platinum;

    companion object {
        /** Map a score to its tier. New-account `Newcomer` time-window check is caller's responsibility. */
        fun fromScore(score: Int): ReputationTier = when {
            score >= 800 -> Platinum
            score >= 600 -> Gold
            score >= 400 -> Silver
            score >= 200 -> Bronze
            else -> Newcomer
        }
    }
}
