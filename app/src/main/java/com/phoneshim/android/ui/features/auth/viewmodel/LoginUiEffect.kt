package com.phoneshim.android.ui.features.auth.viewmodel

import com.phoneshim.android.ui.common.base.UiEffect

sealed interface LoginUiEffect : UiEffect {
    data object NavigateToGoalSetup : LoginUiEffect
    data class ShowSnackbar(val message: String) : LoginUiEffect
}
