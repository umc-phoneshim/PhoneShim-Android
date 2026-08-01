package com.phoneshim.android.ui.features.auth.social

import android.app.Activity
import com.phoneshim.android.domain.model.SocialProvider

interface SocialAuthClient {
    suspend fun authenticate(
        activity: Activity,
        provider: SocialProvider,
    ): SocialAuthResult
}

sealed interface SocialAuthResult {
    data class Success(
        val providerAccessToken: String,
        val providerUserId: String? = null,
        val email: String? = null,
    ) : SocialAuthResult

    data object Cancelled : SocialAuthResult

    data class Failure(
        val cause: Throwable,
    ) : SocialAuthResult
}
