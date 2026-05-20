package com.apptest.feature.testing.domain.usecase

import com.apptest.feature.testing.data.TestingRepository
import com.apptest.feature.testing.domain.model.TestingSnapshot
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveTestingUseCase @Inject constructor(private val repo: TestingRepository) {
    operator fun invoke(): Flow<TestingSnapshot> = repo.observe()
}
