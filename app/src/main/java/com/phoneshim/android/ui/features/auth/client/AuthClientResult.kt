package com.phoneshim.android.ui.features.auth.client

sealed interface AuthClientResult {
    data class Success(
        val providerAccessToken: String,
        val providerUserId: String? = null,
        val email: String? = null,
    ) : AuthClientResult

    data object Cancelled : AuthClientResult

    data class Failure(
        val cause: Throwable,
    ) : AuthClientResult
}
