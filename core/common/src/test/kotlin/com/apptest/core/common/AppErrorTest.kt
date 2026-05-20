package com.apptest.core.common

import com.google.common.truth.Truth.assertThat
import java.io.IOException
import java.net.SocketTimeoutException
import org.junit.Test

class AppErrorTest {

    @Test fun `fromThrowable maps IOException to Network`() {
        val err = AppError.fromThrowable(IOException("offline"))
        assertThat(err).isInstanceOf(AppError.Network::class.java)
        assertThat(err.message).isEqualTo("offline")
        assertThat(err.cause).isInstanceOf(IOException::class.java)
    }

    @Test fun `fromThrowable maps SocketTimeoutException (an IOException subclass) to Network`() {
        val err = AppError.fromThrowable(SocketTimeoutException("slow"))
        assertThat(err).isInstanceOf(AppError.Network::class.java)
    }

    @Test fun `fromThrowable maps unknown throwable to Unknown`() {
        val err = AppError.fromThrowable(IllegalArgumentException("bad"))
        assertThat(err).isInstanceOf(AppError.Unknown::class.java)
        assertThat(err.cause).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test fun `Validation carries field + message`() {
        val err = AppError.Validation(field = "email", message = "invalid")
        assertThat(err.field).isEqualTo("email")
        assertThat(err.message).isEqualTo("invalid")
    }

    @Test fun `NotFound message is derived from resource`() {
        assertThat(AppError.NotFound("App").message).isEqualTo("App not found")
    }

    @Test fun `Auth reason enum has expected entries`() {
        assertThat(AppError.AuthReason.entries.map { it.name }).containsExactly(
            "Unauthenticated", "TokenExpired", "InvalidCredential", "SignInCancelled",
        )
    }

    @Test fun `RateLimited carries optional retry-after`() {
        val withRetry = AppError.RateLimited(retryAfterSeconds = 60)
        val noRetry = AppError.RateLimited(retryAfterSeconds = null)
        assertThat(withRetry.retryAfterSeconds).isEqualTo(60L)
        assertThat(noRetry.retryAfterSeconds).isNull()
    }
}
