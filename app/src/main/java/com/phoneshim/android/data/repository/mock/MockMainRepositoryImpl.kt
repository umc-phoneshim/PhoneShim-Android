package com.phoneshim.android.data.repository.mock

import com.phoneshim.android.data.mock.MockData
import com.phoneshim.android.domain.model.AppUsage
import com.phoneshim.android.domain.repository.MainRepository
import javax.inject.Inject

class MockMainRepositoryImpl @Inject constructor() : MainRepository {
    override suspend fun getTodayUsage(): Result<List<AppUsage>> =
        Result.success(MockData.todayUsage)
}