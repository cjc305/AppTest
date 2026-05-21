package com.apptest.feature.home.data

import com.apptest.core.common.AppError
import com.apptest.core.common.AppResult
import com.apptest.core.common.DispatcherProvider
import com.apptest.core.common.ReputationTier
import com.apptest.core.network.apps.SupabaseAppsApiService
import com.apptest.core.network.matches.MatchWithAppDto
import com.apptest.core.network.matches.ActiveMatchWithAppDto
import com.apptest.core.network.matches.SupabaseMatchesApiService
import com.apptest.core.network.profiles.ProfileDto
import com.apptest.core.network.profiles.SupabaseProfilesApiService
import com.apptest.feature.home.domain.model.ActiveTest
import com.apptest.feature.home.domain.model.HomeData
import com.apptest.feature.home.domain.model.HomeUser
import com.apptest.feature.home.domain.model.MatchedApp
import com.apptest.feature.home.domain.model.OwnedApp
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

/**
 * Real Supabase-backed [HomeRepository]. Replaces [FakeHomeRepository] via [HomeDataModule].
 *
 * Makes 4 parallel calls: profile, new match, active tests, owned apps.
 * RLS scopes each query to the authenticated user — no explicit user-id filter needed.
 */
@Singleton
class SupabaseHomeRepository @Inject constructor(
    private val profilesApi: SupabaseProfilesApiService,
    private val matchesApi: SupabaseMatchesApiService,
    private val appsApi: SupabaseAppsApiService,
    private val dispatchers: DispatcherProvider,
) : HomeRepository {

    override suspend fun getHomeData(): AppResult<HomeData> = withContext(dispatchers.io) {
        try {
            val profileDeferred = async { profilesApi.getMyProfile() }
            val matchDeferred = async { matchesApi.getNewMatch() }
            val activeDeferred = async { matchesApi.getActiveTests() }
            val appsDeferred = async { appsApi.listOwned() }

            val homeData = HomeData(
                user = profileDeferred.await().firstOrNull()?.toHomeUser()
                    ?: HomeUser("User", ReputationTier.Newcomer, 0),
                newMatch = matchDeferred.await().firstOrNull()?.toMatchedApp(),
                activeTests = activeDeferred.await().map { it.toActiveTest() },
                myApps = appsDeferred.await().map {
                    OwnedApp(
                        id = it.id,
                        name = it.name,
                        currentTesters = 0, // V1: no real-time count
                        requiredTesters = it.requiredTesters,
                    )
                },
            )
            AppResult.Success(homeData)
        } catch (c: CancellationException) {
            throw c
        } catch (t: Throwable) {
            AppResult.Failure(AppError.fromThrowable(t))
        }
    }
}

// ─── Mapping helpers ──────────────────────────────────────────────────────────

private fun ProfileDto.toHomeUser() = HomeUser(
    displayName = displayName,
    tier = ReputationTier.entries.firstOrNull { it.name == reputationTier } ?: ReputationTier.Newcomer,
    credits = credits,
)

private fun MatchWithAppDto.toMatchedApp() = MatchedApp(
    id = appId,
    name = apps?.name ?: "",
    category = apps?.category ?: "",
    description = apps?.description ?: "",
    testersNeeded = apps?.requiredTesters ?: 0,
    matchScore = matchScore,
)

private fun ActiveMatchWithAppDto.toActiveTest() = ActiveTest(
    appId = appId,
    appName = apps?.name ?: "",
    day = daysActive,
    totalDays = apps?.requiredDays ?: 14,
    pingStatusOk = lastHeartbeatAt.isPingOk(),
)

private fun String?.isPingOk(): Boolean {
    if (this == null) return false
    return try {
        Duration.between(Instant.parse(this), Instant.now()).toHours() < 25
    } catch (_: Exception) { false }
}
