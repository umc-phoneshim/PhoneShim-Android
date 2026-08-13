package com.phoneshim.android.data.di

import com.phoneshim.android.data.repository.MockAuthRepositoryImpl
import com.phoneshim.android.data.repository.MockPendingAuthRepositoryImpl
import com.phoneshim.android.domain.repository.AuthRepository
import com.phoneshim.android.domain.repository.PendingAuthRepository
import com.phoneshim.android.domain.model.AuthFeatureAvailability
import com.phoneshim.android.ui.features.auth.client.GoogleAuthClient
import com.phoneshim.android.ui.features.auth.client.MockGoogleAuthClientImpl
import com.phoneshim.android.ui.features.auth.client.KakaoAuthClient
import com.phoneshim.android.ui.features.auth.client.MockKakaoAuthClientImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DevAuthModule {
    companion object {
        @Provides
        @Singleton
        fun provideAuthFeatureAvailability() = AuthFeatureAvailability(
            canGoogleLogin = true,
            shouldLoadRemoteProfile = false,
        )
    }

    @Binds @Singleton
    abstract fun bindAuthRepository(implementation: MockAuthRepositoryImpl): AuthRepository

    @Binds @Singleton
    abstract fun bindGoogleAuthClient(implementation: MockGoogleAuthClientImpl): GoogleAuthClient

    @Binds @Singleton
    abstract fun bindKakaoAuthClient(implementation: MockKakaoAuthClientImpl): KakaoAuthClient

    @Binds @Singleton
    abstract fun bindPendingAuthRepository(
        implementation: MockPendingAuthRepositoryImpl,
    ): PendingAuthRepository
}
