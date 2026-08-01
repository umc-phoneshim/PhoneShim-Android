package com.phoneshim.android.data.di

import com.phoneshim.android.data.repository.MyPageRepositoryImpl
import com.phoneshim.android.data.repository.GoalRepositoryImpl
import com.phoneshim.android.data.repository.ReportRepositoryImpl
import com.phoneshim.android.data.repository.ReportUsageReasonRepositoryImpl
import com.phoneshim.android.data.repository.mock.MockMainRepositoryImpl
import com.phoneshim.android.domain.repository.MainRepository
import com.phoneshim.android.domain.repository.GoalRepository
import com.phoneshim.android.domain.repository.MyPageRepository
import com.phoneshim.android.domain.repository.ReportRepository
import com.phoneshim.android.domain.repository.ReportUsageReasonRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class EnvironmentRepositoryModule {
    @Binds @Singleton abstract fun bindGoalRepository(impl: GoalRepositoryImpl): GoalRepository
    @Binds @Singleton abstract fun bindMainRepository(impl: MockMainRepositoryImpl): MainRepository
    @Binds @Singleton abstract fun bindReportRepository(impl: ReportRepositoryImpl): ReportRepository
    @Binds @Singleton abstract fun bindMyPageRepository(impl: MyPageRepositoryImpl): MyPageRepository
    @Binds @Singleton abstract fun bindReportUsageReasonRepository(
        impl: ReportUsageReasonRepositoryImpl,
    ): ReportUsageReasonRepository
}
