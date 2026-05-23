package com.apptest.feature.home.data

import com.apptest.core.common.AppError
import com.apptest.core.common.AppResult
import com.apptest.core.common.DispatcherProvider
import com.apptest.core.common.ReputationTier
import com.apptest.core.network.apps.SupabaseAppsApiService
import com.apptest.core.network.matches.ActiveMatchWithAppDto
import com.apptest.core.network.matches.MatchWithAppDto
import com.apptest.core.network.matches.SupabaseMatchesApiService
import com.apptest.core.network.profiles.ProfileDto
import com.apptest.core.network.profiles.SupabaseProfilesApiService
import com.apptest.feature.home.domain.model.ActiveTest
import com.apptest.feature.home.domain.model.HomeData
import com.apptest.feature.home.domain.model.HomeUser
import com.apptest.feature.home.domain.model.MatchedApp
import com.apptest.feature.home.domain.model.OwnedApp
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Real Supabase-backed [HomeRepository]. Replaces [FakeHomeRepository] via [HomeDataModule].
 *
 * Makes 4 parallel calls: profile, new match, active tests, owned apps.
 * V1 simplifications: matchScore=0, day=0, totalDays=14, pingStatusOk=true (no heartbeat col).
 */
@Singleton
class SupabaseHomeRepository @Inject constructor(
    private val profilesApi: SupabaseProfilesApiService,
    private val matchesApi: SupabaseMatchesApiService,
    private val appsApi: SupabaseAppsApiService,
    private val dispatchers: DispatcherProvider,
) : HomeRepository {

    /**
     * Partial-success aggregation: each of the 4 calls is independently bounded by [PER_CALL_MS]
     * and wrapped in runCatching so a single slow or failing endpoint can't blank out the whole
     * Home screen. Profile failure still falls back to a placeholder user (not Failure).
     *
     * Returns [AppResult.Failure] only when the wrapping coroutine itself crashes — individual
     * endpoint failures degrade gracefully to defaults / empty lists.
     */
    override suspend fun getHomeData(): AppResult<HomeData> = withContext(dispatchers.io) {
        try {
            val profileDeferred = async { runCatching { withTimeoutOrNull(PER_CALL_MS) { profilesApi.getMyProfile() } } }
            val matchDeferred   = async { runCatching { withTimeoutOrNull(PER_CALL_MS) { matchesApi.getNewMatch() } } }
            val activeDeferred  = async { runCatching { withTimeoutOrNull(PER_CALL_MS) { matchesApi.getActiveTests() } } }
            val appsDeferred    = async { runCatching { withTimeoutOrNull(PER_CALL_MS) { appsApi.listOwned() } } }

            val profileResult = profileDeferred.await()
            val matchResult   = matchDeferred.await()
            val activeResult  = activeDeferred.await()
            val appsResult    = appsDeferred.await()

            // MED-014: if ALL 4 calls failed (network down, auth expired, etc.) surface as Failure
            // so the HomeScreen shows a retry button instead of silently showing placeholder data.
            val allFailed = profileResult.isFailure && matchResult.isFailure &&
                activeResult.isFailure && appsResult.isFailure
            if (allFailed) {
                val first = profileResult.exceptionOrNull() ?: Exception("All home calls failed")
                return@withContext AppResult.Failure(AppError.fromThrowable(first))
            }

            AppResult.Success(HomeData(
                user = profileResult.getOrNull()?.firstOrNull()?.toHomeUser()
                    ?: HomeUser("User", ReputationTier.Newcomer, 0),
                newMatch = matchResult.getOrNull()?.firstOrNull()?.toMatchedApp(),
                activeTests = activeResult.getOrNull().orEmpty().map { it.toActiveTest() },
                myApps = appsResult.getOrNull().orEmpty().map {
                    OwnedApp(id = it.id, name = it.name, currentTesters = 0, requiredTesters = 12)
                },
            ))
        } catch (c: CancellationException) {
            throw c
        } catch (t: Throwable) {
            AppResult.Failure(AppError.fromThrowable(t))
        }
    }

    private companion object {
        const val PER_CALL_MS = 5_000L
    }
}

// ─── Mapping helpers ──────────────────────────────────────────────────────────

private fun ProfileDto.toHomeUser() = HomeUser(
    displayName = displayName,
    tier = ReputationTier.entries.firstOrNull { it.name == tier.uppercase() }
        ?: ReputationTier.Newcomer,
    credits = 0, // V1: credits column not in DB
)

private fun MatchWithAppDto.toMatchedApp() = MatchedApp(
    id = appId,
    name = apps?.name ?: "",
    category = apps?.category ?: "",
    description = apps?.description ?: "",
    testersNeeded = 12, // V1: required_testers not in DB
    matchScore = 0, // V1: match_score not in DB
)

private fun ActiveMatchWithAppDto.toActiveTest() = ActiveTest(
    appId = appId,
    appName = apps?.name ?: "",
    day = 0, // V1: days_active not in DB
    totalDays = 14, // V1: required_days not in DB
    pingStatusOk = true, // V1: last_heartbeat_at not in DB — default to ok
)
