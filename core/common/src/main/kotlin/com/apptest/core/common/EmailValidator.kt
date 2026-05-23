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

    /**
     * Returns true if [email] is a valid RFC-5321 address.
     *
     * MED-015 (audit 2026-05-23): trims the input before validation — callers binding a
     * TextField value may not have called trim() before passing here. We still REJECT
     * addresses whose trimmed form differs from the original so callers know to strip
     * whitespace before sending to the server; we do NOT silently accept " foo@bar.com ".
     */
    fun isValid(email: String): Boolean {
        val trimmed = email.trim()
        if (trimmed.length !in 3..254) return false
        if (trimmed != email) return false  // reject leading/trailing whitespace explicitly
        if (".." in trimmed) return false
        if (trimmed.startsWith('.') || trimmed.endsWith('.')) return false
        return PATTERN.matches(trimmed)
    }
}
