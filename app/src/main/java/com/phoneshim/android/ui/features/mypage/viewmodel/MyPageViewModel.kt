package com.phoneshim.android.ui.features.mypage.viewmodel

import androidx.lifecycle.viewModelScope
import com.phoneshim.android.data.api.common.ApiException
import com.phoneshim.android.domain.usecase.GetMyInfoUseCase
import com.phoneshim.android.domain.usecase.UpdateMyInfoUseCase
import com.phoneshim.android.domain.usecase.WithdrawUseCase
import com.phoneshim.android.ui.common.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val getMyInfoUseCase: GetMyInfoUseCase,
    private val updateMyInfoUseCase: UpdateMyInfoUseCase,
    private val withdrawUseCase: WithdrawUseCase,
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
                    setState { copy(isLoading = false) }
                    sendEffect(MyPageUiEffect.ShowMessage(throwable.toUserMessage("프로필을 불러오지 못했습니다.")))
                }
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
                    sendEffect(MyPageUiEffect.ShowMessage("저장했습니다."))
                }
                .onFailure { throwable ->
                    setState { copy(isSaving = false) }
                    sendEffect(MyPageUiEffect.ShowMessage(throwable.toUserMessage("저장하지 못했습니다.")))
                }
        }
    }

    private fun logout() {
        // TODO(Auth 담당): POST /api/auth/logout 이 서버에 구현되면(현재 "예정")
        //  LogoutUseCase 를 만들어 호출하고 저장된 토큰도 함께 비워 주세요.
        sendEffect(MyPageUiEffect.NavigateToLogin)
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
                    sendEffect(MyPageUiEffect.NavigateToWithdraw)
                }
                .onFailure { throwable ->
                    setState { copy(isSaving = false) }
                    sendEffect(MyPageUiEffect.ShowMessage(throwable.toUserMessage("탈퇴 처리에 실패했습니다.")))
                }
        }
    }
}

/** 서버가 준 메시지가 있으면 그대로 쓰고, 없으면 기본 문구를 사용합니다. */
private fun Throwable.toUserMessage(fallback: String): String = when (this) {
    is ApiException -> if (isUnauthorized) {
        "다시 로그인해 주세요."
    } else {
        message?.takeIf { it.isNotBlank() } ?: fallback
    }
    is IllegalArgumentException -> message ?: fallback
    else -> fallback
}
