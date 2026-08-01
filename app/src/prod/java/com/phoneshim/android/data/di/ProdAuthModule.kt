package com.phoneshim.android.data.di

import com.phoneshim.android.data.api.AuthApi
import com.phoneshim.android.data.repository.AuthRepositoryImpl
import com.phoneshim.android.data.repository.UnavailablePendingAuthRepositoryImpl
import com.phoneshim.android.domain.repository.AuthRepository
import com.phoneshim.android.domain.repository.PendingAuthRepository
import com.phoneshim.android.ui.features.auth.client.GoogleAuthClient
import com.phoneshim.android.ui.features.auth.client.GoogleAuthClientImpl
import com.phoneshim.android.ui.features.auth.client.KakaoAuthClient
import com.phoneshim.android.ui.features.auth.client.KakaoAuthClientImpl
import com.phoneshim.android.ui.features.auth.client.KakaoSdkInitializer
import com.phoneshim.android.ui.features.auth.client.SocialSdkInitializer
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class ProdAuthBindingModule {
    @Binds @Singleton
    abstract fun bindAuthRepository(implementation: AuthRepositoryImpl): AuthRepository

    @Binds @Singleton
    abstract fun bindGoogleAuthClient(implementation: GoogleAuthClientImpl): GoogleAuthClient

    @Binds @Singleton
    abstract fun bindKakaoAuthClient(implementation: KakaoAuthClientImpl): KakaoAuthClient

    @Binds @IntoSet
    abstract fun bindKakaoSdkInitializer(
        implementation: KakaoSdkInitializer,
    ): SocialSdkInitializer

    @Binds @Singleton
    abstract fun bindPendingAuthRepository(
        implementation: UnavailablePendingAuthRepositoryImpl,
    ): PendingAuthRepository
}

@Module
@InstallIn(SingletonComponent::class)
object ProdAuthApiModule {
    @Provides @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)
}
