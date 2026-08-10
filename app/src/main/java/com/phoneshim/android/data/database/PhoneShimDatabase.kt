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
    // 4: user_profile_cache 추가 (온보딩 성별·나이 저장)
    // 5: 목표 캐시에 서버 식별자(monitoredAppId/appGoalId/serverGoalId)와 targetCount 추가
    //    Reminder API 전체 필드, restricted app 관계, 날짜별 sync marker 추가
    // 6: app_goal_cache 에 goalReason 추가 (설정 화면의 어플 목표 문구)
    version = 6,
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
