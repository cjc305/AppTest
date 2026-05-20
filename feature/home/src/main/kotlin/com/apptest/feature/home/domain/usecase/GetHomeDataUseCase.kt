package com.apptest.feature.home.domain.usecase

import com.apptest.core.common.AppResult
import com.apptest.core.common.DispatcherProvider
import com.apptest.core.domain.NoParamUseCase
import com.apptest.feature.home.data.HomeRepository
import com.apptest.feature.home.domain.model.HomeData
import javax.inject.Inject

/**
 * Loads the full Home snapshot. Thin delegation today — exists so the screen layer talks to
 * a UseCase (per `compose_components.md §6` anti-pattern #1: UI not allowed to call repo
 * directly) and so V2 can layer in caching / freshness logic here without touching UI.
 */
class GetHomeDataUseCase @Inject constructor(
    private val repo: HomeRepository,
    dispatchers: DispatcherProvider,
) : NoParamUseCase<HomeData>(dispatchers) {

    override suspend fun execute(params: Unit): AppResult<HomeData> = repo.getHomeData()
}
