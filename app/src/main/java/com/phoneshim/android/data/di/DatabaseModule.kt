package com.phoneshim.android.data.di

import android.content.Context
import androidx.room.Room
import com.phoneshim.android.data.database.PhoneShimDatabase
import com.phoneshim.android.data.database.dao.ReminderDao
import com.phoneshim.android.data.database.dao.TimetableDao
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
        Room.databaseBuilder(context, PhoneShimDatabase::class.java, PhoneShimDatabase.DATABASE_NAME).build()

    @Provides
    fun provideReminderDao(database: PhoneShimDatabase): ReminderDao = database.reminderDao()

    @Provides
    fun provideTimetableDao(database: PhoneShimDatabase): TimetableDao = database.timetableDao()
}
