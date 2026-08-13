package com.phoneshim.android.ui.features.mypage.viewmodel

import com.phoneshim.android.domain.model.User
import com.phoneshim.android.domain.model.UserStatus
import com.phoneshim.android.domain.model.WithdrawalResult
import com.phoneshim.android.ui.common.base.UiEffect
import com.phoneshim.android.ui.common.base.UiEvent
import com.phoneshim.android.ui.common.base.UiState

/**
 * 08. 마이페이지 화면군(MyScreen / MySideMenuScreen)의 MVI 계약.
 *
 * 화면은 상태를 직접 들고 있지 않고 [MyPageUiState] 만 그리며,
 * 사용자 입력은 전부 [MyPageUiEvent] 로 ViewModel 에 전달합니다.
 */
data class MyPageUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val isWithdrawPopupVisible: Boolean = false,

    /** 이름/다짐 문구 인라인 편집 중인지. */
    val isEditing: Boolean = false,
    val nameDraft: String = "",
    val motivationDraft: String = "",
    val nameError: String? = null,
    val motivationError: String? = null,
    val isSaving: Boolean = false,

    /** 탈퇴 요청이 접수돼 14일 유예 상태가 된 경우의 결과. */
    val withdrawal: WithdrawalResult? = null,

    /** 다짐 문구 안내 툴팁 노출 여부. 편집 모드에 들어가면 띄웁니다. */
    val isMotivationTooltipVisible: Boolean = false,
) : UiState {
    val name: String get() = user?.nickname.orEmpty()
    val email: String get() = user?.email.orEmpty()
    val motivation: String get() = user?.motivation.orEmpty()

    val isProfileReady: Boolean get() = user != null

    /** 탈퇴 유예 중이면 마이페이지 상단에 복구 안내를 노출합니다. */
    val isWithdrawalPending: Boolean
        // GET /api/users/me 응답에 status 가 없어 탈퇴를 방금 요청한 경우에만 알 수 있습니다.
        // TODO(백엔드): 프로필 응답에 status 를 포함해 주면 앱 재실행 후에도 배너를 띄울 수 있습니다.
        get() = withdrawal?.status == UserStatus.WITHDRAWAL_PENDING

    val withdrawalNoticeText: String?
        get() = if (!isWithdrawalPending) {
            null
        } else {
            val until = withdrawal?.recoverableUntil
            if (until.isNullOrBlank()) {
                "탈퇴가 접수되었습니다. ${WithdrawalResult.GRACE_PERIOD_DAYS}일 안에는 복구할 수 있습니다."
            } else {
                "탈퇴가 접수되었습니다. ${until.take(10)}까지 복구할 수 있습니다."
            }
        }

    val canSave: Boolean
        get() = isEditing && !isSaving && nameError == null && motivationError == null &&
            nameDraft.isNotBlank()
}

sealed interface MyPageUiEvent : UiEvent {
    /** 화면 진입 시 1회. 프로필 로딩 트리거. */
    data object ScreenEntered : MyPageUiEvent
    data object SideMenuClicked : MyPageUiEvent
    data object LogoutClicked : MyPageUiEvent
    data object ContactSupportClicked : MyPageUiEvent

    /** 이름/다짐 문구 편집. */
    data object EditClicked : MyPageUiEvent
    data object EditCancelled : MyPageUiEvent
    data class NameChanged(val value: String) : MyPageUiEvent
    data class MotivationChanged(val value: String) : MyPageUiEvent
    data object SaveClicked : MyPageUiEvent

    /** 사이드 메뉴에서 "회원 탈퇴" 를 눌러 확인 팝업을 여는 이벤트. */
    data object WithdrawMenuClicked : MyPageUiEvent
    data object WithdrawPopupDismissed : MyPageUiEvent

    /** 확인 팝업에서 최종 "탈퇴" 를 누른 이벤트. */
    data object WithdrawConfirmed : MyPageUiEvent
}

sealed interface MyPageUiEffect : UiEffect {
    data object NavigateToSideMenu : MyPageUiEffect
    data class NavigateToLogin(val noticeMessage: String) : MyPageUiEffect
    data object OpenContactSupport : MyPageUiEffect
    data class ShowMessage(val message: String) : MyPageUiEffect
}
