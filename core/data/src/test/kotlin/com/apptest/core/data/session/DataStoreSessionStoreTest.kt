package com.apptest.core.data.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.google.common.truth.Truth.assertThat
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStoreSessionStoreTest {

    @get:Rule val tempDir = TemporaryFolder()

    private lateinit var scope: CoroutineScope
    private lateinit var ds: DataStore<Preferences>
    private lateinit var store: DataStoreSessionStore

    @Before fun setUp() {
        scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher() + Dispatchers.Unconfined)
        val file = File(tempDir.root, "test.preferences_pb")
        ds = PreferenceDataStoreFactory.create(scope = scope) { file }
        store = DataStoreSessionStore(ds)
    }

    @After fun tearDown() {
        scope.cancel()
    }

    @Test fun `empty store emits null`() = runTest {
        assertThat(store.session.first()).isNull()
    }

    @Test fun `save then read returns same session`() = runTest {
        val s = AuthSession(jwt = "j", refreshToken = "r", expiresAtEpochMs = 5_000L)
        store.save(s)
        assertThat(store.session.first()).isEqualTo(s)
    }

    @Test fun `clear empties the store`() = runTest {
        store.save(AuthSession("j", "r", 5_000L))
        store.clear()
        assertThat(store.session.first()).isNull()
    }

    @Test fun `token returns jwt when not expired`() = runTest {
        val future = System.currentTimeMillis() + 60_000L
        store.save(AuthSession("jwt-fresh", "r", future))
        assertThat(store.token()).isEqualTo("jwt-fresh")
    }

    @Test fun `token returns null when expired`() = runTest {
        store.save(AuthSession("jwt-stale", "r", expiresAtEpochMs = 1L))
        assertThat(store.token()).isNull()
    }

    @Test fun `token returns null when no session`() = runTest {
        assertThat(store.token()).isNull()
    }
}
