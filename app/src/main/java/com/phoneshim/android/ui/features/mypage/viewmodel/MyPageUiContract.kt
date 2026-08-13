package com.phoneshim.android.ui.features.mypage.viewmodel

import com.phoneshim.android.domain.model.AppGoal
import com.phoneshim.android.domain.model.Goal
import com.phoneshim.android.domain.model.User
import com.phoneshim.android.domain.model.UserStatus
import com.phoneshim.android.domain.model.WithdrawalResult
import com.phoneshim.android.ui.common.PhoneShimSnackbarType
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

    /** 마이페이지 "목표" 카드에 보여줄 요약. 아직 목표를 세우지 않았으면 null. */
    val goal: Goal? = null,
) : UiState {
    val name: String get() = user?.nickname.orEmpty()
    val email: String get() = user?.email.orEmpty()
    val motivation: String get() = user?.motivation.orEmpty()

    val hasGoal: Boolean get() = goal != null && goal.dailyGoalMinutes > 0

    /** 하루 총 목표. 예) "3시간 30분" */
    val dailyGoalLabel: String
        get() {
            val minutes = goal?.dailyGoalMinutes ?: return ""
            val hours = minutes / MINUTES_PER_HOUR
            val remainder = minutes % MINUTES_PER_HOUR
            return when {
                hours > 0 && remainder > 0 -> "${hours}시간 ${remainder}분"
                hours > 0 -> "${hours}시간"
                else -> "${remainder}분"
            }
        }

    /** 목표를 세운 주의 앱 목록. 카드가 길어지지 않게 앞의 몇 개만 보여줍니다. */
    val goalApps: List<AppGoal> get() = goal?.apps.orEmpty().take(MAX_GOAL_APPS)

    val hiddenGoalAppCount: Int
        get() = (goal?.apps.orEmpty().size - MAX_GOAL_APPS).coerceAtLeast(0)

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

    private companion object {
        const val MINUTES_PER_HOUR = 60
        const val MAX_GOAL_APPS = 3
    }
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
    data class ShowMessage(
        val message: String,
        val type: PhoneShimSnackbarType = PhoneShimSnackbarType.Error,
    ) : MyPageUiEffect
}
