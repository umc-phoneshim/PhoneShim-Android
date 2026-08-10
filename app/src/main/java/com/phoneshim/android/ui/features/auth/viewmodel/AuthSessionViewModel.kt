package com.phoneshim.android.ui.features.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoneshim.android.domain.usecase.ClearAuthSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AuthSessionEffect {
    data object NavigateToLogin : AuthSessionEffect
}

@HiltViewModel
class AuthSessionViewModel @Inject constructor(
    private val clearAuthSessionUseCase: ClearAuthSessionUseCase,
) : ViewModel() {
    private val _effect = Channel<AuthSessionEffect>(Channel.BUFFERED)
    val effect: Flow<AuthSessionEffect> = _effect.receiveAsFlow()
    private val _noticeMessage = MutableStateFlow<String?>(null)
    val noticeMessage: StateFlow<String?> = _noticeMessage.asStateFlow()

    private var isExpirationHandled = false

    fun onAuthExpired() {
        // 여러 화면의 동시 401이 세션 삭제와 로그인 이동을 중복 실행하지 않도록 현재 세션에서 한 번만 처리한다.
        if (isExpirationHandled) return
        isExpirationHandled = true
        _noticeMessage.value = null

        viewModelScope.launch {
            try {
                clearAuthSessionUseCase()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Throwable) {
                // 저장소 오류가 있더라도 만료된 세션 화면에 사용자를 남겨두지 않습니다.
            }
            _effect.send(AuthSessionEffect.NavigateToLogin)
        }
    }

    fun onSessionEnded(noticeMessage: String) {
        _noticeMessage.value = noticeMessage
        viewModelScope.launch { _effect.send(AuthSessionEffect.NavigateToLogin) }
    }

    fun onSessionStarted() {
        // 재로그인 이후 발생하는 401은 새로운 세션의 만료이므로 다시 처리할 수 있어야 한다.
        isExpirationHandled = false
        _noticeMessage.value = null
    }
}
