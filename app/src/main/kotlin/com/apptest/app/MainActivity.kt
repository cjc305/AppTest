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
import com.apptest.core.common.jwtSubject
import com.apptest.core.data.session.SessionStore
import com.apptest.core.designsystem.theme.AppTheme
import com.apptest.core.domain.auth.AuthRepository
import com.apptest.core.navigation.startDestinationFor
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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

        // Subscribe to FCM topic once the user is signed in. Each user subscribes to
        // "user_<uid>" so the backend can push without storing tokens. On sign-out / account
        // switch, unsubscribe the previous uid so the device stops receiving the old user's
        // notifications (prevents cross-account notification leak).
        lifecycleScope.launch {
            var lastUid: String? = null
            sessionStore.session
                .map { session -> session?.jwt?.let { it.jwtSubject() } }
                .distinctUntilChanged()
                .collect { uid ->
                    val previous = lastUid
                    if (previous != null && previous != uid) {
                        FirebaseMessaging.getInstance().unsubscribeFromTopic("user_$previous")
                            .addOnSuccessListener { android.util.Log.d("FCM", "unsubscribed user_$previous") }
                            .addOnFailureListener { android.util.Log.w("FCM", "unsubscribe failed: ${it.message}") }
                    }
                    lastUid = uid
                    if (uid != null) {
                        FirebaseMessaging.getInstance().subscribeToTopic("user_$uid")
                            .addOnSuccessListener { android.util.Log.d("FCM", "subscribed to user_$uid") }
                            .addOnFailureListener { android.util.Log.w("FCM", "subscribe failed: ${it.message}") }
                    }
                }
        }

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

