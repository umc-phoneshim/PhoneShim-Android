package com.phoneshim.android.domain.model

/**
 * 서버에 등록된 '주의 앱' 한 개.
 *
 * [InstalledApp] 과의 차이: [InstalledApp] 은 기기에 깔린 앱(서버가 모르는 상태)이고,
 * 이 모델은 사용자가 주의 앱으로 등록해 서버가 [id] 를 발급한 상태입니다.
 *
 * [id] 는 서버 monitoredAppId(UUID)입니다. Reminder 의 restrictedAppIds, UsageLog 업로드가
 * 이 값을 쓰고, 차단 엔진은 [packageName] 을 씁니다. 둘 사이 변환은
 * [com.phoneshim.android.domain.repository.MonitoredAppRepository] 가 담당합니다.
 */
data class MonitoredApp(
    val id: String,
    val packageName: String,
    val appName: String,
    // 서버가 내려주는 아이콘 주소/식별값. Android 는 보통 PackageManager 에서 직접 아이콘을
    // 읽으므로 화면에서는 쓰지 않지만, 서버 계약을 그대로 들고 다니기 위해 유지합니다.
    val appIcon: String? = null,
)
