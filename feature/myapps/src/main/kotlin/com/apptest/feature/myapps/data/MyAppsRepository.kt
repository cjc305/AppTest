package com.apptest.feature.myapps.data

import com.apptest.core.common.AppResult
import com.apptest.core.domain.Repository
import com.apptest.feature.myapps.domain.model.AppDraft
import com.apptest.feature.myapps.domain.model.MatchedTesterEmail
import com.apptest.feature.myapps.domain.model.MyAppsLoadStatus
import com.apptest.feature.myapps.domain.model.OwnedAppRow
import kotlinx.coroutines.flow.Flow

/**
 * Owned-apps aggregate for the dev side. List is a Flow so editor save reflects in list
 * immediately without manual refresh.
 *
 * V1: backed by [FakeMyAppsRepository] (in-memory). Real impl wires Supabase REST + Realtime
 * once APT-V1-R-040 / APT-API-006~008 land.
 */
interface MyAppsRepository : Repository {

    /** Live list of apps owned by current user. Emits whenever [save]/[delete] mutates state. */
    fun observe(): Flow<List<OwnedAppRow>>

    /**
     * Initial-load status. Combine with [observe] in UI to tell "loaded empty" apart from
     * "load failed". Implementations that never fail (e.g. fakes) may emit only [Loaded].
     */
    fun loadStatus(): Flow<MyAppsLoadStatus>

    /** Look up a single app by id for editing. Returns null if not found. */
    suspend fun get(id: String): OwnedAppRow?

    /** Create (id null) or update (id set). Returns id on success. */
    suspend fun save(draft: AppDraft): AppResult<String>

    /**
     * Flip a DRAFT/PAUSED app to ACTIVE — starts matching with testers.
     * Calls the `activate_app` Postgres RPC server-side (validates ownership).
     */
    suspend fun activate(id: String): AppResult<Unit>

    suspend fun pause(id: String): AppResult<Unit>
    suspend fun resume(id: String): AppResult<Unit>
    suspend fun delete(id: String): AppResult<Unit>

    /**
     * Plan A: list matched testers' emails for the given app so the owner can
     * paste them into Play Console's closed-test allowlist. Backed by the
     * `get_matched_tester_emails` RPC which enforces caller == app.owner_id.
     */
    suspend fun getMatchedTesterEmails(appId: String): AppResult<List<MatchedTesterEmail>>
}
