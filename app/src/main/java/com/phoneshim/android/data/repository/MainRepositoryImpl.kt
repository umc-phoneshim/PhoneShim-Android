package com.phoneshim.android.data.repository

import com.phoneshim.android.data.api.MainApi
import com.phoneshim.android.domain.model.AppUsage
import com.phoneshim.android.domain.repository.MainRepository
import javax.inject.Inject

class MainRepositoryImpl @Inject constructor(
    private val mainApi: MainApi,
) : MainRepository {
    override suspend fun getTodayUsage(): Result<List<AppUsage>> = runCatching {
        mainApi.getTodayUsage().map {
            AppUsage(packageName = it.packageName, appName = it.appName, usageMinutes = it.usageMinutes)
        }
    }
}
