package com.apptest.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Brand palette fallback. Used when Dynamic Color is unavailable (pre-Android 12 or disabled).
 * Source of truth: `_specs/design_system.md` §2.2. Update both when changing.
 *
 * Tier colors (Newcomer..Platinum) are NOT here — those live in [AppExtendedColors] because
 * M3 ColorScheme has no slot for them.
 */

// ── Brand seeds (used to derive on*/Container variants) ─────────────────────
private val IndigoPrimary = Color(0xFF4F46E5)      // brand primary (indigo-600)
private val CyanSecondary = Color(0xFF06B6D4)      // tester-action (cyan-500)
private val AmberTertiary = Color(0xFFF59E0B)      // reputation accent (amber-500)
private val RedError = Color(0xFFDC2626)           // error/destructive (red-600)

// ── Light scheme ────────────────────────────────────────────────────────────
internal val AppLightColors = lightColorScheme(
    primary               = IndigoPrimary,
    onPrimary             = Color(0xFFFFFFFF),
    primaryContainer      = Color(0xFFE0E7FF),
    onPrimaryContainer    = Color(0xFF1E1B4B),
    inversePrimary        = Color(0xFFA5B4FC),

    secondary             = CyanSecondary,
    onSecondary           = Color(0xFFFFFFFF),
    secondaryContainer    = Color(0xFFCFFAFE),
    onSecondaryContainer  = Color(0xFF083344),

    tertiary              = AmberTertiary,
    onTertiary            = Color(0xFFFFFFFF),
    tertiaryContainer     = Color(0xFFFEF3C7),
    onTertiaryContainer   = Color(0xFF451A03),

    error                 = RedError,
    onError               = Color(0xFFFFFFFF),
    errorContainer        = Color(0xFFFEE2E2),
    onErrorContainer      = Color(0xFF450A0A),

    background            = Color(0xFFFAFAF9),
    onBackground          = Color(0xFF1C1917),
    surface               = Color(0xFFFAFAF9),
    onSurface             = Color(0xFF1C1917),
    surfaceVariant        = Color(0xFFE7E5E4),
    onSurfaceVariant      = Color(0xFF44403C),
    surfaceTint           = IndigoPrimary,

    // M3 Expressive surface containers (used per design_system §5)
    surfaceContainerLowest  = Color(0xFFFFFFFF),
    surfaceContainerLow     = Color(0xFFF5F5F4),
    surfaceContainer        = Color(0xFFEDEBE8),
    surfaceContainerHigh    = Color(0xFFE7E5E4),
    surfaceContainerHighest = Color(0xFFDDD9D5),

    outline               = Color(0xFF78716C),
    outlineVariant        = Color(0xFFD4D0CB),
    scrim                 = Color(0xFF000000),
    inverseSurface        = Color(0xFF292524),
    inverseOnSurface      = Color(0xFFF5F5F4),
)

// ── Dark scheme ─────────────────────────────────────────────────────────────
internal val AppDarkColors = darkColorScheme(
    primary               = Color(0xFFA5B4FC),
    onPrimary             = Color(0xFF1E1B4B),
    primaryContainer      = Color(0xFF312E81),
    onPrimaryContainer    = Color(0xFFE0E7FF),
    inversePrimary        = IndigoPrimary,

    secondary             = Color(0xFF67E8F9),
    onSecondary           = Color(0xFF083344),
    secondaryContainer    = Color(0xFF155E75),
    onSecondaryContainer  = Color(0xFFCFFAFE),

    tertiary              = Color(0xFFFCD34D),
    onTertiary            = Color(0xFF451A03),
    tertiaryContainer     = Color(0xFF92400E),
    onTertiaryContainer   = Color(0xFFFEF3C7),

    error                 = Color(0xFFFCA5A5),
    onError               = Color(0xFF450A0A),
    errorContainer        = Color(0xFF7F1D1D),
    onErrorContainer      = Color(0xFFFEE2E2),

    background            = Color(0xFF1C1917),
    onBackground          = Color(0xFFF5F5F4),
    surface               = Color(0xFF1C1917),
    onSurface             = Color(0xFFF5F5F4),
    surfaceVariant        = Color(0xFF44403C),
    onSurfaceVariant      = Color(0xFFD4D0CB),
    surfaceTint           = Color(0xFFA5B4FC),

    surfaceContainerLowest  = Color(0xFF0C0A09),
    surfaceContainerLow     = Color(0xFF1C1917),
    surfaceContainer        = Color(0xFF292524),
    surfaceContainerHigh    = Color(0xFF44403C),
    surfaceContainerHighest = Color(0xFF57534E),

    outline               = Color(0xFF78716C),
    outlineVariant        = Color(0xFF44403C),
    scrim                 = Color(0xFF000000),
    inverseSurface        = Color(0xFFF5F5F4),
    inverseOnSurface      = Color(0xFF292524),
)
