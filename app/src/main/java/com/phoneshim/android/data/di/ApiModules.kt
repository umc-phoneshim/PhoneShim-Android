package com.phoneshim.android.data.di

import com.phoneshim.android.data.api.AppGoalApi
import com.phoneshim.android.data.api.AuthApi
import com.phoneshim.android.data.api.GoalApi
import com.phoneshim.android.data.api.HealthApi
import com.phoneshim.android.data.api.MainApi
import com.phoneshim.android.data.api.MonitoredAppApi
import com.phoneshim.android.data.api.MyPageApi
import com.phoneshim.android.data.api.ReminderApi
import com.phoneshim.android.data.api.ReportApi
import com.phoneshim.android.data.api.TotalGoalApi
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

// 목표 도메인 3종. 기존 단일 GoalApi를 대체합니다(GoalApi.kt 주석 참고).
@Module
@InstallIn(SingletonComponent::class)
object MonitoredAppApiModule {
    @Provides
    @Singleton
    fun provideMonitoredAppApi(retrofit: Retrofit): MonitoredAppApi =
        retrofit.create(MonitoredAppApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
object TotalGoalApiModule {
    @Provides
    @Singleton
    fun provideTotalGoalApi(retrofit: Retrofit): TotalGoalApi =
        retrofit.create(TotalGoalApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
object AppGoalApiModule {
    @Provides
    @Singleton
    fun provideAppGoalApi(retrofit: Retrofit): AppGoalApi =
        retrofit.create(AppGoalApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
object MainApiModule {
    @Provides
    @Singleton
    fun provideMainApi(retrofit: Retrofit): MainApi = retrofit.create(MainApi::class.java)
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
