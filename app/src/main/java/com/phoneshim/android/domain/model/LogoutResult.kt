package com.phoneshim.android.domain.model

sealed interface LogoutResult {
    data object ServerConfirmed : LogoutResult
    data object LocalOnly : LogoutResult
}
