package com.phoneshim.android.auth

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.phoneshim.android.BuildConfig
import com.phoneshim.android.ui.features.auth.social.SocialAuthResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleSocialAuthClient @Inject constructor() {
    suspend fun authenticate(activity: Activity): SocialAuthResult {
        if (BuildConfig.GOOGLE_WEB_CLIENT_ID.isBlank()) {
            return SocialAuthResult.Failure(
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
                // TODO: 서버의 accessToken 필드가 Google ID token을 허용하는지 확정해야 합니다.
                SocialAuthResult.Success(
                    providerAccessToken = googleCredential.idToken,
                    providerUserId = googleCredential.id,
                    email = googleCredential.id,
                )
            } else {
                SocialAuthResult.Failure(
                    IllegalStateException("지원하지 않는 Google 인증 결과입니다."),
                )
            }
        } catch (_: GetCredentialCancellationException) {
            SocialAuthResult.Cancelled
        } catch (error: Throwable) {
            SocialAuthResult.Failure(error)
        }
    }
}
