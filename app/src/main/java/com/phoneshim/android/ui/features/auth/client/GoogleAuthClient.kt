package com.phoneshim.android.ui.features.auth.client

interface GoogleAuthClient {
    suspend fun authenticate(): AuthClientResult
}
