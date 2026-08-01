package com.phoneshim.android.auth

import android.app.Activity
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.phoneshim.android.BuildConfig
import com.phoneshim.android.ui.features.auth.social.SocialAuthResult
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class KakaoSocialAuthClient @Inject constructor() {
    suspend fun authenticate(activity: Activity): SocialAuthResult {
        if (BuildConfig.KAKAO_NATIVE_APP_KEY.isBlank()) {
            return SocialAuthResult.Failure(
                IllegalStateException("KAKAO_NATIVE_APP_KEY가 설정되지 않았습니다."),
            )
        }

        return suspendCoroutine { continuation ->
            fun completeWithToken(token: OAuthToken?, error: Throwable?) {
                if (error != null || token == null) {
                    continuation.resume(
                        SocialAuthResult.Failure(
                            error ?: IllegalStateException("Kakao access token이 없습니다."),
                        ),
                    )
                    return
                }

                UserApiClient.instance.me { user, _ ->
                    continuation.resume(
                        SocialAuthResult.Success(
                            providerAccessToken = token.accessToken,
                            providerUserId = user?.id?.toString(),
                            email = user?.kakaoAccount?.email,
                        ),
                    )
                }
            }

            fun loginWithKakaoAccount() {
                UserApiClient.instance.loginWithKakaoAccount(activity, callback = ::completeWithToken)
            }

            if (UserApiClient.instance.isKakaoTalkLoginAvailable(activity)) {
                UserApiClient.instance.loginWithKakaoTalk(activity) { token, error ->
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        continuation.resume(SocialAuthResult.Cancelled)
                    } else if (error != null) {
                        loginWithKakaoAccount()
                    } else {
                        completeWithToken(token, null)
                    }
                }
            } else {
                loginWithKakaoAccount()
            }
        }
    }
}
