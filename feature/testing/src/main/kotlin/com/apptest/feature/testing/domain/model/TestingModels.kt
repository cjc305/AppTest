package com.apptest.feature.testing.domain.model

enum class TestStatus { Active, AtRisk, Completed, Abandoned }
enum class TestFilter { Active, Done, All }

data class ActiveTestEntry(
    val testId: String,
    val appId: String,
    val appName: String,
    val day: Int,
    val totalDays: Int,
    val pingStatusOk: Boolean,        // false = > 24h no ping
    val status: TestStatus,           // Active or AtRisk for this list
)

data class CompletedTestEntry(
    val testId: String,
    val appId: String,
    val appName: String,
    val daysCompleted: Int,
    val reputationGained: Int,
    val proofId: String?,             // null until generated
)

data class TestingSnapshot(
    val active: List<ActiveTestEntry>,
    val completed: List<CompletedTestEntry>,
)
