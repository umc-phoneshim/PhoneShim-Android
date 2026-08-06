package com.phoneshim.android.ui.features.auth.client

sealed interface AuthClientResult {
    data class Success(
        /** 서버 JWT 교환 직후 폐기해야 하며 로그나 영속 저장소에 남기지 않는다. */
        val providerToken: String,
        val providerUserId: String? = null,
        val email: String? = null,
    ) : AuthClientResult

    data object Cancelled : AuthClientResult

    data class Failure(
        val cause: Throwable,
    ) : AuthClientResult
}
