package com.phoneshim.android.ui.features.auth.client

interface KakaoAuthClient {
    suspend fun authenticate(): AuthClientResult
}
