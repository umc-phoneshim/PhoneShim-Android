package com.phoneshim.android.blocking.di

import android.content.Context
import com.phoneshim.android.blocking.detection.ForegroundAppDetector
import com.phoneshim.android.blocking.detection.UsageMinutesReader
import com.phoneshim.android.blocking.policy.BlockingPolicyProvider
import com.phoneshim.android.blocking.policy.ReminderSchedulePolicyProvider
import com.phoneshim.android.blocking.policy.RoomBlockingPolicyProvider
import com.phoneshim.android.blocking.policy.SchedulePolicyProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BlockingModule {

    @Provides
    @Singleton
    fun provideForegroundAppDetector(
        @ApplicationContext context: Context,
    ): ForegroundAppDetector = ForegroundAppDetector(context)

    @Provides
    @Singleton
    fun provideUsageMinutesReader(
        @ApplicationContext context: Context,
    ): UsageMinutesReader = UsageMinutesReader(context)
}

/**
 * 정책 provider 바인딩.
 *  - 쿼터(온보딩/설정): RoomBlockingPolicyProvider
 *  - 일정(리마인더)   : ReminderSchedulePolicyProvider
 * 구현체 교체가 필요하면 여기 bind 만 바꾼다.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class BlockingPolicyBindModule {

    @Binds
    @Singleton
    abstract fun bindPolicyProvider(
        impl: RoomBlockingPolicyProvider,
    ): BlockingPolicyProvider

    @Binds
    @Singleton
    abstract fun bindSchedulePolicyProvider(
        impl: ReminderSchedulePolicyProvider,
    ): SchedulePolicyProvider
}
