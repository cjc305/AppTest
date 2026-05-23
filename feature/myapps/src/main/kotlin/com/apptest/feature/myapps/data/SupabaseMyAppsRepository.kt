package com.apptest.feature.myapps.data

import com.apptest.core.common.AppError
import com.apptest.core.common.AppResult
import com.apptest.core.common.DispatcherProvider
import com.apptest.core.network.apps.AppStatusBody
import com.apptest.core.network.apps.AppUpsertBody
import com.apptest.core.network.apps.SupabaseAppsApiService
import com.apptest.feature.myapps.domain.model.AppDraft
import com.apptest.feature.myapps.domain.model.MyAppsLoadStatus
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Real Supabase-backed [MyAppsRepository]. Replaces [FakeMyAppsRepository].
 *
 * Uses a [MutableStateFlow] cache. [observe] triggers an initial network load on first
 * collection; mutations update both Supabase and the local cache.
 *
 * V1: package_name, play_opt_in_url, required_testers, required_days not in DB —
 * defaulted to empty/"" / 12 / 14. Delete uses HTTP DELETE (no deleted_at column).
 */
@Singleton
class SupabaseMyAppsRepository @Inject constructor(
    private val appsApi: SupabaseAppsApiService,
    private val dispatchers: DispatcherProvider,
) : MyAppsRepository {

    private val _state = MutableStateFlow<List<OwnedAppRow>>(emptyList())
    private val _loadStatus = MutableStateFlow<MyAppsLoadStatus>(MyAppsLoadStatus.Idle)
    private val loadMutex = Mutex()

    /**
     * V1: package_name / play_opt_in_url / required_testers / required_days don't yet exist
     * server-side, so the user's editor input would be lost on reload. Cache them
     * process-locally keyed by app id so the editor shows back what the user typed within
     * the same process. (Cleared on process death — fix properly when schema migration lands.)
     *
     * HIGH-005: description IS in DB (AppDto.description) so it's not in DraftExtras — read
     * directly from the DTO via [toRow]. playOptInUrl remains client-side until V1 schema
     * migration adds the column.
     */
    private val draftExtras = java.util.concurrent.ConcurrentHashMap<String, DraftExtras>()

    private data class DraftExtras(
        val packageName: String,
        val playOptInUrl: String,
        val requiredTesters: Int,
        val requiredDays: Int,
    )

    override fun observe(): Flow<List<OwnedAppRow>> = flow {
        loadOnce()
        emitAll(_state.asStateFlow())
    }.flowOn(dispatchers.io)

    override fun loadStatus(): Flow<MyAppsLoadStatus> = _loadStatus.asStateFlow()

    /**
     * Idempotent first-load gate. Mutex serializes concurrent collectors so we don't fire
     * two parallel `listOwned` requests on simultaneous subscription. Re-attempts after
     * [Failed] so the user can retry by re-entering the screen.
     */
    private suspend fun loadOnce() {
        loadMutex.withLock {
            val status = _loadStatus.value
            if (status is MyAppsLoadStatus.Loaded || status is MyAppsLoadStatus.Loading) return
            _loadStatus.value = MyAppsLoadStatus.Loading
            try {
                val rows = appsApi.listOwned().map { it.toRow() }
                _state.value = rows
                _loadStatus.value = MyAppsLoadStatus.Loaded
            } catch (c: CancellationException) {
                _loadStatus.value = MyAppsLoadStatus.Idle  // allow retry on resubscribe
                throw c
            } catch (t: Throwable) {
                _loadStatus.value = MyAppsLoadStatus.Failed(AppError.fromThrowable(t))
            }
        }
    }

    override suspend fun get(id: String): OwnedAppRow? = withContext(dispatchers.io) {
        val cached = _state.value.firstOrNull { it.id == id }
            ?: runCatching { appsApi.getById("eq.$id").firstOrNull()?.toRow() }.getOrNull()
        cached?.mergeExtras(draftExtras[id])
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
            // Stash V1-only fields locally so subsequent edits show the user's input back.
            draftExtras[id] = DraftExtras(
                packageName = draft.packageName,
                playOptInUrl = draft.playOptInUrl,
                requiredTesters = draft.requiredTesters,
                requiredDays = draft.requiredDays,
            )
            refreshCache()
            AppResult.Success(id)
        } catch (c: CancellationException) { throw c }
        catch (t: Throwable) { AppResult.Failure(AppError.fromThrowable(t)) }
    }

    override suspend fun pause(id: String): AppResult<Unit> = patchStatus(id, "paused")
    override suspend fun resume(id: String): AppResult<Unit> = patchStatus(id, "recruiting")

    override suspend fun delete(id: String): AppResult<Unit> = withContext(dispatchers.io) {
        try {
            appsApi.softDelete("eq.$id").close()
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
        runCatching { appsApi.listOwned().map { it.toRow().mergeExtras(draftExtras[it.id]) } }
            .onSuccess { _state.value = it }
    }

    private fun OwnedAppRow.mergeExtras(extras: DraftExtras?): OwnedAppRow {
        if (extras == null) return this
        // HIGH-005 fix: also merge playOptInUrl (was being dropped between save and re-load).
        return copy(
            packageName = extras.packageName.ifBlank { packageName },
            playOptInUrl = extras.playOptInUrl.ifBlank { playOptInUrl },
            requiredTesters = extras.requiredTesters,
            requiredDays = extras.requiredDays,
        )
    }
}

// ─── Mapping helpers ──────────────────────────────────────────────────────────

private fun com.apptest.core.network.apps.AppDto.toRow() = OwnedAppRow(
    id = id,
    name = name,
    packageName = "",                       // V1: not in DB
    description = description,              // HIGH-005: description IS in DB (AppDto.description)
    playOptInUrl = "",                      // V1: not in DB (overlaid by draftExtras when present)
    status = status.toOwnedAppStatus(),
    currentTesters = 0,
    requiredTesters = 12,                   // V1: not in DB (overlaid by draftExtras)
    requiredDays = 14,                      // V1: not in DB (overlaid by draftExtras)
    daysLeft = 14,
)

private fun String.toOwnedAppStatus() = when (this) {
    "recruiting" -> OwnedAppStatus.Recruiting
    "paused" -> OwnedAppStatus.Paused
    "completed" -> OwnedAppStatus.Completed
    else -> OwnedAppStatus.Recruiting
}

private fun AppDraft.toUpsertBody() = AppUpsertBody(
    name = name,
    description = description,
    // V1: packageName, playOptInUrl, requiredTesters, requiredDays not in DB
)
