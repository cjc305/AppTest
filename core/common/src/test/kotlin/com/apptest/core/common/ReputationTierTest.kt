package com.apptest.core.common

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test

class ReputationTierTest {

    @Test fun `fromScore boundaries map per spec`() {
        // (low, expected)
        val cases = listOf(
            -50 to ReputationTier.Newcomer,
            0 to ReputationTier.Newcomer,
            199 to ReputationTier.Newcomer,
            200 to ReputationTier.Bronze,
            399 to ReputationTier.Bronze,
            400 to ReputationTier.Silver,
            599 to ReputationTier.Silver,
            600 to ReputationTier.Gold,
            799 to ReputationTier.Gold,
            800 to ReputationTier.Platinum,
            1000 to ReputationTier.Platinum,
            10_000 to ReputationTier.Platinum, // upper unbounded
        )
        cases.forEach { (score, expected) ->
            assertWithMessage("score=$score").that(ReputationTier.fromScore(score)).isEqualTo(expected)
        }
    }

    @Test fun `entries are ordered by tier rank`() {
        // Important downstream: comparing by ordinal is used in profile breakdown
        assertThat(ReputationTier.entries.map { it.name }).containsExactly(
            "Newcomer", "Bronze", "Silver", "Gold", "Platinum",
        ).inOrder()
    }
}
