package com.phoneshim.android.domain.model

data class SocialIdentity(
    val provider: SocialProvider,
    val providerUserId: String,
    val email: String,
)
