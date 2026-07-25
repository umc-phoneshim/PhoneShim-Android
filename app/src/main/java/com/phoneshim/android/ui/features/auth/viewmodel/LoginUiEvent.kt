package com.phoneshim.android.ui.features.auth.viewmodel

import com.phoneshim.android.ui.common.base.UiEvent

sealed interface LoginUiEvent : UiEvent {
    data object GoogleLoginClicked : LoginUiEvent
    data object KakaoLoginClicked : LoginUiEvent
    data object ErrorDismissed : LoginUiEvent
}
