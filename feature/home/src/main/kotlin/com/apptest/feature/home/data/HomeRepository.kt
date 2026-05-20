package com.apptest.feature.home.data

import com.apptest.core.common.AppResult
import com.apptest.core.domain.Repository
import com.apptest.feature.home.domain.model.HomeData

/**
 * Home aggregate repository. Returns the full HomeData snapshot (greeting + today match +
 * active tests + your apps) in one call. ViewModel maps to UiState.
 *
 * V1: backed by [FakeHomeRepository]. Real impl wires `:core:network` + `:core:database` once
 * APT-V1-R-040 / R-043 land. Swap via `HomeDataModule` Hilt binding.
 */
interface HomeRepository : Repository {
    suspend fun getHomeData(): AppResult<HomeData>
}
