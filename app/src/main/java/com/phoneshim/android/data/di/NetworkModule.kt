package com.phoneshim.android.data.di

import com.phoneshim.android.data.api.AuthApi
import com.phoneshim.android.data.api.AuthInterceptor
import com.phoneshim.android.data.api.GoalApi
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
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * API 명세서 0_공통정보 기준.
 * TODO: 배포용 서버 주소가 정해지면 BuildConfig 로 분리하세요.
 *  에뮬레이터에서 로컬 서버에 붙을 때는 10.0.2.2 가 호스트의 localhost 입니다.
 */
private const val BASE_URL = "http://10.0.2.2:3000/"

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideGoalApi(retrofit: Retrofit): GoalApi = retrofit.create(GoalApi::class.java)

    @Provides
    @Singleton
    fun provideMainApi(retrofit: Retrofit): MainApi = retrofit.create(MainApi::class.java)

    @Provides
    @Singleton
    fun provideReminderApi(retrofit: Retrofit): ReminderApi = retrofit.create(ReminderApi::class.java)

    @Provides
    @Singleton
    fun provideReportApi(retrofit: Retrofit): ReportApi = retrofit.create(ReportApi::class.java)

    @Provides
    @Singleton
    fun provideMyPageApi(retrofit: Retrofit): MyPageApi = retrofit.create(MyPageApi::class.java)

    @Provides
    @Singleton
    fun provideUsageReasonApi(retrofit: Retrofit): UsageReasonApi =
        retrofit.create(UsageReasonApi::class.java)
}
