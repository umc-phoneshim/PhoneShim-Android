package com.phoneshim.android.ui.features.auth.client

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.phoneshim.android.BuildConfig
import com.phoneshim.android.domain.model.AuthException
import com.phoneshim.android.domain.model.PendingAuthFeature
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAuthClientImpl @Inject constructor(
    private val activityProvider: ForegroundActivityProvider,
) : GoogleAuthClient {
    override suspend fun authenticate(): AuthClientResult {
        val activity = activityProvider.requireActivity()
        // 현재 서버의 accessToken 필드가 Google ID token을 검증하는 계약으로 확정되기 전에는 실행하지 않는다.
        if (!BuildConfig.GOOGLE_ID_TOKEN_LOGIN_ENABLED) {
            return AuthClientResult.Failure(
                AuthException.FeatureUnavailable(PendingAuthFeature.GOOGLE_LOGIN_TOKEN_CONTRACT),
            )
        }
        if (BuildConfig.GOOGLE_WEB_CLIENT_ID.isBlank()) {
            return AuthClientResult.Failure(
                IllegalStateException("GOOGLE_WEB_CLIENT_ID가 설정되지 않았습니다."),
            )
        }

        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .setAutoSelectEnabled(false)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            val credential = CredentialManager.create(activity)
                .getCredential(context = activity, request = request)
                .credential

            if (
                credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                AuthClientResult.Success(
                    // 공통 모델의 이름은 providerAccessToken이지만 Google에서는 OIDC ID token을 전달한다.
                    providerAccessToken = googleCredential.idToken,
                    // TODO: 서버가 ID token의 sub/email claim을 검증하도록 계약 변경 후 식별 정보 직접 전달을 제거한다.
                    providerUserId = googleCredential.id,
                    email = googleCredential.id,
                )
            } else {
                AuthClientResult.Failure(
                    IllegalStateException("지원하지 않는 Google 인증 결과입니다."),
                )
            }
        } catch (_: GetCredentialCancellationException) {
            AuthClientResult.Cancelled
        } catch (error: Throwable) {
            AuthClientResult.Failure(error)
        }
    }
}
