package com.apptest.core.network.backend

import kotlinx.serialization.Serializable
import retrofit2.http.GET

/**
 * Public read-only stats from the Ktor backend.
 * Used by Home screen to show a "pool status" banner (cold-start UX).
 */
interface BackendStatsApiService {
    @GET("v1/pool-stats")
    suspend fun poolStats(): PoolStatsDto
}

@Serializable
data class PoolStatsDto(
    val activeApps: Int,
    val testers: Int,
    val immediateMatchMode: Boolean,
    /** "empty" | "small" | "healthy" — see backend PoolStatsRoutes.kt for semantics. */
    val hint: String,
)
