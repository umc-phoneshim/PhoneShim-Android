package com.phoneshim.android.data.di

import com.phoneshim.android.data.repository.DashboardRepositoryImpl
import com.phoneshim.android.data.repository.DeviceUsageRepositoryImpl
import com.phoneshim.android.data.repository.OverlayUsageReasonRepositoryImpl
import com.phoneshim.android.data.repository.GoalRepositoryImpl
import com.phoneshim.android.data.repository.InstalledAppsRepositoryImpl
import com.phoneshim.android.data.repository.MonitoredAppPackageResolver
import com.phoneshim.android.data.repository.MonitoredAppRepositoryImpl
import com.phoneshim.android.data.repository.MyPageRepositoryImpl
import com.phoneshim.android.data.repository.ReportRepositoryImpl
import com.phoneshim.android.data.repository.ReportUsageReasonRepositoryImpl
import com.phoneshim.android.data.repository.UsageLogRepositoryImpl
import com.phoneshim.android.domain.repository.DashboardRepository
import com.phoneshim.android.domain.repository.DeviceUsageRepository
import com.phoneshim.android.domain.repository.GoalRepository
import com.phoneshim.android.domain.repository.InstalledAppsRepository
import com.phoneshim.android.domain.repository.MonitoredAppRepository
import com.phoneshim.android.domain.repository.MyPageRepository
import com.phoneshim.android.domain.repository.PackageMonitoredAppResolver
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

    @Binds
    @Singleton
    abstract fun bindMonitoredAppRepository(
        impl: MonitoredAppRepositoryImpl,
    ): MonitoredAppRepository

    // packageName -> monitoredAppId 매핑. MonitoredApp 도메인 위에서 캐시 우선으로 변환합니다.
    @Binds
    @Singleton
    abstract fun bindPackageMonitoredAppResolver(
        impl: MonitoredAppPackageResolver,
    ): PackageMonitoredAppResolver

    @Binds
    @Singleton
    abstract fun bindReportRepository(impl: ReportRepositoryImpl): ReportRepository

    @Binds
    @Singleton
    abstract fun bindMyPageRepository(impl: MyPageRepositoryImpl): MyPageRepository

    /** 앱 차단 오버레이의 packageName을 monitoredAppId로 변환해 실제 API에 저장합니다. */
    @Binds
    @Singleton
    abstract fun bindUsageReasonRepository(
        impl: OverlayUsageReasonRepositoryImpl,
    ): UsageReasonRepository

    /** 리포트 도메인의 사용 사유 입력/달력. 실제 API 연동 완료. */
    @Binds
    @Singleton
    abstract fun bindReportUsageReasonRepository(
        impl: ReportUsageReasonRepositoryImpl,
    ): ReportUsageReasonRepository
}
