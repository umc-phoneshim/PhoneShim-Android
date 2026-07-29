package com.phoneshim.android.domain.repository

import com.phoneshim.android.domain.model.UsageReasonSubmission

interface UsageReasonRepository {
    suspend fun saveUsageReason(submission: UsageReasonSubmission): Result<Unit>
}
