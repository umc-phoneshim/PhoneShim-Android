package com.phoneshim.android.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminders",
    indices = [Index(value = ["dateEpochDay"])],
)
data class ReminderEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val dateEpochDay: Long,
    val title: String,
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long,
    val restrictionMode: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

@Entity(
    tableName = "reminder_restricted_apps",
    primaryKeys = ["reminderId", "monitoredAppId"],
    foreignKeys = [
        ForeignKey(
            entity = ReminderEntity::class,
            parentColumns = ["id"],
            childColumns = ["reminderId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["reminderId"])],
)
data class ReminderRestrictedAppEntity(
    val reminderId: String,
    val monitoredAppId: String,
)

/** 빈 서버 목록도 정상 동기화된 캐시로 구분하기 위한 날짜별 marker. */
@Entity(tableName = "reminder_sync_state")
data class ReminderSyncStateEntity(
    @PrimaryKey val dateEpochDay: Long,
    val syncedAtEpochMillis: Long,
)
