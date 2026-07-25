package com.phoneshim.android.domain.model

data class UsageReasonSubmission(
    val packageName: String,
    val reason: String,
) {
    init {
        require(packageName.isNotBlank()) { "패키지 이름이 비어 있습니다." }
        require(reason.isNotBlank()) { "사용 이유가 비어 있습니다." }
    }
}
