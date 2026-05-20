package com.apptest.core.domain

import com.apptest.core.common.AppError
import com.apptest.core.common.AppResult
import com.apptest.core.common.DispatcherProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

private class TestDispatchers(
    private val dispatcher: CoroutineDispatcher = UnconfinedTestDispatcher(),
) : DispatcherProvider {
    override val main = dispatcher
    override val io = dispatcher
    override val default = dispatcher
    override val unconfined = dispatcher
}

class UseCaseTest {

    @Test fun `success path returns Success`() = runTest {
        val uc = object : UseCase<Int, Int>(TestDispatchers()) {
            override suspend fun execute(params: Int) = AppResult.Success(params * 2)
        }
        assertThat(uc(21)).isEqualTo(AppResult.Success(42))
    }

    @Test fun `concrete UseCase returning Failure passes through`() = runTest {
        val err = AppError.NotFound("App")
        val uc = object : UseCase<Unit, Int>(TestDispatchers()) {
            override suspend fun execute(params: Unit) = AppResult.Failure(err)
        }
        assertThat(uc(Unit)).isEqualTo(AppResult.Failure(err))
    }

    @Test fun `unexpected throwable is converted to AppResult Failure`() = runTest {
        val uc = object : UseCase<Unit, Int>(TestDispatchers()) {
            override suspend fun execute(params: Unit): AppResult<Int> = throw IllegalStateException("boom")
        }
        val r = uc(Unit)
        assertThat(r).isInstanceOf(AppResult.Failure::class.java)
        assertThat((r as AppResult.Failure).error).isInstanceOf(AppError.Unknown::class.java)
    }

    @Test fun `CancellationException is not swallowed`() = runTest {
        val uc = object : UseCase<Unit, Int>(TestDispatchers()) {
            override suspend fun execute(params: Unit): AppResult<Int> = throw CancellationException("cancel")
        }
        try {
            uc(Unit)
            error("should have thrown")
        } catch (c: CancellationException) {
            assertThat(c.message).isEqualTo("cancel")
        }
    }

    @Test fun `NoParamUseCase invoke without params delegates to invoke(Unit)`() = runTest {
        val uc = object : NoParamUseCase<String>(TestDispatchers()) {
            override suspend fun execute(params: Unit) = AppResult.Success("hi")
        }
        assertThat(uc()).isEqualTo(AppResult.Success("hi"))
    }
}
