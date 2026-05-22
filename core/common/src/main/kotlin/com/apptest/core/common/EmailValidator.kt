package com.apptest.core.common

/**
 * Pure-Kotlin RFC-5321 / RFC-5322 pragmatic email validator. Shared between Fake + real
 * AuthRepository so V1 and tests behave identically. Conservative on purpose — rejects
 * leading/trailing dots, double dots, IDN, and quoted local parts (the tiny fraction of
 * real users with those addresses can use Google sign-in instead).
 */
object EmailValidator {

    private val PATTERN = Regex(
        "^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$"
    )

    fun isValid(email: String): Boolean {
        if (email.length !in 3..254) return false
        val trimmed = email.trim()
        if (trimmed != email) return false
        if (".." in trimmed) return false
        if (trimmed.startsWith('.') || trimmed.endsWith('.')) return false
        return PATTERN.matches(trimmed)
    }
}
