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
}
