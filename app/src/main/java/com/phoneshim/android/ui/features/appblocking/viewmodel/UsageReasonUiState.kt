package com.phoneshim.android.ui.features.appblocking.viewmodel

data class UsageReasonUiState(
    val sessionId: String = "",
    val packageName: String = "",
    val appName: String = "",
    val reasons: List<String> = listOf(
        "여가 시간",
        "이동 시간 중",
        "습관적으로",
        "정보를 얻기 위해",
        "기타",
    ),
    val selectedReason: String? = null,
    val isSaving: Boolean = false,
    val isCompleted: Boolean = false,
    val errorMessage: String? = null,
) {
    val canSubmit: Boolean
        get() = selectedReason != null && !isSaving && !isCompleted
}
