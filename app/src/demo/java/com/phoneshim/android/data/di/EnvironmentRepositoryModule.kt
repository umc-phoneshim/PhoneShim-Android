package com.phoneshim.android.data.di

import com.phoneshim.android.data.demo.DemoMainRepository
import com.phoneshim.android.data.demo.DemoGoalRepository
import com.phoneshim.android.data.demo.DemoMyPageRepository
import com.phoneshim.android.data.demo.DemoReportRepository
import com.phoneshim.android.data.demo.DemoReportUsageReasonRepository
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
    @Binds @Singleton abstract fun bindGoalRepository(impl: DemoGoalRepository): GoalRepository
    @Binds @Singleton abstract fun bindMainRepository(impl: DemoMainRepository): MainRepository
    @Binds @Singleton abstract fun bindReportRepository(impl: DemoReportRepository): ReportRepository
    @Binds @Singleton abstract fun bindMyPageRepository(impl: DemoMyPageRepository): MyPageRepository
    @Binds @Singleton abstract fun bindReportUsageReasonRepository(
        impl: DemoReportUsageReasonRepository,
    ): ReportUsageReasonRepository
}
