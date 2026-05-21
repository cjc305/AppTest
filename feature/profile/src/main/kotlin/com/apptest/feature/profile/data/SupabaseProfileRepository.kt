package com.apptest.feature.profile.data

import com.apptest.core.common.AppError
import com.apptest.core.common.AppResult
import com.apptest.core.common.DispatcherProvider
import com.apptest.core.common.ReputationTier
import com.apptest.core.network.matches.SupabaseMatchesApiService
import com.apptest.core.network.profiles.ProfileDto
import com.apptest.core.network.profiles.SupabaseProfilesApiService
import com.apptest.feature.profile.domain.model.ActivityEvent
import com.apptest.feature.profile.domain.model.ProfileData
import com.apptest.feature.profile.domain.model.ProfileStats30d
import com.apptest.feature.profile.domain.model.ProfileUser
import com.apptest.feature.profile.domain.model.ProofCardSummary
import com.apptest.feature.profile.domain.model.ReputationBreakdown
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

/**
 * Real Supabase-backed [ProfileRepository]. Replaces [FakeProfileRepository].
 *
 * Makes 2 parallel calls: profile row + completed tests count.
 * V1 simplifications: proofs=[] (no proofs table yet), reputationDelta=0, activity=[].
 */
@Singleton
class SupabaseProfileRepository @Inject constructor(
    private val profilesApi: SupabaseProfilesApiService,
    private val matchesApi: SupabaseMatchesApiService,
    private val dispatchers: DispatcherProvider,
) : ProfileRepository {

    override suspend fun getMyProfile(): AppResult<ProfileData> = withContext(dispatchers.io) {
        try {
            val profileDeferred = async { profilesApi.getMyProfile() }
            val completedDeferred = async { matchesApi.getCompletedTests() }

            val profileDto = profileDeferred.await().firstOrNull()
                ?: return@withContext AppResult.Failure(AppError.NotFound("profile"))
            val completedTests = completedDeferred.await()

            val profileData = ProfileData(
                user = profileDto.toUser(),
                stats = ProfileStats30d(
                    completedTests = completedTests.size,
                    daysContributed = completedTests.size * 14, // V1: estimate 14d per test
                    reputationDelta = 0,
                    streakDays = 0, // V1: streak_days not in DB
                ),
                breakdown = profileDto.reputationScore.toBreakdown(),
                proofs = emptyList<ProofCardSummary>(), // V1: proofs table not yet created
                activity = emptyList<ActivityEvent>(),
            )
            AppResult.Success(profileData)
        } catch (c: CancellationException) {
            throw c
        } catch (t: Throwable) {
            AppResult.Failure(AppError.fromThrowable(t))
        }
    }
}

// ─── Mapping helpers ──────────────────────────────────────────────────────────

private fun ProfileDto.toUser() = ProfileUser(
    id = id,
    displayName = displayName,
    photoUrl = avatarUrl,
    tier = ReputationTier.entries.firstOrNull { it.name == tier.uppercase() }
        ?: ReputationTier.Newcomer,
    credits = 0, // V1: credits column not in DB
)

private fun Int.toBreakdown(): ReputationBreakdown {
    val fraction = (coerceAtLeast(0).toFloat() / 1000f).coerceIn(0f, 1f)
    return ReputationBreakdown(
        completionRate = (40 * fraction).toInt(),
        streak = (20 * fraction).toInt(),
        volume = (15 * fraction).toInt(),
        publish = (25 * fraction).toInt(),
        penalty = 0,
    )
}
