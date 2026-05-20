package com.apptest.feature.appdetail.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.apptest.core.designsystem.components.AppText
import com.apptest.core.designsystem.spacing.AppSpacing
import com.apptest.core.designsystem.theme.AppL10n

/**
 * Horizontal pager carousel for AppDetail screenshots. V1: shows fake picsum images so the UI
 * is sideload-presentable; real Firebase Storage URLs land per `_specs/backend_architecture.md`
 * §6 once R-043 backend integration ships.
 *
 * If [urls] is empty (cold-start before Repository load), falls back to a single placeholder card
 * via [ScreenshotsPlaceholderRow].
 */
@Composable
fun ScreenshotCarousel(
    urls: List<String>,
    fallbackCount: Int,
    modifier: Modifier = Modifier,
) {
    if (urls.isEmpty()) {
        ScreenshotsPlaceholderRow(count = fallbackCount, modifier = modifier)
        return
    }
    val state = rememberPagerState(pageCount = { urls.size })

    androidx.compose.foundation.layout.Column(modifier = modifier) {
        HorizontalPager(
            state = state,
            contentPadding = PaddingValues(horizontal = AppSpacing.Md),
            pageSpacing = AppSpacing.Sm,
            modifier = Modifier.fillMaxWidth().height(280.dp),
        ) { page ->
            AsyncImage(
                model = urls[page],
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.medium),
            )
        }
        DotIndicator(
            count = urls.size,
            current = state.currentPage,
            modifier = Modifier
                .padding(top = AppSpacing.Sm)
                .fillMaxWidth(),
        )
    }
}

@Composable
private fun DotIndicator(count: Int, current: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
    ) {
        repeat(count) { index ->
            val color = if (index == current) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceVariant
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}

@Composable
fun ScreenshotsPlaceholderRow(count: Int, modifier: Modifier = Modifier) {
    val l = AppL10n.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainer),
        contentAlignment = Alignment.Center,
    ) {
        AppText(
            text = l.appdetail_screenshots_count.format(count),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
