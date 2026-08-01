package com.phoneshim.android.data.di

import com.phoneshim.android.data.api.GoalApi
import com.phoneshim.android.data.api.HealthApi
import com.phoneshim.android.data.api.MainApi
import com.phoneshim.android.data.api.MyPageApi
import com.phoneshim.android.data.api.ReminderApi
import com.phoneshim.android.data.api.ReportApi
import com.phoneshim.android.data.api.UsageReasonApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object GoalApiModule {
    @Provides
    @Singleton
    fun provideGoalApi(retrofit: Retrofit): GoalApi = retrofit.create(GoalApi::class.java)
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
