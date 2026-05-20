package com.apptest.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.apptest.app.nav.AppNavHost
import com.apptest.core.common.AuthState
import com.apptest.core.designsystem.theme.AppTheme
import com.apptest.core.domain.auth.AuthRepository
import com.apptest.core.navigation.startDestinationFor
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

/**
 * Single Activity host per `_specs/navigation.md` §4.
 *
 * Responsibilities (minimal):
 * - Enable edge-to-edge (per `_specs/design_system.md` hard rule APT-Q-004).
 * - Wrap content in [AppTheme] (so Dynamic Color + tokens propagate).
 * - Observe [AuthState] from [AuthRepository] and drive NavHost startDestination.
 * - Provide side-effecting callbacks the NavHost can't own itself (share intent, sign-out IO).
 * - Pass [WindowSizeClass] down for responsive layouts.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var authRepo: AuthRepository

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash BEFORE super.onCreate so the system swaps from Theme.Starting cleanly.
        // Keep-on-screen while the session is still being read from DataStore — once authState
        // emits a real value (or after a max wait), the splash dismisses.
        val splash = installSplashScreen()
        var sessionRestored = false
        splash.setKeepOnScreenCondition { !sessionRestored }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Mark session restored once the first non-initial AuthState emission arrives.
        // Fallback: assume restored after ~300ms in case SessionStore is slow / never emits.
        lifecycleScope.launch {
            kotlinx.coroutines.withTimeoutOrNull(timeMillis = 300) {
                authRepo.state.collect { /* first emission unblocks splash */
                    sessionRestored = true
                    return@collect
                }
            }
            sessionRestored = true
        }

        setContent {
            AppTheme {
                val windowSizeClass = calculateWindowSizeClass(this)
                val authState by authRepo.state.collectAsStateWithLifecycle(initialValue = AuthState.SignedOut)
                AppNavHost(
                    startDestination = startDestinationFor(authState),
                    windowSizeClass = windowSizeClass,
                    onShareInvite = ::shareInvite,
                    onSignOut = ::signOut,
                )
            }
        }
    }

    private fun shareInvite(uri: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, uri)
        }
        startActivity(Intent.createChooser(intent, null))
    }

    private fun signOut() {
        lifecycleScope.launch { authRepo.signOut() }
    }
}
