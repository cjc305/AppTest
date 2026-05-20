package com.apptest.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.apptest.core.common.AppStringsCatalog

/**
 * Single entry point for AppTest's Material 3 (Expressive) theming.
 *
 * Owns:
 * - Color: Dynamic Color (Android 12+) with brand-palette fallback (see [AppLightColors] / [AppDarkColors]).
 * - Extended colors: tier + success + warning (M3 has no such slots) via [LocalAppExtendedColors].
 * - Typography: M3 Expressive scale (see [AppTypography]).
 * - Shapes: large-radius first (see [AppShapes]).
 *
 * Edge-to-edge is set in `:app`'s MainActivity (this theme only owns color/type/shape).
 * Motion / Glass / Floating / Spatial tokens shipped in future companion files.
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val canDynamic = dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colors = when {
        canDynamic && darkTheme -> dynamicDarkColorScheme(context)
        canDynamic && !darkTheme -> dynamicLightColorScheme(context)
        darkTheme -> AppDarkColors
        else -> AppLightColors
    }
    val extended = if (darkTheme) AppExtendedDark else AppExtendedLight

    // Locale-aware string catalog. Re-resolves whenever system locale changes (Settings →
    // language switch triggers Configuration update → re-composition).
    val configuration = LocalConfiguration.current
    val strings = remember(configuration) {
        val tag = configuration.locales.takeIf { !it.isEmpty }?.get(0)?.toLanguageTag() ?: "en"
        AppStringsCatalog.pick(tag)
    }

    CompositionLocalProvider(
        LocalAppExtendedColors provides extended,
        LocalAppStrings provides strings,
    ) {
        MaterialTheme(
            colorScheme = colors,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}
