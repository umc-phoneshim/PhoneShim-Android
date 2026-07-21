package com.phoneshim.android.ui.features.auth.viewmodel

import com.phoneshim.android.ui.common.base.UiState

enum class LoginProvider {
    GOOGLE,
    KAKAO,
}

data class LoginUiState(
    val selectedProvider: LoginProvider? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) : UiState
