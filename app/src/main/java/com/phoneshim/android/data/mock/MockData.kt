package com.phoneshim.android.data.mock

import com.phoneshim.android.domain.model.AppUsage

object MockData {
    val todayUsage = listOf(
        AppUsage("com.kakao.talk", "카카오톡", 62),
        AppUsage("com.google.android.youtube", "YouTube", 130),
        AppUsage("com.instagram.android", "Instagram", 45),
    )
    // 다른 화면 mock은 각 화면 담당자가 여기에 추가
}