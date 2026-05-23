package com.apptest.core.network.interceptor

import com.apptest.core.domain.auth.TokenProvider
import com.google.common.truth.Truth.assertThat
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
    // HIGH-2 fix: AuthInterceptor now requires host allowlist. Tests host "example.com" is allowed
    // unless a specific test scopes a narrower set.
    private val interceptor = AuthInterceptor(tokenProvider, allowedHosts = setOf("example.com"))

    @Test fun `attaches Bearer header when token is non-null`() {
        every { tokenProvider.tokenBlocking() } returns "abc.def.ghi"
        val original = Request.Builder().url("https://example.com/v1/x").build()
        val captured = captureProceededRequest(original)

        interceptor.intercept(chainFor(original, captured))

        assertThat(captured.captured.header("Authorization")).isEqualTo("Bearer abc.def.ghi")
    }

    @Test fun `omits Authorization header when token is null`() {
        every { tokenProvider.tokenBlocking() } returns null
        val original = Request.Builder().url("https://example.com/v1/x").build()
        val captured = captureProceededRequest(original)

        interceptor.intercept(chainFor(original, captured))

        assertThat(captured.captured.header("Authorization")).isNull()
    }

    @Test fun `preserves existing headers when attaching auth`() {
        every { tokenProvider.tokenBlocking() } returns "t"
        val original = Request.Builder()
            .url("https://example.com/v1/x")
            .header("X-Trace", "abc")
            .build()
        val captured = captureProceededRequest(original)

        interceptor.intercept(chainFor(original, captured))

        assertThat(captured.captured.header("X-Trace")).isEqualTo("abc")
        assertThat(captured.captured.header("Authorization")).isEqualTo("Bearer t")
    }

    @Test fun `does NOT attach Bearer for hosts outside allowlist (HIGH-2)`() {
        every { tokenProvider.tokenBlocking() } returns "supabase.jwt"
        val outside = Request.Builder().url("https://apptest-backend.run.app/v1/match").build()
        val captured = captureProceededRequest(outside)

        interceptor.intercept(chainFor(outside, captured))

        // The Ktor backend host is NOT in allowedHosts → request must not carry Authorization.
        assertThat(captured.captured.header("Authorization")).isNull()
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
