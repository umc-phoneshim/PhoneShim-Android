package com.phoneshim.android.domain.repository

import com.phoneshim.android.domain.model.DashboardSummary

interface DashboardRepository {
    suspend fun getDailySummary(): Result<DashboardSummary>
}
