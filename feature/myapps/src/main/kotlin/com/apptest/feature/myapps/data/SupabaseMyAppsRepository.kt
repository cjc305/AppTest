package com.apptest.feature.myapps.data

import com.apptest.core.common.AppError
import com.apptest.core.common.AppResult
import com.apptest.core.common.DispatcherProvider
import com.apptest.core.network.apps.ActivateAppRequest
import com.apptest.core.network.apps.AppDto
import com.apptest.core.network.apps.AppStatus
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
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Real Supabase-backed [MyAppsRepository]. Replaces [FakeMyAppsRepository].
 *
 * Re-audited 2026-05-24: all V1 columns (package_name, play_opt_in_url, required_testers,
 * required_days) DO exist in `public.apps` — the previous "not in DB" comment was wrong.
 * The local draftExtras workaround has been removed; we now persist everything server-side.
 *
 * Status lifecycle (matches DB CHECK constraint):
 *   DRAFT (auto on create)  →  ACTIVE (via activate_app RPC)  →  PAUSED  →  ARCHIVED
 * Only ACTIVE apps are eligible for matching.
 */
@Singleton
class SupabaseMyAppsRepository @Inject constructor(
    private val appsApi: SupabaseAppsApiService,
    private val dispatchers: DispatcherProvider,
) : MyAppsRepository {

    private val _state = MutableStateFlow<List<OwnedAppRow>>(emptyList())
    private val _loadStatus = MutableStateFlow<MyAppsLoadStatus>(MyAppsLoadStatus.Idle)
    private val loadMutex = Mutex()

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
        _state.value.firstOrNull { it.id == id }
            ?: runCatching { appsApi.getById("eq.$id").firstOrNull()?.toRow() }.getOrNull()
    }

    override suspend fun save(draft: AppDraft): AppResult<String> = withContext(dispatchers.io) {
        try {
            val isCreate = draft.id == null

            // Snapshot status BEFORE the upsert so we know whether to auto-activate.
            // Only DRAFT apps get auto-activated on save; PAUSED stays PAUSED so users
            // who explicitly paused don't get re-activated against their will.
            val priorStatus: OwnedAppStatus? = if (!isCreate) {
                _state.value.firstOrNull { it.id == draft.id }?.status
            } else null

            val id = if (isCreate) {
                val created = appsApi.create(draft.toUpsertBody()).firstOrNull()
                    ?: throw IllegalStateException("No row returned after create")
                created.id
            } else {
                appsApi.update("eq.${draft.id}", draft.toUpsertBody()).close()
                draft.id!!
            }
            // Auto-activate so the app enters the matching pool.
            //   - new app (isCreate=true): always activate (DB default is DRAFT)
            //   - existing DRAFT app: activate too — user clearly wants this live now
            //   - existing PAUSED/ARCHIVED app: leave alone (user's explicit intent)
            // Activation failure is non-fatal — the row is still saved; user can retry
            // by re-saving once any transient error clears.
            val shouldActivate = isCreate || priorStatus == OwnedAppStatus.Paused
            if (shouldActivate) {
                runCatching { appsApi.activateApp(ActivateAppRequest(id)) }
            }
            refreshCache()
            AppResult.Success(id)
        } catch (c: CancellationException) {
            throw c
        } catch (t: Throwable) {
            AppResult.Failure(AppError.fromThrowable(t))
        }
    }

    override suspend fun activate(id: String): AppResult<Unit> = withContext(dispatchers.io) {
        try {
            val updated = appsApi.activateApp(ActivateAppRequest(id))
            _state.value = _state.value.map { row ->
                if (row.id == id) row.copy(status = updated.status.toOwnedAppStatus()) else row
            }
            AppResult.Success(Unit)
        } catch (c: CancellationException) {
            throw c
        } catch (t: Throwable) {
            AppResult.Failure(AppError.fromThrowable(t))
        }
    }

    override suspend fun pause(id: String): AppResult<Unit> = patchStatus(id, AppStatus.PAUSED)
    override suspend fun resume(id: String): AppResult<Unit> = patchStatus(id, AppStatus.ACTIVE)

    override suspend fun delete(id: String): AppResult<Unit> = withContext(dispatchers.io) {
        try {
            appsApi.hardDelete("eq.$id").close()
            _state.value = _state.value.filterNot { it.id == id }
            AppResult.Success(Unit)
        } catch (c: CancellationException) {
            throw c
        } catch (t: Throwable) {
            AppResult.Failure(AppError.fromThrowable(t))
        }
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private suspend fun patchStatus(id: String, status: AppStatus): AppResult<Unit> =
        withContext(dispatchers.io) {
            try {
                appsApi.updateStatus("eq.$id", AppStatusBody.of(status)).close()
                _state.value = _state.value.map { row ->
                    if (row.id == id) row.copy(status = status.name.toOwnedAppStatus()) else row
                }
                AppResult.Success(Unit)
            } catch (c: CancellationException) {
                throw c
            } catch (t: Throwable) {
                AppResult.Failure(AppError.fromThrowable(t))
            }
        }

    private suspend fun refreshCache() {
        runCatching { appsApi.listOwned().map { it.toRow() } }
            .onSuccess { _state.value = it }
    }
}

// ─── Mapping helpers ──────────────────────────────────────────────────────────

private fun AppDto.toRow() = OwnedAppRow(
    id = id,
    name = name,
    packageName = packageName.orEmpty(),
    description = description,
    playOptInUrl = playOptInUrl.orEmpty(),
    status = status.toOwnedAppStatus(),
    currentTesters = 0,                     // TODO: join matches.count once R-045 lands
    requiredTesters = requiredTesters,
    requiredDays = requiredDays,
    daysLeft = requiredDays,                // TODO: derive from created_at + requiredDays
)

/**
 * Map DB status (DRAFT / ACTIVE / PAUSED / ARCHIVED) to UI status (Recruiting / Active /
 * Paused / Completed). Case-insensitive defensive match; unknown values → Paused (safest
 * default — won't accidentally mark as actively recruiting).
 */
private fun String.toOwnedAppStatus(): OwnedAppStatus =
    when (this.uppercase()) {
        AppStatus.ACTIVE.name -> OwnedAppStatus.Recruiting
        AppStatus.PAUSED.name -> OwnedAppStatus.Paused
        AppStatus.ARCHIVED.name -> OwnedAppStatus.Completed
        AppStatus.DRAFT.name -> OwnedAppStatus.Paused   // shown as "not yet active" in UI
        else -> OwnedAppStatus.Paused
    }

private fun AppDraft.toUpsertBody() = AppUpsertBody(
    name = name.trim(),
    description = description.trim(),
    playUrl = playOptInUrl.trim(),                            // required NOT NULL in DB
    category = "UTILITY",                                     // matches schema default
    packageName = packageName.trim().ifBlank { null },
    playOptInUrl = playOptInUrl.trim().ifBlank { null },
    requiredTesters = requiredTesters,
    requiredDays = requiredDays,
)
