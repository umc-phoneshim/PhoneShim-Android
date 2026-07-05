package com.phoneshim.android.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.phoneshim.android.data.database.dao.ReminderDao
import com.phoneshim.android.data.database.dao.TimetableDao
import com.phoneshim.android.data.database.entity.ReminderEntity
import com.phoneshim.android.data.database.entity.TimetableEntity

@Database(
    entities = [ReminderEntity::class, TimetableEntity::class],
    version = 1,
)
abstract class PhoneShimDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
    abstract fun timetableDao(): TimetableDao

    companion object {
        const val DATABASE_NAME = "phoneshim.db"
    }
}
