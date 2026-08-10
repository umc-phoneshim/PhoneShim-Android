package com.phoneshim.android.domain.model

data class UsageStatus(
    val monitoredAppId: String,
    val appName: String,
    val packageName: String,
    val appIcon: String? = null,
    val sortOrder: Int = 0,
    val targetMinutes: Int? = null,
    val targetCount: Int? = null,
    val usedMinutes: Int,
    val entryCount: Int,
)
