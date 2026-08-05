package com.phoneshim.android.data.di
import android.content.Context
import androidx.room.Room
import com.phoneshim.android.data.database.PhoneShimDatabase
import com.phoneshim.android.data.database.MIGRATION_4_5
import com.phoneshim.android.data.database.dao.GoalDao
import com.phoneshim.android.data.database.dao.ReminderDao
import com.phoneshim.android.data.database.dao.ReminderRestrictionDao
import com.phoneshim.android.data.database.dao.TimetableDao
import com.phoneshim.android.data.database.dao.UserProfileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PhoneShimDatabase =
        Room.databaseBuilder(context, PhoneShimDatabase::class.java, PhoneShimDatabase.DATABASE_NAME)
            .addMigrations(MIGRATION_4_5)
            // 1~3 버전의 기존 migration은 아직 제공되지 않아 해당 구버전에만 fallback을 유지한다.
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideReminderDao(database: PhoneShimDatabase): ReminderDao = database.reminderDao()

    @Provides
    fun provideTimetableDao(database: PhoneShimDatabase): TimetableDao = database.timetableDao()

    @Provides
    fun provideGoalDao(database: PhoneShimDatabase): GoalDao = database.goalDao()

    @Provides
    fun provideReminderRestrictionDao(database: PhoneShimDatabase): ReminderRestrictionDao =
        database.reminderRestrictionDao()

    @Provides
    fun provideUserProfileDao(database: PhoneShimDatabase): UserProfileDao = database.userProfileDao()
}
