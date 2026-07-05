package com.phoneshim.android.domain.model

data class Goal(
    val id: String,
    val targetApps: List<AppUsage>,
    val dailyUsageLimitMinutes: Int,
    val accessCountLimit: Int,
    val description: String,
)
