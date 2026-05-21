package com.apptest.feature.appdetail.data

import com.apptest.core.common.AppError
import com.apptest.core.common.AppResult
import com.apptest.core.common.DispatcherProvider
import com.apptest.core.common.ReputationTier
import com.apptest.core.network.apps.SupabaseAppsApiService
import com.apptest.feature.appdetail.domain.model.AppDetailData
import com.apptest.feature.appdetail.domain.model.OwnerInfo
import com.apptest.feature.appdetail.domain.model.Requirements
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext

/**
 * Real Supabase-backed [AppDetailRepository]. Replaces [FakeAppDetailRepository].
 *
 * Calls `GET /rest/v1/apps?id=eq.<id>` with embedded owner profile.
 * V1 simplifications: currentTesters=0, screenshotUrls=[], matchReasons=[].
 */
@Singleton
class SupabaseAppDetailRepository @Inject constructor(
    private val appsApi: SupabaseAppsApiService,
    private val dispatchers: DispatcherProvider,
) : AppDetailRepository {

    override suspend fun getById(appId: String): AppResult<AppDetailData> =
        withContext(dispatchers.io) {
            try {
                val rows = appsApi.getById("eq.$appId")
                val app = rows.firstOrNull()
                    ?: return@withContext AppResult.Failure(AppError.NotFound("app"))

                val detail = AppDetailData(
                    id = app.id,
                    packageName = app.packageName,
                    name = app.name,
                    category = app.category,
                    description = app.description,
                    iconUrl = app.iconUrl,
                    screenshotCount = 0,
                    screenshotUrls = emptyList(),
                    owner = OwnerInfo(
                        displayName = app.profiles?.displayName ?: "Developer",
                        tier = ReputationTier.entries
                            .firstOrNull { it.name == app.profiles?.reputationTier }
                            ?: ReputationTier.Newcomer,
                    ),
                    requirements = Requirements(
                        requiredDays = app.requiredDays,
                        requiredTesters = app.requiredTesters,
                        currentTesters = 0, // V1: no real-time count
                        dailyMinutesEstimated = 15,
                    ),
                    matchReasons = emptyList(),
                    playOptInUrl = app.playOptInUrl,
                )
                AppResult.Success(detail)
            } catch (c: CancellationException) {
                throw c
            } catch (t: Throwable) {
                AppResult.Failure(AppError.fromThrowable(t))
            }
        }
}
