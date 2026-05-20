package com.apptest.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import com.apptest.core.common.AppStrings
import com.apptest.core.common.AppStringsCatalog

/**
 * CompositionLocal providing locale-aware [AppStrings]. Auto-resolves based on system locale
 * via [AppTheme] CompositionLocalProvider.
 *
 * Access pattern:
 * ```kotlin
 * AppText(LocalAppStrings.current.signin_cta_google)
 * ```
 *
 * Default value is `EN`; real value resolved per system locale at theme provision time.
 */
val LocalAppStrings = compositionLocalOf<AppStrings> { AppStringsCatalog.EN }

/** Shorthand: `AppL10n.current.foo` inside a @Composable. */
object AppL10n {
    val current: AppStrings
        @Composable
        @ReadOnlyComposable
        get() = LocalAppStrings.current
}
