package com.phoneshim.android.ui.features.auth.viewmodel

import com.phoneshim.android.ui.common.base.UiEvent
import com.phoneshim.android.domain.model.SocialProvider

sealed interface LoginUiEvent : UiEvent {
    data class LoginClicked(val provider: SocialProvider) : LoginUiEvent
    data object ErrorDismissed : LoginUiEvent
}
