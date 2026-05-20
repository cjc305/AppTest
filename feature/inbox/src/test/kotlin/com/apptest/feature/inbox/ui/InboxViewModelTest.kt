package com.apptest.feature.inbox.ui

import app.cash.turbine.test
import com.apptest.core.common.AppResult
import com.apptest.core.domain.inbox.InboxNotification
import com.apptest.core.domain.inbox.InboxNotificationType
import com.apptest.core.domain.inbox.InboxRepository
import com.apptest.feature.inbox.domain.usecase.ObserveInboxUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.google.common.truth.Truth.assertThat

@OptIn(ExperimentalCoroutinesApi::class)
class InboxViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: InboxRepository
    private lateinit var observeUseCase: ObserveInboxUseCase
    private val notificationsFlow = MutableStateFlow<List<InboxNotification>>(emptyList())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repo = mockk()
        every { repo.observe() } returns notificationsFlow
        coEvery { repo.markRead(any()) } returns AppResult.Success(Unit)
        coEvery { repo.markAllRead() } returns AppResult.Success(Unit)
        observeUseCase = ObserveInboxUseCase(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading before repository emits`() = runTest {
        val flow = MutableStateFlow<List<InboxNotification>>(emptyList())
        every { repo.observe() } returns flow
        val vm = InboxViewModel(ObserveInboxUseCase(repo), repo)

        // Before any emission except the empty initial, state starts as Loading
        // Note: stateIn with initial = Loading means the first value is Loading
        assertThat(vm.state.value).isEqualTo(InboxUiState.Loading)
    }

    @Test
    fun `state becomes Empty when repository emits an empty list`() = runTest {
        val vm = InboxViewModel(observeUseCase, repo)

        vm.state.test {
            // Initial Loading
            awaitItem() // Loading

            // Flow already has emptyList() so Empty should appear
            val state = awaitItem()
            assertThat(state).isEqualTo(InboxUiState.Empty)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `state becomes Loaded with items when repository emits non-empty list`() = runTest {
        val vm = InboxViewModel(observeUseCase, repo)

        vm.state.test {
            awaitItem() // Loading or Empty for initial emptyList

            // Emit items into the flow
            val items = listOf(
                InboxNotification(
                    id = "n1",
                    type = InboxNotificationType.NewMatch,
                    title = "New match",
                    body = "Test description",
                    timestamp = Instant.now(),
                    isRead = false,
                    deepLink = null,
                ),
                InboxNotification(
                    id = "n2",
                    type = InboxNotificationType.System,
                    title = "System",
                    body = "Info",
                    timestamp = Instant.now(),
                    isRead = true,
                    deepLink = null,
                ),
            )
            notificationsFlow.value = items

            val state = awaitItem()
            assertThat(state).isInstanceOf(InboxUiState.Loaded::class.java)
            val loaded = state as InboxUiState.Loaded
            assertThat(loaded.items).hasSize(2)
            // Only n1 is unread — verifies unread count so users see accurate badge
            assertThat(loaded.unreadCount).isEqualTo(1)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
