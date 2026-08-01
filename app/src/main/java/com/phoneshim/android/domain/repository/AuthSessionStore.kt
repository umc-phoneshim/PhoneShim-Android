package com.phoneshim.android.domain.repository

interface AuthSessionStore {
    suspend fun restore(): Boolean
    suspend fun saveAccessToken(accessToken: String)
    suspend fun clear()
    fun hasSession(): Boolean
}
