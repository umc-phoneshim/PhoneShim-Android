package com.phoneshim.android.ui.features.appblocking.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoneshim.android.domain.model.UsageReasonSubmission
import com.phoneshim.android.domain.repository.UsageReasonRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UsageReasonViewModel(
    private val repository: UsageReasonRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UsageReasonUiState())
    val uiState: StateFlow<UsageReasonUiState> = _uiState.asStateFlow()

    private val _effect = Channel<UsageReasonUiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun startSession(
        sessionId: String,
        packageName: String,
        appName: String,
    ) {
        require(sessionId.isNotBlank()) { "사용 이유 세션 ID가 비어 있습니다." }
        require(packageName.isNotBlank()) { "패키지 이름이 비어 있습니다." }

        val current = _uiState.value
        if (
            current.sessionId == sessionId &&
            current.packageName == packageName &&
            current.appName == appName
        ) {
            return
        }

        _uiState.value = UsageReasonUiState(
            sessionId = sessionId,
            packageName = packageName,
            appName = appName,
        )
    }

    fun selectReason(reason: String) {
        _uiState.update { state ->
            if (
                state.isSaving ||
                state.isCompleted ||
                reason !in state.reasons ||
                reason.length > MAX_REASON_LENGTH
            ) {
                state
            } else {
                state.copy(selectedReason = reason, errorMessage = null)
            }
        }
    }

    fun submitReason() {
        val submittedState = _uiState.value
        val reason = submittedState.selectedReason ?: return
        if (
            !submittedState.canSubmit ||
            submittedState.sessionId.isBlank() ||
            submittedState.packageName.isBlank()
        ) {
            return
        }

        val submission = UsageReasonSubmission(
            packageName = submittedState.packageName,
            reason = reason,
        )

        /*
         * 저장이 진행되는 동안 상태를 먼저 잠가 빠른 연속 탭이 Repository 중복 호출로
         * 이어지지 않게 한다. 완료 effect 역시 이 단일 요청의 성공 경로에서만 발생한다.
         */
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        viewModelScope.launch {
            repository.saveUsageReason(submission)
                .onSuccess {
                    /*
                     * 오버레이의 판정이 바뀌면 같은 ViewModelStore 안에서도 새 사용 이유
                     * 세션이 시작될 수 있다. 이전 요청의 늦은 응답이 새 화면을 닫지 않도록
                     * 요청 시작 당시 세션과 현재 세션이 같은 경우에만 결과를 반영한다.
                     */
                    if (_uiState.value.sessionId != submittedState.sessionId) return@onSuccess

                    _uiState.update { it.copy(isSaving = false, isCompleted = true) }

                    /*
                     * Repository 저장 성공을 세션 완료의 유일한 경계로 둔다. Content가
                     * 이 effect를 OverlayAction으로 변환한 뒤에만 서비스가 질문 완료를
                     * 기록하고 오버레이를 닫으므로 저장 실패가 성공처럼 처리되지 않는다.
                     */
                    _effect.send(
                        UsageReasonUiEffect.ReasonSubmitted(
                            sessionId = submittedState.sessionId,
                            submission = submission,
                        ),
                    )
                }
                .onFailure { error ->
                    if (_uiState.value.sessionId != submittedState.sessionId) return@onFailure

                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = error.message ?: DEFAULT_SAVE_ERROR,
                        )
                    }
                }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private companion object {
        const val MAX_REASON_LENGTH = 100
        const val DEFAULT_SAVE_ERROR = "사용 이유를 저장하지 못했습니다. 다시 시도해주세요."
    }
}
