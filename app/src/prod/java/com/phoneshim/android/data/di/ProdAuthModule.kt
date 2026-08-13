package com.phoneshim.android.data.di

import com.phoneshim.android.BuildConfig
import com.phoneshim.android.data.api.AuthApi
import com.phoneshim.android.data.repository.AuthRepositoryImpl
import com.phoneshim.android.data.repository.PendingAuthRepositoryImpl
import com.phoneshim.android.domain.repository.AuthRepository
import com.phoneshim.android.domain.repository.PendingAuthRepository
import com.phoneshim.android.domain.model.AuthFeatureAvailability
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
    companion object {
        @Provides
        @Singleton
        fun provideAuthFeatureAvailability() = AuthFeatureAvailability(
            // Credential Manager는 ID token을 반환하므로 서버 계약과 Web Client ID가 모두 준비된 경우에만 노출한다.
            canGoogleLogin = BuildConfig.GOOGLE_ID_TOKEN_LOGIN_ENABLED &&
                BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank(),
            shouldLoadRemoteProfile = true,
        )
    }

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
        implementation: PendingAuthRepositoryImpl,
    ): PendingAuthRepository
}

@Module
@InstallIn(SingletonComponent::class)
object ProdAuthApiModule {
    @Provides @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)
}
