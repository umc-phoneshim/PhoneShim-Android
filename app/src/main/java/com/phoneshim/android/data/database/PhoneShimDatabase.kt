package com.phoneshim.android.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.phoneshim.android.data.database.dao.GoalDao
import com.phoneshim.android.data.database.dao.ReminderDao
import com.phoneshim.android.data.database.dao.ReminderRestrictionDao
import com.phoneshim.android.data.database.dao.TimetableDao
import com.phoneshim.android.data.database.entity.AppGoalEntity
import com.phoneshim.android.data.database.entity.PhoneGoalEntity
import com.phoneshim.android.data.database.entity.ReminderEntity
import com.phoneshim.android.data.database.entity.ReminderRestrictionEntity
import com.phoneshim.android.data.database.entity.TimetableEntity

@Database(
    entities = [
        ReminderEntity::class,
        TimetableEntity::class,
        AppGoalEntity::class,
        PhoneGoalEntity::class,
        ReminderRestrictionEntity::class,
    ],
    version = 3,
)
abstract class PhoneShimDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
    abstract fun timetableDao(): TimetableDao
    abstract fun goalDao(): GoalDao
    abstract fun reminderRestrictionDao(): ReminderRestrictionDao

    companion object {
        const val DATABASE_NAME = "phoneshim.db"
    }
}

