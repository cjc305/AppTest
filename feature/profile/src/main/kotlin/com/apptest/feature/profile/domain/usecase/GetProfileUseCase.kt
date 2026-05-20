package com.apptest.feature.profile.domain.usecase

import com.apptest.core.common.AppResult
import com.apptest.core.common.DispatcherProvider
import com.apptest.core.domain.NoParamUseCase
import com.apptest.feature.profile.data.ProfileRepository
import com.apptest.feature.profile.domain.model.ProfileData
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(
    private val repo: ProfileRepository,
    dispatchers: DispatcherProvider,
) : NoParamUseCase<ProfileData>(dispatchers) {
    override suspend fun execute(params: Unit): AppResult<ProfileData> = repo.getMyProfile()
}
