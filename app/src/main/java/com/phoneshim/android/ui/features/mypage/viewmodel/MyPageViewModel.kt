package com.phoneshim.android.ui.features.mypage.viewmodel

import androidx.lifecycle.viewModelScope
import com.phoneshim.android.domain.usecase.GetGoalUseCase
import com.phoneshim.android.domain.usecase.GetMyInfoUseCase
import com.phoneshim.android.domain.model.LogoutResult
import com.phoneshim.android.domain.usecase.UpdateMyInfoUseCase
import com.phoneshim.android.domain.usecase.WithdrawUseCase
import com.phoneshim.android.domain.usecase.LogoutUseCase
import com.phoneshim.android.ui.common.PhoneShimSnackbarType
import com.phoneshim.android.ui.common.base.BaseViewModel
import com.phoneshim.android.ui.common.base.toSnackbarMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val getMyInfoUseCase: GetMyInfoUseCase,
    private val getGoalUseCase: GetGoalUseCase,
    private val updateMyInfoUseCase: UpdateMyInfoUseCase,
    private val withdrawUseCase: WithdrawUseCase,
    private val logoutUseCase: LogoutUseCase,
) : BaseViewModel<MyPageUiState, MyPageUiEvent, MyPageUiEffect>(MyPageUiState()) {

    override fun handleEvent(event: MyPageUiEvent) {
        when (event) {
            MyPageUiEvent.ScreenEntered -> loadProfile()
            MyPageUiEvent.SideMenuClicked -> sendEffect(MyPageUiEffect.NavigateToSideMenu)
            MyPageUiEvent.LogoutClicked -> logout()
            MyPageUiEvent.ContactSupportClicked -> sendEffect(MyPageUiEffect.OpenContactSupport)

            MyPageUiEvent.EditClicked -> startEditing()
            MyPageUiEvent.EditCancelled -> cancelEditing()
            is MyPageUiEvent.NameChanged -> changeName(event)
            is MyPageUiEvent.MotivationChanged -> changeMotivation(event)
            MyPageUiEvent.SaveClicked -> save()

            MyPageUiEvent.WithdrawMenuClicked -> setState { copy(isWithdrawPopupVisible = true) }
            MyPageUiEvent.WithdrawPopupDismissed -> setState { copy(isWithdrawPopupVisible = false) }
            MyPageUiEvent.WithdrawConfirmed -> withdraw()
        }
    }

    /** GET /api/users/me. 이미 로드했거나 로딩 중이면 건너뜁니다. */
    private fun loadProfile() {
        if (currentState.isProfileReady || currentState.isLoading) return
        setState { copy(isLoading = true) }
        viewModelScope.launch {
            getMyInfoUseCase()
                .onSuccess { user -> setState { copy(user = user, isLoading = false) } }
                .onFailure { throwable ->
                    handleError(throwable) { error ->
                        setState { copy(isLoading = false) }
                        sendEffect(MyPageUiEffect.ShowMessage(error.toSnackbarMessage()))
                    }
                }
        }
        loadGoal()
    }

    /**
     * 마이페이지 "목표" 카드용 요약.
     *
     * 프로필과 별개로 실패해도 화면 전체를 막지 않습니다.
     * 목표는 없을 수도 있는 값이라(온보딩 전) 실패 시 안내 문구만 카드에 남깁니다.
     */
    private fun loadGoal() {
        viewModelScope.launch {
            getGoalUseCase()
                .onSuccess { goal -> setState { copy(goal = goal) } }
                .onFailure { setState { copy(goal = null) } }
        }
    }

    private fun startEditing() = setState {
        copy(
            isEditing = true,
            nameDraft = name,
            motivationDraft = motivation,
            nameError = null,
            motivationError = null,
        )
    }

    private fun cancelEditing() = setState {
        copy(isEditing = false, nameDraft = "", motivationDraft = "", nameError = null, motivationError = null)
    }

    private fun changeName(event: MyPageUiEvent.NameChanged) = setState {
        copy(
            nameDraft = event.value,
            nameError = if (event.value.isBlank()) "이름을 입력해 주세요." else null,
        )
    }

    private fun changeMotivation(event: MyPageUiEvent.MotivationChanged) = setState {
        val limit = UpdateMyInfoUseCase.MAX_MOTIVATION_LENGTH
        copy(
            motivationDraft = event.value,
            motivationError = if (event.value.length > limit) "${limit}자 이내로 입력해 주세요." else null,
        )
    }

    /** PATCH /api/users/me. 서버 구현이 "예정" 상태라 실패할 수 있습니다. */
    private fun save() {
        val state = currentState
        if (!state.canSave) return
        setState { copy(isSaving = true) }
        viewModelScope.launch {
            updateMyInfoUseCase(name = state.nameDraft, motivation = state.motivationDraft)
                .onSuccess { user ->
                    setState { copy(user = user, isEditing = false, isSaving = false) }
                    sendEffect(
                        MyPageUiEffect.ShowMessage(
                            message = "저장했습니다.",
                            type = PhoneShimSnackbarType.Info,
                        ),
                    )
                }
                .onFailure { throwable ->
                    handleError(throwable) { error ->
                        setState { copy(isSaving = false) }
                        sendEffect(MyPageUiEffect.ShowMessage(error.toSnackbarMessage()))
                    }
                }
        }
    }

    private fun logout() {
        if (currentState.isSaving) return
        setState { copy(isSaving = true) }
        viewModelScope.launch {
            logoutUseCase()
                .onSuccess { result ->
                    setState { copy(isSaving = false) }
                    sendEffect(
                        MyPageUiEffect.NavigateToLogin(
                            when (result) {
                                LogoutResult.ServerConfirmed -> "로그아웃되었습니다."
                                LogoutResult.LocalOnly ->
                                    "서버 연결을 확인하지 못했지만 이 기기의 세션은 종료했습니다."
                            },
                        ),
                    )
                }
                .onFailure { throwable ->
                    handleError(throwable) { error ->
                        setState { copy(isSaving = false) }
                        sendEffect(MyPageUiEffect.ShowMessage(error.toSnackbarMessage()))
                    }
                }
        }
    }

    /**
     * DELETE /api/auth/withdraw.
     * 즉시 삭제가 아니라 14일 유예(WITHDRAWAL_PENDING) 상태로 전환됩니다.
     */
    private fun withdraw() {
        if (currentState.isSaving) return
        setState { copy(isWithdrawPopupVisible = false, isSaving = true) }
        viewModelScope.launch {
            withdrawUseCase()
                .onSuccess { result ->
                    setState { copy(isSaving = false, withdrawal = result) }
                    sendEffect(
                        MyPageUiEffect.NavigateToLogin(
                            "탈퇴 요청이 접수되었습니다. 14일 이내에는 계정을 복구할 수 있습니다.",
                        ),
                    )
                }
                .onFailure { throwable ->
                    handleError(throwable) { error ->
                        setState { copy(isSaving = false) }
                        sendEffect(MyPageUiEffect.ShowMessage(error.toSnackbarMessage()))
                    }
                }
        }
    }
}

