package com.phoneshim.android.auth

import com.phoneshim.android.domain.repository.AuthRepository
import com.phoneshim.android.ui.features.auth.social.SocialAuthClient
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DevAuthModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        implementation: FakeAuthRepository,
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindSocialAuthClient(
        implementation: FakeSocialAuthClient,
    ): SocialAuthClient
}
