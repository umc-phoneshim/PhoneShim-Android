package com.phoneshim.android.domain.repository

import com.phoneshim.android.domain.model.InstalledApp

interface InstalledAppsRepository {
    // 런처에 노출되는 설치 앱 목록 (라벨 오름차순). 폰쉼 자기 자신은 제외합니다.
    suspend fun getInstalledApps(): List<InstalledApp>
}
