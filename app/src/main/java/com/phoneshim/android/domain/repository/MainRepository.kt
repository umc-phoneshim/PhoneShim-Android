package com.phoneshim.android.domain.repository

import com.phoneshim.android.domain.model.AppUsage

interface MainRepository {
    suspend fun getTodayUsage(): Result<List<AppUsage>>
}
