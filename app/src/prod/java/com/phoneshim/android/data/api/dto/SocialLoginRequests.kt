package com.phoneshim.android.data.api.dto

data class GoogleLoginRequest(
    val idToken: String,
)

data class KakaoLoginRequest(
    val accessToken: String,
)
