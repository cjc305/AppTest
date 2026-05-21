package com.apptest.feature.home.data

import com.apptest.core.common.AppResult
import com.apptest.core.common.ReputationTier
import com.apptest.feature.home.domain.model.ActiveTest
import com.apptest.feature.home.domain.model.HomeData
import com.apptest.feature.home.domain.model.HomeUser
import com.apptest.feature.home.domain.model.MatchedApp
import com.apptest.feature.home.domain.model.OwnedApp
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay

/**
 * V1 demo fake: returns deterministic mock data + simulated 300ms latency.
 * Replace with real impl (Retrofit + Supabase) when APT-V1-R-040 lands; binding in
 * `HomeDataModule` switches automatically.
 *
 * Singleton so screen-rotation doesn't reset state (would matter once we add cache).
 */
@Singleton
class FakeHomeRepository @Inject constructor() : HomeRepository {

    override suspend fun getHomeData(): AppResult<HomeData> {
        delay(300)  // simulate network latency
        return AppResult.Success(MOCK_HOME_DATA)
    }

    private companion object {
        val MOCK_HOME_DATA = HomeData(
            user = HomeUser(
                displayName = "Alice",
                tier = ReputationTier.Silver,
                credits = 4,
            ),
            newMatch = MatchedApp(
                id = "demo-app-001",
                name = "NoteFlash",
                category = "Productivity",
                description = "A Pomodoro timer that turns focus sessions into flashcards. Need testers for the iOS-style swipe gesture.",
                testersNeeded = 7,
                matchScore = 87,
            ),
            activeTests = listOf(
                ActiveTest(appId = "a1", appName = "QuickHabit", day = 5, totalDays = 14, pingStatusOk = true),
                ActiveTest(appId = "a2", appName = "PixelBudget", day = 12, totalDays = 14, pingStatusOk = false),
                ActiveTest(appId = "a3", appName = "TripPal", day = 1, totalDays = 14, pingStatusOk = true),
            ),
            myApps = listOf(
                OwnedApp(id = "my1", name = "MyApp1", currentTesters = 8, requiredTesters = 12),
                OwnedApp(id = "my2", name = "MyApp2", currentTesters = 0, requiredTesters = 12),
            ),
        )
    }
}
