package com.apptest.core.data.session

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AuthSessionTest {

    private val sample = AuthSession(jwt = "j", refreshToken = "r", expiresAtEpochMs = 1_000L)

    @Test fun `not expired when now is before expiry`() {
        assertThat(sample.isExpired(nowEpochMs = 999L)).isFalse()
    }

    @Test fun `expired exactly at expiry`() {
        assertThat(sample.isExpired(nowEpochMs = 1_000L)).isTrue()
    }

    @Test fun `expired after expiry`() {
        assertThat(sample.isExpired(nowEpochMs = 5_000L)).isTrue()
    }
}
