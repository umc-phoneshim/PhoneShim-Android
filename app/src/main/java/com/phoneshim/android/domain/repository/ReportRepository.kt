package com.phoneshim.android.domain.repository

import com.phoneshim.android.domain.model.DailyReport

interface ReportRepository {
    suspend fun getDailyReport(date: String): Result<DailyReport>
    suspend fun submitUsageReason(entryId: String, reason: String): Result<Unit>
}
