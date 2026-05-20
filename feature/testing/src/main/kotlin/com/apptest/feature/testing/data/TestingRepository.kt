package com.apptest.feature.testing.data

import com.apptest.core.common.AppResult
import com.apptest.core.domain.Repository
import com.apptest.feature.testing.domain.model.TestingSnapshot
import kotlinx.coroutines.flow.Flow

interface TestingRepository : Repository {
    fun observe(): Flow<TestingSnapshot>
    suspend fun submitHeartbeat(testId: String): AppResult<Unit>
    suspend fun abandon(testId: String): AppResult<Unit>
}
