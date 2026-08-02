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
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.atomic.AtomicBoolean

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

        return suspendCancellableCoroutine { continuation ->
            // Kakao 로그인과 사용자 조회 callback이 경쟁해도 coroutine은 정확히 한 번만 완료한다.
            val completed = AtomicBoolean(false)

            fun resumeOnce(result: AuthClientResult) {
                if (completed.compareAndSet(false, true) && continuation.isActive) {
                    continuation.resume(result)
                }
            }

            fun completeWithToken(token: OAuthToken?, error: Throwable?) {
                if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                    resumeOnce(AuthClientResult.Cancelled)
                    return
                }
                if (error != null || token == null) {
                    resumeOnce(
                        AuthClientResult.Failure(
                            error ?: IllegalStateException("Kakao access token이 없습니다."),
                        ),
                    )
                    return
                }

                UserApiClient.instance.me { user, userError ->
                    if (userError != null || user == null) {
                        resumeOnce(
                            AuthClientResult.Failure(
                                userError ?: IllegalStateException("Kakao 사용자 정보가 없습니다."),
                            ),
                        )
                    } else {
                        resumeOnce(
                            AuthClientResult.Success(
                                providerAccessToken = token.accessToken,
                                providerUserId = user.id?.toString(),
                                email = user.kakaoAccount?.email,
                            ),
                        )
                    }
                }
            }

            fun loginWithKakaoAccount() {
                UserApiClient.instance.loginWithKakaoAccount(activity, callback = ::completeWithToken)
            }

            if (UserApiClient.instance.isKakaoTalkLoginAvailable(activity)) {
                UserApiClient.instance.loginWithKakaoTalk(activity) { token, error ->
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        resumeOnce(AuthClientResult.Cancelled)
                    } else if (error != null) {
                        // 명시적인 사용자 취소는 fallback하지 않고, 카카오톡 실행 실패만 카카오계정으로 대체한다.
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
