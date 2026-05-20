package com.apptest.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Brand-specific colors not represented by M3 [androidx.compose.material3.ColorScheme] slots.
 * Currently houses reputation tier colors per `_specs/design_system.md` §2.3.
 *
 * Access via `AppExtended.colors` inside any @Composable.
 * Provided by [AppTheme] via [LocalAppExtendedColors] CompositionLocal.
 */
@Immutable
data class AppExtendedColors(
    // Reputation tier colors (see _specs/reputation_system.md §1 for tier semantics)
    val tierNewcomer: Color,
    val tierBronze: Color,
    val tierSilver: Color,
    val tierGold: Color,
    val tierPlatinumStart: Color,   // gradient: start
    val tierPlatinumEnd: Color,     // gradient: end

    // Semantic "success" — M3 has no success slot; needed for completion states
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,

    // Warning — used for at-risk / heartbeat-overdue states
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
)

internal val AppExtendedLight = AppExtendedColors(
    tierNewcomer       = Color(0xFFA8A29E),                 // muted stone
    tierBronze         = Color(0xFFB97A56),                 // warm bronze
    tierSilver         = Color(0xFFBFC3C9),                 // cool silver
    tierGold           = Color(0xFFFFC93C),                 // expressive gold
    tierPlatinumStart  = Color(0xFFE5E4E2),                 // gradient (135deg)
    tierPlatinumEnd    = Color(0xFFA8C5E6),                 // gradient (135deg)

    success            = Color(0xFF16A34A),                 // green-600
    onSuccess          = Color(0xFFFFFFFF),
    successContainer   = Color(0xFFDCFCE7),
    onSuccessContainer = Color(0xFF052E16),

    warning            = Color(0xFFEA580C),                 // orange-600
    onWarning          = Color(0xFFFFFFFF),
    warningContainer   = Color(0xFFFFEDD5),
    onWarningContainer = Color(0xFF431407),
)

internal val AppExtendedDark = AppExtendedColors(
    tierNewcomer       = Color(0xFF78716C),
    tierBronze         = Color(0xFFD4956B),
    tierSilver         = Color(0xFFD6D9DD),
    tierGold           = Color(0xFFFFD75C),
    tierPlatinumStart  = Color(0xFFD4D2CF),
    tierPlatinumEnd    = Color(0xFF9DB8D8),

    success            = Color(0xFF4ADE80),
    onSuccess          = Color(0xFF052E16),
    successContainer   = Color(0xFF14532D),
    onSuccessContainer = Color(0xFFDCFCE7),

    warning            = Color(0xFFFB923C),
    onWarning          = Color(0xFF431407),
    warningContainer   = Color(0xFF7C2D12),
    onWarningContainer = Color(0xFFFFEDD5),
)

/** Throws if accessed outside [AppTheme] — catches accidental usage in untheme'd previews. */
val LocalAppExtendedColors = staticCompositionLocalOf<AppExtendedColors> {
    error("AppExtendedColors not provided. Wrap content in AppTheme { ... }.")
}

/** Accessor object: `AppExtended.colors.tierGold` inside a @Composable. */
object AppExtended {
    val colors: AppExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppExtendedColors.current
}
