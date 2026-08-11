package com.phoneshim.android.domain.model

data class DailyUsageLog(
    val id: String,
    val monitoredAppId: String,
    val date: String,
    val usedMinutes: Int,
    val entryCount: Int,
)
