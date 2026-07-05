package com.phoneshim.android.domain.model

data class TimetableEntry(
    val id: String,
    val appName: String,
    val startedAt: Long,
    val durationMinutes: Int,
    val usageReason: String?,
)
