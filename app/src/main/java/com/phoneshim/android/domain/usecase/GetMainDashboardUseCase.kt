package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.AppUsage
import com.phoneshim.android.domain.repository.MainRepository
import javax.inject.Inject

class GetMainDashboardUseCase @Inject constructor(
    private val mainRepository: MainRepository,
) {
    suspend operator fun invoke(): Result<List<AppUsage>> =
        mainRepository.getTodayUsage()
}
