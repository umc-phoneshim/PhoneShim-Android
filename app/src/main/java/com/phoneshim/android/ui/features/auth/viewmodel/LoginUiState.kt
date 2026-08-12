package com.phoneshim.android.ui.features.auth.viewmodel

import com.phoneshim.android.ui.common.base.UiState
import com.phoneshim.android.domain.model.SocialProvider

data class LoginUiState(
    val selectedProvider: SocialProvider? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isWithdrawalPending: Boolean = false,
    val canGoogleLogin: Boolean = true,
) : UiState
