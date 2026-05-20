package com.apptest.feature.profile.data

import com.apptest.core.common.AppResult
import com.apptest.core.common.ReputationTier
import com.apptest.feature.profile.domain.model.ActivityEvent
import com.apptest.feature.profile.domain.model.ActivityEventKind
import com.apptest.feature.profile.domain.model.ProfileData
import com.apptest.feature.profile.domain.model.ProfileStats30d
import com.apptest.feature.profile.domain.model.ProfileUser
import com.apptest.feature.profile.domain.model.ProofCardSummary
import com.apptest.feature.profile.domain.model.ReputationBreakdown
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay

@Singleton
class FakeProfileRepository @Inject constructor() : ProfileRepository {

    override suspend fun getMyProfile(): AppResult<ProfileData> {
        delay(200)
        return AppResult.Success(MOCK)
    }

    private companion object {
        val MOCK = ProfileData(
            user = ProfileUser(
                id = "u1",
                displayName = "Alice Chen",
                photoUrl = null,
                tier = ReputationTier.Silver,
                credits = 4,
            ),
            stats = ProfileStats30d(
                completedTests = 8,
                daysContributed = 84,
                reputationDelta = 24,
                streakDays = 5,
            ),
            breakdown = ReputationBreakdown(
                completionRate = 35,
                streak = 16,
                volume = 8,
                publish = 5,
                penalty = 0,
            ),
            proofs = listOf(
                ProofCardSummary("p1", "AppX", "2026-04-30"),
                ProofCardSummary("p2", "AppY", "2026-04-15"),
                ProofCardSummary("p3", "AppZ", "2026-03-20"),
            ),
            activity = listOf(
                ActivityEvent("e1", ActivityEventKind.ReputationGain, "+6 rep · completed AppA", "2h ago"),
                ActivityEvent("e2", ActivityEventKind.CreditSpent, "-1 cr · published AppB", "1d ago"),
                ActivityEvent("e3", ActivityEventKind.TierChange, "Promoted to Silver 🎉", "1mo ago"),
                ActivityEvent("e4", ActivityEventKind.AppPublished, "Published AppA", "1mo ago"),
            ),
        )
    }
}
