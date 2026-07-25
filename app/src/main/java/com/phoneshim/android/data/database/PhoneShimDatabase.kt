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
import com.phoneshim.android.data.database.entity.ReminderRestrictionEntity
import com.phoneshim.android.data.database.entity.TimetableEntity
import com.phoneshim.android.data.database.entity.UserProfileEntity

@Database(
    entities = [
        ReminderEntity::class,
        TimetableEntity::class,
        AppGoalEntity::class,
        PhoneGoalEntity::class,
        ReminderRestrictionEntity::class,
        UserProfileEntity::class,
    ],
    // 4: user_profile_cache 추가 (온보딩 성별·나이 저장)
    version = 4,
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

