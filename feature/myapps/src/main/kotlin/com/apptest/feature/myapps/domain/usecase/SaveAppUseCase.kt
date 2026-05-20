package com.apptest.feature.myapps.domain.usecase

import com.apptest.core.common.AppError
import com.apptest.core.common.AppResult
import com.apptest.core.common.DispatcherProvider
import com.apptest.core.domain.UseCase
import com.apptest.feature.myapps.data.MyAppsRepository
import com.apptest.feature.myapps.domain.PlayOptInUrlValidator
import com.apptest.feature.myapps.domain.model.AppDraft
import com.apptest.feature.myapps.domain.model.PlayUrlValidation
import javax.inject.Inject

/**
 * Persists a draft (create or update). Performs final-shot validation before hand-off; UI
 * already does per-keystroke checks but we don't trust UI alone.
 */
class SaveAppUseCase @Inject constructor(
    private val repo: MyAppsRepository,
    dispatchers: DispatcherProvider,
) : UseCase<AppDraft, String>(dispatchers) {

    override suspend fun execute(params: AppDraft): AppResult<String> {
        validate(params)?.let { return AppResult.Failure(it) }
        return repo.save(params)
    }

    private fun validate(draft: AppDraft): AppError? {
        if (draft.name.length !in 2..50) return AppError.Validation("name", "Name must be 2–50 chars")
        if (draft.description.length > 500) return AppError.Validation("description", "Max 500 chars")
        if (draft.packageName.isBlank()) return AppError.Validation("packageName", "Required")
        when (val v = PlayOptInUrlValidator.validate(draft.playOptInUrl)) {
            is PlayUrlValidation.Invalid -> return AppError.Validation("playOptInUrl", v.reason)
            PlayUrlValidation.Empty -> return AppError.Validation("playOptInUrl", "Required")
            PlayUrlValidation.Valid -> Unit
        }
        if (draft.requiredTesters !in 1..100) return AppError.Validation("requiredTesters", "Must be 1–100")
        if (draft.requiredDays !in 7..30) return AppError.Validation("requiredDays", "Must be 7–30")
        return null
    }
}
