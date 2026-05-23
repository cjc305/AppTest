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
import com.apptest.core.data.session.SessionStore
import com.apptest.core.designsystem.theme.AppTheme
import com.apptest.core.domain.auth.AuthRepository
import com.apptest.core.navigation.startDestinationFor
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

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
    @Inject lateinit var sessionStore: SessionStore

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

        // Mark session restored once the first DataStore read completes (real session value
        // observed, not the StateFlow placeholder). 1500ms fallback covers slow cold-start I/O
        // — anything longer would harm splash UX more than briefly showing SignIn would.
        lifecycleScope.launch {
            withTimeoutOrNull(timeMillis = 1500L) {
                sessionStore.session.first()
            }
            sessionRestored = true
        }

        // HIGH-003 (audit 2026-05-23): FCM topic subscribe/unsubscribe moved to
        // [FcmTopicManager], wired in AppTestApplication.onCreate. Lives in ApplicationScope
        // and persists `last_subscribed_uid` + pending unsubscribes in DataStore so account
        // switches survive process death.

        setContent {
            AppTheme {
                val windowSizeClass = calculateWindowSizeClass(this)
                val authState by authRepo.state.collectAsStateWithLifecycle(initialValue = AuthState.SignedOut)
                // CRIT-3 fix: NavHost ignores startDestination changes after first composition.
                // Re-key on authState so sign-in/sign-out re-creates the graph, popping the user
                // off SignIn → Onboarding/Home (or vice versa) without explicit nav.navigate calls.
                androidx.compose.runtime.key(authState) {
                    AppNavHost(
                        startDestination = startDestinationFor(authState),
                        windowSizeClass = windowSizeClass,
                        onShareInvite = ::shareInvite,
                        onSignOut = ::signOut,
                    )
                }
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
        lifecycleScope.launch {
            val result = authRepo.signOut()
            if (result is com.apptest.core.common.AppResult.Failure) {
                android.util.Log.w("MainActivity", "signOut error: ${result.error}")
            }
            // AuthState → SignedOut is driven reactively regardless of API result
        }
    }
}

