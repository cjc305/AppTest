package com.apptest.feature.myapps.data

import com.apptest.core.common.AppError
import com.apptest.core.common.AppResult
import com.apptest.core.common.DispatcherProvider
import com.apptest.core.network.apps.AppDeleteBody
import com.apptest.core.network.apps.AppStatusBody
import com.apptest.core.network.apps.AppUpsertBody
import com.apptest.core.network.apps.SupabaseAppsApiService
import com.apptest.feature.myapps.domain.model.AppDraft
import com.apptest.feature.myapps.domain.model.OwnedAppRow
import com.apptest.feature.myapps.domain.model.OwnedAppStatus
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Real Supabase-backed [MyAppsRepository]. Replaces [FakeMyAppsRepository].
 *
 * Uses a [MutableStateFlow] cache. [observe] triggers an initial network load on first
 * collection; mutations update both Supabase and the local cache.
 */
@Singleton
class SupabaseMyAppsRepository @Inject constructor(
    private val appsApi: SupabaseAppsApiService,
    private val dispatchers: DispatcherProvider,
) : MyAppsRepository {

    private val _state = MutableStateFlow<List<OwnedAppRow>>(emptyList())
    private var loaded = false

    override fun observe(): Flow<List<OwnedAppRow>> = flow {
        if (!loaded) {
            runCatching { appsApi.listOwned().map { it.toRow() } }
                .onSuccess { _state.value = it; loaded = true }
        }
        emitAll(_state.asStateFlow())
    }.flowOn(dispatchers.io)

    override suspend fun get(id: String): OwnedAppRow? = withContext(dispatchers.io) {
        _state.value.firstOrNull { it.id == id }
            ?: runCatching { appsApi.getById("eq.$id").firstOrNull()?.toRow() }.getOrNull()
    }

    override suspend fun save(draft: AppDraft): AppResult<String> = withContext(dispatchers.io) {
        try {
            val id = if (draft.id == null) {
                val created = appsApi.create(draft.toUpsertBody()).firstOrNull()
                    ?: throw Exception("No row returned after create")
                created.id
            } else {
                appsApi.update("eq.${draft.id}", draft.toUpsertBody()).close()
                draft.id
            }
            refreshCache()
            AppResult.Success(id)
        } catch (c: CancellationException) { throw c }
        catch (t: Throwable) { AppResult.Failure(AppError.fromThrowable(t)) }
    }

    override suspend fun pause(id: String): AppResult<Unit> = patchStatus(id, "paused")
    override suspend fun resume(id: String): AppResult<Unit> = patchStatus(id, "recruiting")

    override suspend fun delete(id: String): AppResult<Unit> = withContext(dispatchers.io) {
        try {
            appsApi.softDelete("eq.$id", AppDeleteBody()).close()
            _state.value = _state.value.filterNot { it.id == id }
            AppResult.Success(Unit)
        } catch (c: CancellationException) { throw c }
        catch (t: Throwable) { AppResult.Failure(AppError.fromThrowable(t)) }
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private suspend fun patchStatus(id: String, status: String): AppResult<Unit> =
        withContext(dispatchers.io) {
            try {
                appsApi.updateStatus("eq.$id", AppStatusBody(status)).close()
                _state.value = _state.value.map { row ->
                    if (row.id == id) row.copy(status = status.toOwnedAppStatus()) else row
                }
                AppResult.Success(Unit)
            } catch (c: CancellationException) { throw c }
            catch (t: Throwable) { AppResult.Failure(AppError.fromThrowable(t)) }
        }

    private suspend fun refreshCache() {
        runCatching { appsApi.listOwned().map { it.toRow() } }
            .onSuccess { _state.value = it }
    }
}

// ─── Mapping helpers ──────────────────────────────────────────────────────────

private fun com.apptest.core.network.apps.AppDto.toRow() = OwnedAppRow(
    id = id,
    name = name,
    packageName = packageName,
    status = status.toOwnedAppStatus(),
    currentTesters = 0,
    requiredTesters = requiredTesters,
    requiredDays = requiredDays,
    daysLeft = requiredDays,
)

private fun String.toOwnedAppStatus() = when (this) {
    "recruiting" -> OwnedAppStatus.Recruiting
    "paused" -> OwnedAppStatus.Paused
    "completed" -> OwnedAppStatus.Completed
    else -> OwnedAppStatus.Recruiting
}

private fun AppDraft.toUpsertBody() = AppUpsertBody(
    name = name,
    packageName = packageName,
    description = description,
    playOptInUrl = playOptInUrl,
    requiredTesters = requiredTesters,
    requiredDays = requiredDays,
)
