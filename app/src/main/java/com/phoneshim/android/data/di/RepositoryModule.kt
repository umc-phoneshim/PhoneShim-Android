package com.phoneshim.android.data.di

import com.phoneshim.android.data.repository.AuthRepositoryImpl
import com.phoneshim.android.data.repository.fake.FakeUsageReasonRepositoryImpl
import com.phoneshim.android.data.repository.GoalRepositoryImpl
import com.phoneshim.android.data.repository.InstalledAppsRepositoryImpl
import com.phoneshim.android.data.repository.MainRepositoryImpl
import com.phoneshim.android.data.repository.MyPageRepositoryImpl
import com.phoneshim.android.data.repository.ReportRepositoryImpl
import com.phoneshim.android.data.repository.ReportUsageReasonRepositoryImpl
import com.phoneshim.android.data.repository.mock.MockMainRepositoryImpl
import com.phoneshim.android.domain.repository.AuthRepository
import com.phoneshim.android.domain.repository.GoalRepository
import com.phoneshim.android.domain.repository.InstalledAppsRepository
import com.phoneshim.android.domain.repository.MainRepository
import com.phoneshim.android.domain.repository.MyPageRepository
import com.phoneshim.android.domain.repository.ReportRepository
import com.phoneshim.android.domain.repository.ReportUsageReasonRepository
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
    abstract fun bindInstalledAppsRepository(
        impl: InstalledAppsRepositoryImpl,
    ): InstalledAppsRepository

    @Binds
    @Singleton
    abstract fun bindMainRepository(impl: MockMainRepositoryImpl): MainRepository

    @Binds
    @Singleton
    abstract fun bindReportRepository(impl: ReportRepositoryImpl): ReportRepository

    @Binds
    @Singleton
    abstract fun bindMyPageRepository(impl: MyPageRepositoryImpl): MyPageRepository

    /**
     * 앱 차단 오버레이 경로. 아직 Fake 입니다.
     * TODO: 이 경로가 실제 API(POST /api/usage-reasons)를 쓰려면 monitoredAppId 가 필요한데
     *  오버레이는 packageName 만 알고 있습니다. MonitoredApp 도메인에서 packageName → monitoredAppId
     *  조회가 제공되면 ReportUsageReasonRepositoryImpl 로 통합하고 Fake 를 제거하세요.
     */
    @Binds
    @Singleton
    abstract fun bindUsageReasonRepository(
        impl: FakeUsageReasonRepositoryImpl,
    ): UsageReasonRepository

    /** 리포트 도메인의 사용 사유 입력/달력. 실제 API 연동 완료. */
    @Binds
    @Singleton
    abstract fun bindReportUsageReasonRepository(
        impl: ReportUsageReasonRepositoryImpl,
    ): ReportUsageReasonRepository
}
