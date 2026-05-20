package com.apptest.feature.myapps.data

import com.apptest.core.common.AppResult
import com.apptest.core.domain.Repository
import com.apptest.feature.myapps.domain.model.AppDraft
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

    /** Look up a single app by id for editing. Returns null if not found. */
    suspend fun get(id: String): OwnedAppRow?

    /** Create (id null) or update (id set). Returns id on success. */
    suspend fun save(draft: AppDraft): AppResult<String>

    suspend fun pause(id: String): AppResult<Unit>
    suspend fun resume(id: String): AppResult<Unit>
    suspend fun delete(id: String): AppResult<Unit>
}
