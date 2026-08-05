package com.phoneshim.android.data.di

import com.phoneshim.android.data.repository.AuthRepositoryImpl
import com.phoneshim.android.data.repository.DashboardRepositoryImpl
import com.phoneshim.android.data.repository.DeviceUsageRepositoryImpl
import com.phoneshim.android.data.repository.fake.FakePackageMonitoredAppResolver
import com.phoneshim.android.data.repository.fake.FakeUsageReasonRepositoryImpl
import com.phoneshim.android.data.repository.GoalRepositoryImpl
import com.phoneshim.android.data.repository.InstalledAppsRepositoryImpl
import com.phoneshim.android.data.repository.MyPageRepositoryImpl
import com.phoneshim.android.data.repository.ReminderRepositoryImpl
import com.phoneshim.android.data.repository.ReportRepositoryImpl
import com.phoneshim.android.data.repository.ReportUsageReasonRepositoryImpl
import com.phoneshim.android.data.repository.UsageLogRepositoryImpl
import com.phoneshim.android.domain.repository.AuthRepository
import com.phoneshim.android.domain.repository.DashboardRepository
import com.phoneshim.android.domain.repository.DeviceUsageRepository
import com.phoneshim.android.domain.repository.GoalRepository
import com.phoneshim.android.domain.repository.InstalledAppsRepository
import com.phoneshim.android.domain.repository.MyPageRepository
import com.phoneshim.android.domain.repository.PackageMonitoredAppResolver
import com.phoneshim.android.domain.repository.ReminderRepository
import com.phoneshim.android.domain.repository.ReportRepository
import com.phoneshim.android.domain.repository.ReportUsageReasonRepository
import com.phoneshim.android.domain.repository.UsageLogRepository
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
    abstract fun bindUsageLogRepository(impl: UsageLogRepositoryImpl): UsageLogRepository

    @Binds
    @Singleton
    abstract fun bindDashboardRepository(impl: DashboardRepositoryImpl): DashboardRepository

    @Binds
    @Singleton
    abstract fun bindDeviceUsageRepository(impl: DeviceUsageRepositoryImpl): DeviceUsageRepository

    /**
     * packageName -> monitoredAppId 매핑. 노뱅의 MonitoredApp 도메인이 develop에 아직 안 들어와서
     * Fake로 스텁 처리합니다. 실제 도메인이 공개되면 실구현으로 교체하고 Fake를 지우세요.
     */
    @Binds
    @Singleton
    abstract fun bindPackageMonitoredAppResolver(
        impl: FakePackageMonitoredAppResolver,
    ): PackageMonitoredAppResolver

    @Binds
    @Singleton
    abstract fun bindReminderRepository(impl: ReminderRepositoryImpl): ReminderRepository

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
