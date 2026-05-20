package com.apptest.core.designsystem.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.dp

/**
 * Shimmer placeholder for loading skeletons. Animates a moving highlight band across
 * a [MaterialTheme.colorScheme.surfaceContainer] background.
 *
 * Caller must provide layout dimensions via `modifier` (e.g. `Modifier.size(80.dp, 20.dp)`).
 * Without dimensions the placeholder collapses to zero size.
 *
 * Pair with `AppLoadingState` (in :core:ui) for full-list skeleton — placeholder is also
 * usable inline (e.g., avatar shimmer while remote image loads).
 */
@Composable
fun AppPlaceholder(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
) {
    val base = MaterialTheme.colorScheme.surfaceContainer
    val highlight = MaterialTheme.colorScheme.surfaceContainerHigh
        .copy(alpha = 0.7f)
        .compositeOver(base)

    val transition = rememberInfiniteTransition(label = "placeholder-shimmer")
    val progress by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer-progress",
    )

    val brush = Brush.linearGradient(
        colors = listOf(base, highlight, base),
        start = Offset(progress * 200f, 0f),
        end = Offset((progress + 0.5f) * 200f, 200f),
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(brush),
    )
}
