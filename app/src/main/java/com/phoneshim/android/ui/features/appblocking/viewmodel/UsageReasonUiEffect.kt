package com.phoneshim.android.ui.features.appblocking.viewmodel

import com.phoneshim.android.domain.model.UsageReasonSubmission

sealed interface UsageReasonUiEffect {
    data class ReasonSubmitted(
        val sessionId: String,
        val submission: UsageReasonSubmission,
    ) : UsageReasonUiEffect
}
