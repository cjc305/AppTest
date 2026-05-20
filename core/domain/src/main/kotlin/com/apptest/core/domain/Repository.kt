package com.apptest.core.domain

/**
 * Marker interface for repositories living in `:feature:*` modules.
 * Conventions:
 * - Repository methods return [com.apptest.core.common.AppResult], never throw.
 * - Repository methods are suspend (or return Flow). No callback APIs.
 * - One Repository per aggregate root (User, App, TestRequest, Review), not per screen.
 * - Repository lives in `:core:data` for shared aggregates, or `:feature:X:data` for feature-local ones.
 */
interface Repository
