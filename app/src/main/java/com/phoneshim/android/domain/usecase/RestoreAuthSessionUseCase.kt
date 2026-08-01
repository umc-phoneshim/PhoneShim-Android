package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.repository.AuthSessionStore
import javax.inject.Inject

class RestoreAuthSessionUseCase @Inject constructor(
    private val authSessionStore: AuthSessionStore,
) {
    suspend operator fun invoke(): Boolean = authSessionStore.restore()
}
