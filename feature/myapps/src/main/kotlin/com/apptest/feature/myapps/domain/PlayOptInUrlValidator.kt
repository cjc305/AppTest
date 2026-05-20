package com.apptest.feature.myapps.domain

import com.apptest.feature.myapps.domain.model.PlayUrlValidation

/**
 * Pure-function validator for Play Console closed-test opt-in URLs.
 *
 * V1 rule (relaxed): must be `https://play.google.com/...`. Stricter shape (path contains
 * `/apps/testing/` etc.) deferred — real validation happens server-side too.
 *
 * Use over a full UseCase since this is sync + no IO + needed on every keystroke.
 */
object PlayOptInUrlValidator {

    fun validate(raw: String): PlayUrlValidation {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return PlayUrlValidation.Empty

        val lower = trimmed.lowercase()
        if (!lower.startsWith("https://")) {
            return PlayUrlValidation.Invalid("Must start with https://")
        }
        val host = trimmed.removePrefix("https://").substringBefore('/')
        if (host != "play.google.com") {
            return PlayUrlValidation.Invalid("Host must be play.google.com (was: $host)")
        }
        return PlayUrlValidation.Valid
    }
}
