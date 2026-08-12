package com.phoneshim.android.ui.features.auth.client

import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.phoneshim.android.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAuthClientImpl @Inject constructor(
    private val activityProvider: ForegroundActivityProvider,
) : GoogleAuthClient {
    override suspend fun authenticate(): AuthClientResult {
        val activity = activityProvider.requireActivity()
        if (!BuildConfig.GOOGLE_ID_TOKEN_LOGIN_ENABLED) {
            return AuthClientResult.Failure(
                IllegalStateException("Google ID token 로그인이 비활성화되어 있습니다."),
            )
        }
        if (BuildConfig.GOOGLE_WEB_CLIENT_ID.isBlank()) {
            return AuthClientResult.Failure(
                IllegalStateException("GOOGLE_WEB_CLIENT_ID가 설정되지 않았습니다."),
            )
        }

        return try {
            val googleIdOption = GetSignInWithGoogleOption.Builder(
                BuildConfig.GOOGLE_WEB_CLIENT_ID,
            )
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
                    providerToken = googleCredential.idToken,
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
