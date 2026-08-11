package com.phoneshim.android.data.di

import com.phoneshim.android.data.repository.ReminderRepositoryImpl
import com.phoneshim.android.domain.repository.ReminderRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** prod 빌드는 실제 Reminder API를 사용한다. */
@Module
@InstallIn(SingletonComponent::class)
abstract class ReminderRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindReminderRepository(impl: ReminderRepositoryImpl): ReminderRepository
}
