package com.apptest.feature.appdetail.data

import com.apptest.core.common.AppResult
import com.apptest.core.domain.Repository
import com.apptest.feature.appdetail.domain.model.AppDetailData

/**
 * App detail read repository. V1: backed by [FakeAppDetailRepository].
 * Real impl (post APT-V1-R-040) will compose Supabase `apps` + `profiles` + `match_run_pairs`
 * server-side and return a single aggregate.
 */
interface AppDetailRepository : Repository {
    suspend fun getById(appId: String): AppResult<AppDetailData>
}
