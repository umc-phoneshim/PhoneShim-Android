package com.phoneshim.android.data.di

import com.phoneshim.android.data.api.AuthApi
import com.phoneshim.android.data.api.DashboardApi
import com.phoneshim.android.data.api.DeviceUsageApi
import com.phoneshim.android.data.api.GoalApi
import com.phoneshim.android.data.api.HealthApi
import com.phoneshim.android.data.api.MyPageApi
import com.phoneshim.android.data.api.ReminderApi
import com.phoneshim.android.data.api.ReportApi
import com.phoneshim.android.data.api.UsageLogApi
import com.phoneshim.android.data.api.UsageReasonApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object AuthApiModule {
    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
object GoalApiModule {
    @Provides
    @Singleton
    fun provideGoalApi(retrofit: Retrofit): GoalApi = retrofit.create(GoalApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
object UsageLogApiModule {
    @Provides
    @Singleton
    fun provideUsageLogApi(retrofit: Retrofit): UsageLogApi = retrofit.create(UsageLogApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
object DashboardApiModule {
    @Provides
    @Singleton
    fun provideDashboardApi(retrofit: Retrofit): DashboardApi = retrofit.create(DashboardApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
object DeviceUsageApiModule {
    @Provides
    @Singleton
    fun provideDeviceUsageApi(retrofit: Retrofit): DeviceUsageApi =
        retrofit.create(DeviceUsageApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
object ReminderApiModule {
    @Provides
    @Singleton
    fun provideReminderApi(retrofit: Retrofit): ReminderApi = retrofit.create(ReminderApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
object ReportApiModule {
    @Provides
    @Singleton
    fun provideReportApi(retrofit: Retrofit): ReportApi = retrofit.create(ReportApi::class.java)

    @Provides
    @Singleton
    fun provideUsageReasonApi(retrofit: Retrofit): UsageReasonApi =
        retrofit.create(UsageReasonApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
object MyPageApiModule {
    @Provides
    @Singleton
    fun provideMyPageApi(retrofit: Retrofit): MyPageApi = retrofit.create(MyPageApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
object HealthApiModule {
    @Provides
    @Singleton
    fun provideHealthApi(retrofit: Retrofit): HealthApi = retrofit.create(HealthApi::class.java)
}
