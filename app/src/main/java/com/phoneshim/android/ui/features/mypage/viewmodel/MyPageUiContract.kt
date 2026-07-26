package com.phoneshim.android.ui.features.mypage.viewmodel

import com.phoneshim.android.domain.model.User
import com.phoneshim.android.ui.common.base.UiEffect
import com.phoneshim.android.ui.common.base.UiEvent
import com.phoneshim.android.ui.common.base.UiState

/**
 * 08. 마이페이지 화면군(MyScreen / MySideMenuScreen)의 MVI 계약.
 *
 * 화면은 상태를 직접 들고 있지 않고 [MyPageUiState] 만 그려주며,
 * 사용자 입력은 전부 [MyPageUiEvent] 로 ViewModel 에 전달합니다.
 */
data class MyPageUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    /** 사이드 메뉴의 "회원 탈퇴" 확인 팝업 노출 여부. */
    val isWithdrawPopupVisible: Boolean = false,
) : UiState {
    val nickname: String get() = user?.nickname.orEmpty()
    val email: String get() = user?.email.orEmpty()

    /** 프로필 영역을 placeholder 로 둘지 판단하기 위한 값. */
    val isProfileReady: Boolean get() = user != null
}

sealed interface MyPageUiEvent : UiEvent {
    /** 화면 진입 시 1회. 프로필 로딩 트리거. */
    data object ScreenEntered : MyPageUiEvent
    data object SideMenuClicked : MyPageUiEvent
    data object LogoutClicked : MyPageUiEvent
    data object ContactSupportClicked : MyPageUiEvent

    /** 사이드 메뉴에서 "회원 탈퇴" 를 눌러 확인 팝업을 여는 이벤트. */
    data object WithdrawMenuClicked : MyPageUiEvent
    data object WithdrawPopupDismissed : MyPageUiEvent

    /** 확인 팝업에서 최종 "탈퇴" 를 누른 이벤트. */
    data object WithdrawConfirmed : MyPageUiEvent
}

sealed interface MyPageUiEffect : UiEffect {
    data object NavigateToSideMenu : MyPageUiEffect
    data object NavigateToWithdraw : MyPageUiEffect
    data object NavigateToLogin : MyPageUiEffect
    data object OpenContactSupport : MyPageUiEffect
    data class ShowMessage(val message: String) : MyPageUiEffect
}
