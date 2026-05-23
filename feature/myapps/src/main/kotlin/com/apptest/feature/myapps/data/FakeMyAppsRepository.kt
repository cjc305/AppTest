package com.apptest.feature.myapps.data

import com.apptest.core.common.AppResult
import com.apptest.feature.myapps.domain.model.AppDraft
import com.apptest.feature.myapps.domain.model.MyAppsLoadStatus
import com.apptest.feature.myapps.domain.model.OwnedAppRow
import com.apptest.feature.myapps.domain.model.OwnedAppStatus
import kotlinx.coroutines.flow.flowOf
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * V1 in-memory fake. State persists for app process lifetime; rotating screen / popping
 * editor preserves changes. Cleared on process death.
 *
 * Singleton so list ↔ editor share the same mutable state.
 */
@Singleton
class FakeMyAppsRepository @Inject constructor() : MyAppsRepository {

    private val _items = MutableStateFlow(seed())

    override fun observe(): Flow<List<OwnedAppRow>> = _items.asStateFlow()

    override fun loadStatus(): Flow<MyAppsLoadStatus> = flowOf(MyAppsLoadStatus.Loaded)

    override suspend fun get(id: String): OwnedAppRow? {
        delay(80)
        return _items.value.firstOrNull { it.id == id }
    }

    override suspend fun save(draft: AppDraft): AppResult<String> {
        delay(200)
        val id = draft.id ?: UUID.randomUUID().toString()
        val newRow = OwnedAppRow(
            id = id,
            name = draft.name,
            packageName = draft.packageName,
            description = draft.description,            // HIGH-005: now round-trips
            playOptInUrl = draft.playOptInUrl,          // HIGH-005: now round-trips
            status = OwnedAppStatus.Recruiting,
            currentTesters = if (draft.id == null) 0 else (_items.value.firstOrNull { it.id == id }?.currentTesters ?: 0),
            requiredTesters = draft.requiredTesters,
            requiredDays = draft.requiredDays,
            daysLeft = draft.requiredDays,
        )
        _items.update { list ->
            if (list.any { it.id == id }) list.map { if (it.id == id) newRow else it } else list + newRow
        }
        return AppResult.Success(id)
    }

    override suspend fun pause(id: String): AppResult<Unit> = mutateStatus(id, OwnedAppStatus.Paused)
    override suspend fun resume(id: String): AppResult<Unit> = mutateStatus(id, OwnedAppStatus.Recruiting)

    override suspend fun delete(id: String): AppResult<Unit> {
        delay(100)
        _items.update { list -> list.filterNot { it.id == id } }
        return AppResult.Success(Unit)
    }

    private suspend fun mutateStatus(id: String, status: OwnedAppStatus): AppResult<Unit> {
        delay(80)
        _items.update { list -> list.map { if (it.id == id) it.copy(status = status) else it } }
        return AppResult.Success(Unit)
    }

    private companion object {
        fun seed(): List<OwnedAppRow> = listOf(
            OwnedAppRow(
                id = "my1", name = "MyApp1", packageName = "com.example.myapp1",
                description = "Sample first app for testing UI flows.",
                playOptInUrl = "https://play.google.com/apps/testing/com.example.myapp1",
                status = OwnedAppStatus.Recruiting,
                currentTesters = 8, requiredTesters = 12, requiredDays = 14, daysLeft = 6,
            ),
            OwnedAppRow(
                id = "my2", name = "MyApp2", packageName = "com.example.myapp2",
                description = "Sample second app for testing UI flows.",
                playOptInUrl = "https://play.google.com/apps/testing/com.example.myapp2",
                status = OwnedAppStatus.Recruiting,
                currentTesters = 0, requiredTesters = 12, requiredDays = 14, daysLeft = 14,
            ),
        )
    }
}
