package com.phoneshim.android.data.di

import com.phoneshim.android.data.repository.AuthRepositoryImpl
import com.phoneshim.android.data.repository.fake.FakeUsageReasonRepositoryImpl
import com.phoneshim.android.data.repository.GoalRepositoryImpl
import com.phoneshim.android.data.repository.MainRepositoryImpl
import com.phoneshim.android.data.repository.MyPageRepositoryImpl
import com.phoneshim.android.data.repository.ReminderRepositoryImpl
import com.phoneshim.android.data.repository.ReportRepositoryImpl
import com.phoneshim.android.data.repository.mock.MockMainRepositoryImpl
import com.phoneshim.android.domain.repository.AuthRepository
import com.phoneshim.android.domain.repository.GoalRepository
import com.phoneshim.android.domain.repository.MainRepository
import com.phoneshim.android.domain.repository.MyPageRepository
import com.phoneshim.android.domain.repository.ReminderRepository
import com.phoneshim.android.domain.repository.ReportRepository
import com.phoneshim.android.domain.repository.UsageReasonRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindGoalRepository(impl: GoalRepositoryImpl): GoalRepository

    @Binds
    @Singleton
    abstract fun bindMainRepository(impl: MockMainRepositoryImpl): MainRepository

    @Binds
    @Singleton
    abstract fun bindReminderRepository(impl: ReminderRepositoryImpl): ReminderRepository

    @Binds
    @Singleton
    abstract fun bindReportRepository(impl: ReportRepositoryImpl): ReportRepository

    @Binds
    @Singleton
    abstract fun bindMyPageRepository(impl: MyPageRepositoryImpl): MyPageRepository

    @Binds
    @Singleton
    abstract fun bindUsageReasonRepository(
        impl: FakeUsageReasonRepositoryImpl,
    ): UsageReasonRepository
}
