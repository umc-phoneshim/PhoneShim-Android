package com.phoneshim.android.domain.model

data class DashboardSummary(
    val date: String,
    val targetMinutes: Int? = null,
    val usedMinutes: Int,
    val remainingMinutes: Int? = null,
    val isExceeded: Boolean,
)
