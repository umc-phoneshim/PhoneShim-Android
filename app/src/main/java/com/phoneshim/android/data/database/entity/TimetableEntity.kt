package com.phoneshim.android.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timetable_entries")
data class TimetableEntity(
    @PrimaryKey val id: String,
    val appName: String,
    val startedAt: Long,
    val durationMinutes: Int,
    val usageReason: String?,
)
