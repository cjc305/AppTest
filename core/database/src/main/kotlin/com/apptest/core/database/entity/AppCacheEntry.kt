package com.apptest.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Generic key-value cache row. Use case: cheap JSON-blob caching for feature reads that
 * survive process death (e.g., last seen Home feed) without designing a typed table.
 *
 * Typed tables (test_requests, match_cache, proofs) land with R-044 / R-046 — they get
 * their own `@Entity` files in `:core:database/entity/` and DAOs in `dao/`.
 *
 * Schema is intentionally minimal:
 * - [key] — caller-defined namespace, e.g., `"home:feed:v1"`. Caller owns versioning.
 * - [payloadJson] — `kotlinx.serialization` JSON string of caller's DTO.
 * - [updatedAtEpochMs] — for caller-side TTL checks (Room has no built-in expiry).
 */
@Entity(tableName = "app_cache")
data class AppCacheEntry(
    @PrimaryKey val key: String,
    val payloadJson: String,
    val updatedAtEpochMs: Long,
)
