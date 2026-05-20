package com.apptest.core.common

import com.google.common.truth.Truth.assertThat
import java.io.IOException
import kotlinx.coroutines.CancellationException
import org.junit.Test

class ResultTest {

    @Test fun `getOrNull returns value on Success`() {
        assertThat(AppResult.Success(42).getOrNull()).isEqualTo(42)
    }

    @Test fun `getOrNull returns null on Failure`() {
        val r: AppResult<Int> = AppResult.Failure(AppError.Unknown())
        assertThat(r.getOrNull()).isNull()
    }

    @Test fun `errorOrNull mirrors getOrNull`() {
        val err = AppError.Validation(field = "x", message = "bad")
        val r: AppResult<Int> = AppResult.Failure(err)
        assertThat(r.errorOrNull()).isEqualTo(err)
        assertThat(AppResult.Success(1).errorOrNull()).isNull()
    }

    @Test fun `map transforms only on Success`() {
        assertThat(AppResult.Success(3).map { it * 2 }.getOrNull()).isEqualTo(6)
        val fail: AppResult<Int> = AppResult.Failure(AppError.Unknown())
        assertThat(fail.map { it * 2 }).isInstanceOf(AppResult.Failure::class.java)
    }

    @Test fun `flatMap short-circuits on Failure`() {
        val r: AppResult<Int> = AppResult.Failure(AppError.NotFound("x"))
        var called = false
        val out = r.flatMap { called = true; AppResult.Success(it) }
        assertThat(called).isFalse()
        assertThat(out).isSameInstanceAs(r)
    }

    @Test fun `onSuccess and onFailure side effects`() {
        var hits = 0
        AppResult.Success(1).onSuccess { hits++ }.onFailure { hits += 10 }
        AppResult.Failure(AppError.Unknown()).onSuccess { hits += 100 }.onFailure { hits++ }
        assertThat(hits).isEqualTo(2)
    }

    @Test fun `runCatchingApp wraps IOException as Network`() {
        val r = runCatchingApp<Int> { throw IOException("offline") }
        val err = (r as AppResult.Failure).error
        assertThat(err).isInstanceOf(AppError.Network::class.java)
    }

    @Test fun `runCatchingApp re-throws CancellationException`() {
        try {
            runCatchingApp<Int> { throw CancellationException("nope") }
            error("should have thrown")
        } catch (c: CancellationException) {
            assertThat(c.message).isEqualTo("nope")
        }
    }

    @Test fun `runCatchingApp wraps generic Throwable as Unknown`() {
        val r = runCatchingApp<Int> { throw IllegalStateException("boom") }
        val err = (r as AppResult.Failure).error
        assertThat(err).isInstanceOf(AppError.Unknown::class.java)
        assertThat(err.cause).isInstanceOf(IllegalStateException::class.java)
    }
}
