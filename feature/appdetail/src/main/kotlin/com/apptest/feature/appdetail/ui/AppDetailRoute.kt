package com.apptest.feature.appdetail.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Stateful entry. Owns VM injection + side-effect consumption (opens Play Store via Intent
 * when VM fires an event — keeps Composition pure).
 */
@Composable
fun AppDetailRoute(
    onNavigateUp: () -> Unit,
    viewModel: AppDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.openPlayStoreEvents.collect { url ->
            try {
                val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (_: ActivityNotFoundException) {
                viewModel.onJoinIntentFailed()
            } catch (t: Throwable) {
                android.util.Log.w("AppDetail", "Play Store intent failed: ${t.message}")
                viewModel.onJoinIntentFailed()
            }
        }
    }

    AppDetailScreen(
        state = state,
        onNavigateUp = onNavigateUp,
        onJoin = viewModel::onJoinClicked,
        onRetry = viewModel::load,
    )
}
