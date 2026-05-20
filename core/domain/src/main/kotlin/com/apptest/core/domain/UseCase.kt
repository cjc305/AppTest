package com.apptest.core.domain

import com.apptest.core.common.AppError
import com.apptest.core.common.AppResult
import com.apptest.core.common.DispatcherProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext

/**
 * Base for one-shot use cases. Each concrete use case represents exactly one user-facing action.
 * Rule: 1 file = 1 UseCase. ≤ 50 lines.
 *
 * Example:
 * ```
 * class JoinTestUseCase @Inject constructor(
 *     private val repo: TestRequestRepository,
 *     dispatchers: DispatcherProvider,
 * ) : UseCase<JoinTestUseCase.Params, TestRequest>(dispatchers) {
 *     override suspend fun execute(params: Params) = repo.join(params.appId, params.testerId)
 *     data class Params(val appId: String, val testerId: String)
 * }
 * ```
 */
abstract class UseCase<in P, R>(
    private val dispatchers: DispatcherProvider,
) {
    suspend operator fun invoke(params: P): AppResult<R> = withContext(dispatchers.io) {
        try {
            execute(params)
        } catch (c: CancellationException) {
            throw c
        } catch (t: Throwable) {
            AppResult.Failure(AppError.fromThrowable(t))
        }
    }

    protected abstract suspend fun execute(params: P): AppResult<R>
}

/** For use cases that don't need a parameter. */
abstract class NoParamUseCase<R>(dispatchers: DispatcherProvider) : UseCase<Unit, R>(dispatchers) {
    suspend operator fun invoke(): AppResult<R> = invoke(Unit)
}

/** Sugar for void-returning use cases. */
typealias VoidUseCaseResult = AppResult<Unit>
