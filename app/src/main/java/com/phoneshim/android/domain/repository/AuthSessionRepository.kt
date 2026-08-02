package com.phoneshim.android.domain.repository

interface AuthSessionRepository {
    suspend fun restoreSession(): Boolean
    suspend fun clearSession()
    fun hasSession(): Boolean
}
