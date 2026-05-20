package com.apptest.core.common

import java.io.IOException

/**
 * Canonical error taxonomy. All errors crossing module boundaries map to one of these.
 * Add subtypes here, never as ad-hoc exceptions in feature code.
 */
sealed class AppError(open val message: String?, open val cause: Throwable? = null) {

    data class Network(override val message: String? = null, override val cause: Throwable? = null) : AppError(message, cause)
    data class Http(val code: Int, override val message: String?, override val cause: Throwable? = null) : AppError(message, cause)
    data class Auth(val reason: AuthReason, override val message: String? = null) : AppError(message)
    data class Validation(val field: String, override val message: String) : AppError(message)
    data class NotFound(val resource: String) : AppError("$resource not found")
    data class Forbidden(override val message: String? = null) : AppError(message)
    data class Conflict(override val message: String? = null) : AppError(message)
    data class RateLimited(val retryAfterSeconds: Long?) : AppError("rate limited")
    data class Unknown(override val message: String? = null, override val cause: Throwable? = null) : AppError(message, cause)

    enum class AuthReason { Unauthenticated, TokenExpired, InvalidCredential, SignInCancelled }

    companion object {
        /** CancellationException is intentionally NOT handled here — callers must re-throw it. */
        fun fromThrowable(t: Throwable): AppError = when (t) {
            is IOException -> Network(t.message, t)
            else -> Unknown(t.message, t)
        }
    }
}
