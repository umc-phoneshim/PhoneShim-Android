package com.phoneshim.android.ui.features.auth.client

import android.app.Activity
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.phoneshim.android.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class KakaoAuthClientImpl @Inject constructor(
    private val activityProvider: ForegroundActivityProvider,
) : KakaoAuthClient {
    override suspend fun authenticate(): AuthClientResult {
        val activity = activityProvider.requireActivity()
        if (BuildConfig.KAKAO_NATIVE_APP_KEY.isBlank()) {
            return AuthClientResult.Failure(
                IllegalStateException("KAKAO_NATIVE_APP_KEY가 설정되지 않았습니다."),
            )
        }

        return suspendCoroutine { continuation ->
            fun completeWithToken(token: OAuthToken?, error: Throwable?) {
                if (error != null || token == null) {
                    continuation.resume(
                        AuthClientResult.Failure(
                            error ?: IllegalStateException("Kakao access token이 없습니다."),
                        ),
                    )
                    return
                }

                UserApiClient.instance.me { user, _ ->
                    continuation.resume(
                        AuthClientResult.Success(
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
                        continuation.resume(AuthClientResult.Cancelled)
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
