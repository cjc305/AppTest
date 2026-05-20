package com.apptest.feature.appdetail.domain.usecase

import com.apptest.core.common.AppResult
import com.apptest.core.common.DispatcherProvider
import com.apptest.core.domain.UseCase
import com.apptest.feature.appdetail.data.AppDetailRepository
import com.apptest.feature.appdetail.domain.model.AppDetailData
import javax.inject.Inject

/**
 * Thin delegation today. Exists so the UI doesn't talk to repo directly (per
 * `compose_components.md §6` anti-pattern #1) and so V2 can layer caching / freshness here.
 */
class GetAppDetailUseCase @Inject constructor(
    private val repo: AppDetailRepository,
    dispatchers: DispatcherProvider,
) : UseCase<String, AppDetailData>(dispatchers) {

    override suspend fun execute(params: String): AppResult<AppDetailData> = repo.getById(params)
}
