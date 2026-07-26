package com.phoneshim.android.ui.features.mypage.viewmodel

import com.phoneshim.android.domain.usecase.WithdrawUseCase
import com.phoneshim.android.ui.common.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val withdrawUseCase: WithdrawUseCase,
) : BaseViewModel<MyPageUiState, MyPageUiEvent, MyPageUiEffect>(MyPageUiState()) {

    override fun handleEvent(event: MyPageUiEvent) {
        when (event) {
            MyPageUiEvent.ScreenEntered -> loadProfile()
            MyPageUiEvent.SideMenuClicked -> sendEffect(MyPageUiEffect.NavigateToSideMenu)
            MyPageUiEvent.LogoutClicked -> logout()
            MyPageUiEvent.ContactSupportClicked -> sendEffect(MyPageUiEffect.OpenContactSupport)
            MyPageUiEvent.WithdrawMenuClicked -> setState { copy(isWithdrawPopupVisible = true) }
            MyPageUiEvent.WithdrawPopupDismissed -> setState { copy(isWithdrawPopupVisible = false) }
            MyPageUiEvent.WithdrawConfirmed -> withdraw()
        }
    }

    private fun loadProfile() {
        if (currentState.isProfileReady || currentState.isLoading) return
        // TODO: GetMyProfileUseCase 가 추가되면 호출해 uiState.user 를 채우세요.
        //  로딩 구간에는 isLoading = true 로 두고 실패 시 ShowMessage 이펙트를 보냅니다.
    }

    private fun logout() {
        // TODO: LogoutUseCase 추가 후 토큰 삭제 → NavigateToLogin 이펙트 발행으로 교체하세요.
        sendEffect(MyPageUiEffect.NavigateToLogin)
    }

    private fun withdraw() {
        setState { copy(isWithdrawPopupVisible = false) }
        // TODO: viewModelScope 에서 withdrawUseCase() 를 호출하고
        //  성공 시 NavigateToWithdraw, 실패 시 ShowMessage 이펙트를 발행하도록 교체하세요.
        sendEffect(MyPageUiEffect.NavigateToWithdraw)
    }
}
