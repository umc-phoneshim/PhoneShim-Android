package com.phoneshim.android.auth

import com.phoneshim.android.domain.model.SocialProvider
import com.phoneshim.android.ui.features.auth.social.SocialAuthClient
import com.phoneshim.android.ui.features.auth.social.SocialAuthResult
import com.phoneshim.android.ui.features.auth.social.ForegroundActivityProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProdSocialAuthClient @Inject constructor(
    private val googleSocialAuthClient: GoogleSocialAuthClient,
    private val kakaoSocialAuthClient: KakaoSocialAuthClient,
    private val foregroundActivityProvider: ForegroundActivityProvider,
) : SocialAuthClient {
    override suspend fun authenticate(provider: SocialProvider): SocialAuthResult {
        val activity = try {
            foregroundActivityProvider.requireActivity()
        } catch (error: IllegalStateException) {
            return SocialAuthResult.Failure(error)
        }
        return when (provider) {
        SocialProvider.GOOGLE -> googleSocialAuthClient.authenticate(activity)
        SocialProvider.KAKAO -> kakaoSocialAuthClient.authenticate(activity)
        }
    }
}
