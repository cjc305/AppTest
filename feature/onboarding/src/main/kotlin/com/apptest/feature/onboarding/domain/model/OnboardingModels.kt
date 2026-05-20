package com.apptest.feature.onboarding.domain.model

enum class OnboardingIntent {
    FindTesters,    // I have an app, need testers
    TestOthers,     // I want to test others' apps (V1 default)
    Both;

    val label: String
        get() = when (this) {
            FindTesters -> "Find testers for my app"
            TestOthers -> "Test others' apps"
            Both -> "Both"
        }
}

data class OnboardingDraft(
    val intent: OnboardingIntent = OnboardingIntent.TestOthers,
    val categories: Set<String> = emptySet(),
    val languages: Set<String> = setOf("en"),         // defaults from system later
) {
    val isValidStep2: Boolean get() = categories.isNotEmpty()
    val isValidStep3: Boolean get() = languages.isNotEmpty()
}

object OnboardingCatalog {
    val ALL_CATEGORIES: List<String> = listOf(
        "Productivity", "Game", "Tools", "Health",
        "Finance", "Photo", "Travel", "Education",
    )
    val ALL_LANGUAGES: List<String> = listOf("en", "zh-TW", "ja", "ko")
}
