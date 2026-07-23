package com.phoneshim.android.domain.model

// 기기에 설치된(런처에 노출되는) 앱 한 개.
// 온보딩 주의 앱 선택에서 사용하며, 차단 엔진이 감지에 쓰는 packageName을 그대로 들고 다닙니다.
data class InstalledApp(
    val packageName: String,
    val label: String,
)
