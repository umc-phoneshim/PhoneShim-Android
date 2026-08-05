package com.phoneshim.android.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.phoneshim.android.data.database.dao.GoalDao
import com.phoneshim.android.data.database.dao.ReminderDao
import com.phoneshim.android.data.database.dao.ReminderRestrictionDao
import com.phoneshim.android.data.database.dao.TimetableDao
import com.phoneshim.android.data.database.dao.UserProfileDao
import com.phoneshim.android.data.database.entity.AppGoalEntity
import com.phoneshim.android.data.database.entity.PhoneGoalEntity
import com.phoneshim.android.data.database.entity.ReminderEntity
import com.phoneshim.android.data.database.entity.ReminderRestrictedAppEntity
import com.phoneshim.android.data.database.entity.ReminderRestrictionEntity
import com.phoneshim.android.data.database.entity.ReminderSyncStateEntity
import com.phoneshim.android.data.database.entity.TimetableEntity
import com.phoneshim.android.data.database.entity.UserProfileEntity

@Database(
    entities = [
        ReminderEntity::class,
        ReminderRestrictedAppEntity::class,
        ReminderSyncStateEntity::class,
        TimetableEntity::class,
        AppGoalEntity::class,
        PhoneGoalEntity::class,
        ReminderRestrictionEntity::class,
        UserProfileEntity::class,
    ],
    // 5: Reminder API 전체 필드, restricted app 관계, 날짜별 sync marker 추가
    version = 5,
)
abstract class PhoneShimDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
    abstract fun timetableDao(): TimetableDao
    abstract fun goalDao(): GoalDao
    abstract fun reminderRestrictionDao(): ReminderRestrictionDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        const val DATABASE_NAME = "phoneshim.db"
    }
}
