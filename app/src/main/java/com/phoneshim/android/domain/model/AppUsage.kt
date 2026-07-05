package com.phoneshim.android.domain.model

data class AppUsage(
    val packageName: String,
    val appName: String,
    val usageMinutes: Int,
)
