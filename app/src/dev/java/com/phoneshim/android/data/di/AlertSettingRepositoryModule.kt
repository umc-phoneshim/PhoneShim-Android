package com.phoneshim.android.data.di

import com.phoneshim.android.data.repository.fake.FakeAlertSettingRepositoryImpl
import com.phoneshim.android.domain.repository.AlertSettingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AlertSettingRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAlertSettingRepository(
        implementation: FakeAlertSettingRepositoryImpl,
    ): AlertSettingRepository
}
