package com.apptest.core.network.interceptor

import com.apptest.core.domain.auth.TokenProvider
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test

class AuthInterceptorTest {

    private val tokenProvider = mockk<TokenProvider>()
    private val interceptor = AuthInterceptor(tokenProvider)

    @Test fun `attaches Bearer header when token is non-null`() {
        coEvery { tokenProvider.token() } returns "abc.def.ghi"
        val original = Request.Builder().url("https://example.com/v1/x").build()
        val captured = captureProceededRequest(original)

        interceptor.intercept(chainFor(original, captured))

        assertThat(captured.captured.header("Authorization")).isEqualTo("Bearer abc.def.ghi")
    }

    @Test fun `omits Authorization header when token is null`() {
        coEvery { tokenProvider.token() } returns null
        val original = Request.Builder().url("https://example.com/v1/x").build()
        val captured = captureProceededRequest(original)

        interceptor.intercept(chainFor(original, captured))

        assertThat(captured.captured.header("Authorization")).isNull()
    }

    @Test fun `preserves existing headers when attaching auth`() {
        coEvery { tokenProvider.token() } returns "t"
        val original = Request.Builder()
            .url("https://example.com/v1/x")
            .header("X-Trace", "abc")
            .build()
        val captured = captureProceededRequest(original)

        interceptor.intercept(chainFor(original, captured))

        assertThat(captured.captured.header("X-Trace")).isEqualTo("abc")
        assertThat(captured.captured.header("Authorization")).isEqualTo("Bearer t")
    }

    // ── helpers ───────────────────────────────────────────────────────────────────────────

    private fun captureProceededRequest(@Suppress("UNUSED_PARAMETER") original: Request) = slot<Request>()

    private fun chainFor(original: Request, captured: io.mockk.CapturingSlot<Request>): Interceptor.Chain {
        val chain = mockk<Interceptor.Chain>()
        every { chain.request() } returns original
        every { chain.proceed(capture(captured)) } answers {
            Response.Builder()
                .request(captured.captured)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body("".toResponseBody(null))
                .build()
        }
        return chain
    }
}
