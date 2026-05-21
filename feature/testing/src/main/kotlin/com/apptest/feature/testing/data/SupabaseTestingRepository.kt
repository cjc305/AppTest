package com.apptest.feature.testing.data

import com.apptest.core.common.AppError
import com.apptest.core.common.AppResult
import com.apptest.core.common.DispatcherProvider
import com.apptest.core.network.matches.ActiveMatchWithAppDto
import com.apptest.core.network.matches.CompletedMatchWithAppDto
import com.apptest.core.network.matches.SupabaseMatchesApiService
import com.apptest.feature.testing.domain.model.ActiveTestEntry
import com.apptest.feature.testing.domain.model.CompletedTestEntry
import com.apptest.feature.testing.domain.model.TestStatus
import com.apptest.feature.testing.domain.model.TestingSnapshot
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

/**
 * Real Supabase-backed [TestingRepository]. Replaces [FakeTestingRepository].
 *
 * V1: days_active, last_heartbeat_at, required_days not in DB — defaulted.
 * Heartbeat touches status=active as a keep-alive signal.
 */
@Singleton
class SupabaseTestingRepository @Inject constructor(
    private val matchesApi: SupabaseMatchesApiService,
    private val dispatchers: DispatcherProvider,
) : TestingRepository {

    private val _state = MutableStateFlow(TestingSnapshot(emptyList(), emptyList()))

    override fun observe(): Flow<TestingSnapshot> = flow {
        runCatching {
            withContext(dispatchers.io) {
                val activeDeferred = async { matchesApi.getActiveTests() }
                val completedDeferred = async { matchesApi.getCompletedTests() }
                TestingSnapshot(
                    active = activeDeferred.await().map { it.toEntry() },
                    completed = completedDeferred.await().map { it.toCompleted() },
                )
            }
        }.onSuccess { _state.value = it }
        emitAll(_state.asStateFlow())
    }.flowOn(dispatchers.io)

    override suspend fun submitHeartbeat(testId: String): AppResult<Unit> =
        withContext(dispatchers.io) {
            try {
                matchesApi.submitHeartbeat("eq.$testId").close()
                _state.update { snap ->
                    snap.copy(active = snap.active.map {
                        if (it.testId == testId) it.copy(pingStatusOk = true, status = TestStatus.Active)
                        else it
                    })
                }
                AppResult.Success(Unit)
            } catch (c: CancellationException) { throw c }
            catch (t: Throwable) { AppResult.Failure(AppError.fromThrowable(t)) }
        }

    override suspend fun abandon(testId: String): AppResult<Unit> =
        withContext(dispatchers.io) {
            try {
                matchesApi.abandon("eq.$testId").close()
                _state.update { snap ->
                    snap.copy(active = snap.active.filterNot { it.testId == testId })
                }
                AppResult.Success(Unit)
            } catch (c: CancellationException) { throw c }
            catch (t: Throwable) { AppResult.Failure(AppError.fromThrowable(t)) }
        }
}

// ─── Mapping helpers ──────────────────────────────────────────────────────────

private fun ActiveMatchWithAppDto.toEntry() = ActiveTestEntry(
    testId = id,
    appId = appId,
    appName = apps?.name ?: "",
    day = 0, // V1: days_active not in DB
    totalDays = 14, // V1: required_days not in DB
    pingStatusOk = true, // V1: last_heartbeat_at not in DB — default ok
    status = TestStatus.Active,
)

private fun CompletedMatchWithAppDto.toCompleted() = CompletedTestEntry(
    testId = id,
    appId = appId,
    appName = apps?.name ?: "",
    daysCompleted = 14, // V1: days_active not in DB
    reputationGained = 0,
    proofId = null,
)
