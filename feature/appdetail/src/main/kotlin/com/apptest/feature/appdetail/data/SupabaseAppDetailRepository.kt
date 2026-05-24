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
 * Uses [SupabaseAppsApiService.getByIdIncludingArchived] so the screen can render
 * an "App removed by developer" banner when testers click an inbox notification
 * for an app that's been archived since match assignment.
 */
@Singleton
class SupabaseAppDetailRepository @Inject constructor(
    private val appsApi: SupabaseAppsApiService,
    private val dispatchers: DispatcherProvider,
) : AppDetailRepository {

    override suspend fun getById(appId: String): AppResult<AppDetailData> =
        withContext(dispatchers.io) {
            try {
                val rows = appsApi.getByIdIncludingArchived("eq.$appId")
                val app = rows.firstOrNull()
                    ?: return@withContext AppResult.Failure(AppError.NotFound("app"))

                val detail = AppDetailData(
                    id = app.id,
                    packageName = app.packageName.orEmpty(),
                    name = app.name,
                    category = app.category,
                    description = app.description,
                    iconUrl = app.iconUrl,
                    screenshotCount = 0,
                    screenshotUrls = emptyList(),
                    owner = OwnerInfo(
                        displayName = app.profiles?.displayName ?: "Developer",
                        tier = ReputationTier.entries
                            .firstOrNull { it.name == app.profiles?.tier?.uppercase() }
                            ?: ReputationTier.Newcomer,
                    ),
                    requirements = Requirements(
                        requiredDays = app.requiredDays,
                        requiredTesters = app.requiredTesters,
                        currentTesters = 0,
                        dailyMinutesEstimated = 15,
                    ),
                    matchReasons = emptyList(),
                    playOptInUrl = app.playOptInUrl.orEmpty().ifBlank { app.playUrl },
                    status = app.status,
                )
                AppResult.Success(detail)
            } catch (c: CancellationException) {
                throw c
            } catch (t: Throwable) {
                AppResult.Failure(AppError.fromThrowable(t))
            }
        }
}
