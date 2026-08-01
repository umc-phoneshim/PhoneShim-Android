package com.phoneshim.android.auth

import com.phoneshim.android.domain.repository.AuthRepository
import com.phoneshim.android.ui.features.auth.social.SocialAuthClient
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class ProdAuthBindingModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        implementation: RemoteAuthRepository,
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindSocialAuthClient(
        implementation: ProdSocialAuthClient,
    ): SocialAuthClient
}

@Module
@InstallIn(SingletonComponent::class)
object ProdAuthApiModule {
    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)
}
