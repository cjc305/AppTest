package com.apptest.feature.myapps.domain.usecase

import com.apptest.feature.myapps.data.MyAppsRepository
import com.apptest.feature.myapps.domain.model.OwnedAppRow
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Live owned-apps list. Thin delegation; exists so UI is decoupled from repo (per
 * `compose_components.md §6` anti-pattern #1) and so V2 can layer cache freshness here.
 */
class GetMyAppsUseCase @Inject constructor(
    private val repo: MyAppsRepository,
) {
    operator fun invoke(): Flow<List<OwnedAppRow>> = repo.observe()
}
