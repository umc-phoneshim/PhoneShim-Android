package com.phoneshim.android.data.di

import com.phoneshim.android.data.repository.AuthRepositoryImpl
import com.phoneshim.android.data.repository.fake.FakeUsageReasonRepositoryImpl
import com.phoneshim.android.data.repository.InstalledAppsRepositoryImpl
import com.phoneshim.android.data.repository.ReminderRepositoryImpl
import com.phoneshim.android.domain.repository.AuthRepository
import com.phoneshim.android.domain.repository.InstalledAppsRepository
import com.phoneshim.android.domain.repository.ReminderRepository
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
    abstract fun bindInstalledAppsRepository(
        impl: InstalledAppsRepositoryImpl,
    ): InstalledAppsRepository

    @Binds
    @Singleton
    abstract fun bindReminderRepository(impl: ReminderRepositoryImpl): ReminderRepository

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

}
