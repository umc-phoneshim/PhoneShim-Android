package com.phoneshim.android.domain.repository

import com.phoneshim.android.domain.model.AuthSessionState
import kotlinx.coroutines.flow.StateFlow

interface AuthSessionRepository {
    val sessionState: StateFlow<AuthSessionState>

    suspend fun restoreSession(): Boolean
    suspend fun clearSession()
    fun hasSession(): Boolean
}
