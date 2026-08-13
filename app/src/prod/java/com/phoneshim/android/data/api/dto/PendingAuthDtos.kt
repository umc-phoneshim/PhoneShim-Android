package com.phoneshim.android.data.api.dto

data class ProviderTokenRequest(
    val provider: String,
    val idToken: String? = null,
    val accessToken: String? = null,
)

data class RecoverWithdrawalResponseDto(
    val accessToken: String,
)

data class LinkedAccountResponseDto(
    val id: String,
    val provider: String,
    val providerUserId: String,
    val email: String? = null,
)
