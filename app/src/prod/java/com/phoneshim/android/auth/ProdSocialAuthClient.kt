package com.phoneshim.android.auth

import android.app.Activity
import com.phoneshim.android.domain.model.SocialProvider
import com.phoneshim.android.ui.features.auth.social.SocialAuthClient
import com.phoneshim.android.ui.features.auth.social.SocialAuthResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProdSocialAuthClient @Inject constructor(
    private val googleSocialAuthClient: GoogleSocialAuthClient,
    private val kakaoSocialAuthClient: KakaoSocialAuthClient,
) : SocialAuthClient {
    override suspend fun authenticate(
        activity: Activity,
        provider: SocialProvider,
    ): SocialAuthResult = when (provider) {
        SocialProvider.GOOGLE -> googleSocialAuthClient.authenticate(activity)
        SocialProvider.KAKAO -> kakaoSocialAuthClient.authenticate(activity)
    }
}
