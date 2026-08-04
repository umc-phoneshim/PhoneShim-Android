package com.phoneshim.android.data.di

import com.phoneshim.android.data.repository.fake.FakeReminderRepositoryImpl
import com.phoneshim.android.domain.repository.ReminderRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** dev 빌드에서는 인증 서버 없이 Reminder UI와 CRUD 흐름을 검증한다. */
@Module
@InstallIn(SingletonComponent::class)
abstract class ReminderRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindReminderRepository(impl: FakeReminderRepositoryImpl): ReminderRepository
}
