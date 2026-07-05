package com.phoneshim.android.domain.model

data class Reminder(
    val id: String,
    val title: String,
    val scheduledAt: Long,
)
