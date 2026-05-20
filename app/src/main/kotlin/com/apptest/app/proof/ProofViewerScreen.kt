@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.apptest.app.proof

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.apptest.core.designsystem.components.AppIcon
import com.apptest.core.designsystem.components.AppText
import com.apptest.core.designsystem.spacing.AppSpacing
import com.apptest.core.designsystem.theme.AppL10n
import com.apptest.core.ui.components.AppTopBar

/**
 * Fullscreen proof card viewer. URL pattern matches `backend_architecture.md` §6:
 * `https://apptest.dev/v/{proofId}.png`. Until the proof generator service ships
 * (APT-V1-R-048), the URL is a placeholder and Coil will display its error state.
 */
@Composable
fun ProofViewerScreen(
    proofId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val l = AppL10n.current
    val url = "https://apptest.dev/v/$proofId.png"

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Black,
        topBar = {
            AppTopBar(
                title = l.cta_back.let { /* TopBar wants a title; use proof id short */ "Proof · $proofId" },
                navIcon = {
                    IconButton(onClick = onBack) {
                        AppIcon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = l.cta_back,
                        )
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
                .padding(AppSpacing.Md),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = url,
                contentDescription = "Proof $proofId",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
            AppText(
                text = "$proofId",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.padding(AppSpacing.Sm),
            )
        }
    }
}
