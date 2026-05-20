package com.apptest.core.common

import kotlinx.coroutines.CancellationException

/**
 * Discriminated result type. All UseCase and Repository methods return this — never throw.
 * Use [getOrNull]/[errorOrNull] for branchless access, or pattern-match on the sealed type.
 */
sealed interface AppResult<out T> {
    data class Success<T>(val value: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}

inline fun <T> AppResult<T>.getOrNull(): T? = (this as? AppResult.Success)?.value
inline fun <T> AppResult<T>.errorOrNull(): AppError? = (this as? AppResult.Failure)?.error

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(value))
    is AppResult.Failure -> this
}

inline fun <T, R> AppResult<T>.flatMap(transform: (T) -> AppResult<R>): AppResult<R> = when (this) {
    is AppResult.Success -> transform(value)
    is AppResult.Failure -> this
}

inline fun <T> AppResult<T>.onSuccess(action: (T) -> Unit): AppResult<T> = apply {
    if (this is AppResult.Success) action(value)
}

inline fun <T> AppResult<T>.onFailure(action: (AppError) -> Unit): AppResult<T> = apply {
    if (this is AppResult.Failure) action(error)
}

/**
 * Wraps a thunk that may throw. Reserved for adapter boundaries (Retrofit calls, DB calls).
 * Re-throws [CancellationException] per coroutine cancellation contract — never swallow it.
 */
inline fun <T> runCatchingApp(block: () -> T): AppResult<T> = try {
    AppResult.Success(block())
} catch (c: CancellationException) {
    throw c
} catch (t: Throwable) {
    AppResult.Failure(AppError.fromThrowable(t))
}
