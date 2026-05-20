package com.apptest.feature.testing.data

import com.apptest.core.common.AppResult
import com.apptest.feature.testing.domain.model.ActiveTestEntry
import com.apptest.feature.testing.domain.model.CompletedTestEntry
import com.apptest.feature.testing.domain.model.TestStatus
import com.apptest.feature.testing.domain.model.TestingSnapshot
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Singleton
class FakeTestingRepository @Inject constructor() : TestingRepository {

    private val _snapshot = MutableStateFlow(seed())
    override fun observe(): Flow<TestingSnapshot> = _snapshot.asStateFlow()

    override suspend fun submitHeartbeat(testId: String): AppResult<Unit> {
        _snapshot.update { snap ->
            snap.copy(active = snap.active.map {
                if (it.testId == testId) it.copy(pingStatusOk = true, status = TestStatus.Active) else it
            })
        }
        return AppResult.Success(Unit)
    }

    override suspend fun abandon(testId: String): AppResult<Unit> {
        _snapshot.update { snap ->
            snap.copy(active = snap.active.filterNot { it.testId == testId })
        }
        return AppResult.Success(Unit)
    }

    private companion object {
        fun seed(): TestingSnapshot = TestingSnapshot(
            active = listOf(
                ActiveTestEntry("t1", "a1", "QuickHabit", day = 5, totalDays = 14, pingStatusOk = true, status = TestStatus.Active),
                ActiveTestEntry("t2", "a2", "PixelBudget", day = 12, totalDays = 14, pingStatusOk = false, status = TestStatus.AtRisk),
                ActiveTestEntry("t3", "a3", "TripPal", day = 1, totalDays = 14, pingStatusOk = true, status = TestStatus.Active),
            ),
            completed = listOf(
                CompletedTestEntry("c1", "x1", "AppX", daysCompleted = 14, reputationGained = 12, proofId = "proof-c1"),
                CompletedTestEntry("c2", "x2", "AppY", daysCompleted = 14, reputationGained = 9, proofId = "proof-c2"),
            ),
        )
    }
}
