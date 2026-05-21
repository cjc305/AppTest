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
// M3 purple (#6750A4) — aligns with AppTest icon gradient + Claude Design prototype.
// Tokens derived from Material Theme Builder (seed #6750A4, baseline scheme).
private val PurplePrimary = Color(0xFF6750A4)      // brand primary (M3 purple seed)
private val CyanSecondary = Color(0xFF06B6D4)      // tester-action (cyan-500)
private val AmberTertiary = Color(0xFFF59E0B)      // reputation accent (amber-500)
private val RedError = Color(0xFFDC2626)           // error/destructive (red-600)

// ── Light scheme ────────────────────────────────────────────────────────────
internal val AppLightColors = lightColorScheme(
    primary               = PurplePrimary,
    onPrimary             = Color(0xFFFFFFFF),
    primaryContainer      = Color(0xFFEADDFF),
    onPrimaryContainer    = Color(0xFF21005D),
    inversePrimary        = Color(0xFFD0BCFF),

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
    surfaceTint           = PurplePrimary,

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
// Tokens derived from Material Theme Builder (seed #6750A4, dark baseline).
// Backgrounds use cool purple-dark tones to match Claude Design prototype (#0F0F1A range).
internal val AppDarkColors = darkColorScheme(
    primary               = Color(0xFFD0BCFF),
    onPrimary             = Color(0xFF381E72),
    primaryContainer      = Color(0xFF4F378B),
    onPrimaryContainer    = Color(0xFFEADDFF),
    inversePrimary        = PurplePrimary,

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

    background            = Color(0xFF141218),
    onBackground          = Color(0xFFE6E0E9),
    surface               = Color(0xFF141218),
    onSurface             = Color(0xFFE6E0E9),
    surfaceVariant        = Color(0xFF49454F),
    onSurfaceVariant      = Color(0xFFCAC4D0),
    surfaceTint           = Color(0xFFD0BCFF),

    surfaceContainerLowest  = Color(0xFF0F0D13),
    surfaceContainerLow     = Color(0xFF1D1B20),
    surfaceContainer        = Color(0xFF211F26),
    surfaceContainerHigh    = Color(0xFF2B2930),
    surfaceContainerHighest = Color(0xFF36343B),

    outline               = Color(0xFF938F99),
    outlineVariant        = Color(0xFF49454F),
    scrim                 = Color(0xFF000000),
    inverseSurface        = Color(0xFFE6E0E9),
    inverseOnSurface      = Color(0xFF322F35),
)
