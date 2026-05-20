package com.apptest.feature.profile.data

import com.apptest.core.common.AppResult
import com.apptest.core.domain.Repository
import com.apptest.feature.profile.domain.model.ProfileData

interface ProfileRepository : Repository {
    suspend fun getMyProfile(): AppResult<ProfileData>
}
