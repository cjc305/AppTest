package com.apptest.feature.appdetail.data

import com.apptest.core.common.AppError
import com.apptest.core.common.AppResult
import com.apptest.core.common.ReputationTier
import com.apptest.feature.appdetail.domain.model.AppDetailData
import com.apptest.feature.appdetail.domain.model.MatchReason
import com.apptest.feature.appdetail.domain.model.OwnerInfo
import com.apptest.feature.appdetail.domain.model.Requirements
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay

/**
 * V1 demo fake. Returns mock for any appId; for unknown ids in tests returns NotFound to
 * exercise error path.
 */
@Singleton
class FakeAppDetailRepository @Inject constructor() : AppDetailRepository {

    override suspend fun getById(appId: String): AppResult<AppDetailData> {
        delay(250)
        return when (appId) {
            "" -> AppResult.Failure(AppError.NotFound("app"))
            else -> AppResult.Success(mockFor(appId))
        }
    }

    private fun mockFor(appId: String): AppDetailData = AppDetailData(
        id = appId,
        packageName = "com.example.noteflash",
        name = "NoteFlash",
        category = "Productivity",
        description = "A Pomodoro timer that turns focus sessions into flashcards. " +
            "Need testers for the iOS-style swipe gesture and Notion sync edge cases. " +
            "Daily usage estimated at 10 minutes plus 1 launch.",
        iconUrl = null,
        screenshotCount = 5,
        screenshotUrls = listOf(
            "https://picsum.photos/seed/${appId}1/400/800",
            "https://picsum.photos/seed/${appId}2/400/800",
            "https://picsum.photos/seed/${appId}3/400/800",
            "https://picsum.photos/seed/${appId}4/400/800",
            "https://picsum.photos/seed/${appId}5/400/800",
        ),
        owner = OwnerInfo(
            displayName = "Alex",
            tier = ReputationTier.Gold,
        ),
        requirements = Requirements(
            requiredDays = 14,
            requiredTesters = 12,
            currentTesters = 5,
            dailyMinutesEstimated = 10,
        ),
        matchReasons = listOf(
            MatchReason("Category match (Productivity)"),
            MatchReason("Your tier: Silver (high)"),
            MatchReason("Similar timezone"),
        ),
        playOptInUrl = "https://play.google.com/apps/testing/com.example.noteflash",
    )
}
