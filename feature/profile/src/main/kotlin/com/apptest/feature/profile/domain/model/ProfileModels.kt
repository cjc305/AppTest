package com.apptest.feature.profile.domain.model

import com.apptest.core.common.ReputationTier

data class ProfileUser(
    val id: String,
    val displayName: String,
    val photoUrl: String?,
    val tier: ReputationTier,
    val credits: Int,
)

data class ProfileStats30d(
    val completedTests: Int,
    val daysContributed: Int,
    val reputationDelta: Int,        // can be negative
    val streakDays: Int,
)

/** Sub-score breakdown of current reputation. Each maxValue = max from formula §2 of reputation_system.md. */
data class ReputationBreakdown(
    val completionRate: Int,          // 0..40
    val streak: Int,                  // 0..20
    val volume: Int,                  // 0..15
    val publish: Int,                 // 0..25
    val penalty: Int,                 // 0..150 (sum of abandonment + fraud)
) {
    val total: Int get() = completionRate + streak + volume + publish - penalty
}

data class ProofCardSummary(
    val proofId: String,
    val appName: String,
    val completedAt: String,          // ISO-ish display string for V1
)

enum class ActivityEventKind { ReputationGain, ReputationLoss, CreditSpent, AppPublished, TierChange }

data class ActivityEvent(
    val id: String,
    val kind: ActivityEventKind,
    val label: String,
    val timestampDisplay: String,
)

data class ProfileData(
    val user: ProfileUser,
    val stats: ProfileStats30d,
    val breakdown: ReputationBreakdown,
    val proofs: List<ProofCardSummary>,
    val activity: List<ActivityEvent>,
)
