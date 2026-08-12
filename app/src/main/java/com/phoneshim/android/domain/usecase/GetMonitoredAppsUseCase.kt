package com.phoneshim.android.domain.usecase

import com.phoneshim.android.domain.model.MonitoredApp
import com.phoneshim.android.domain.repository.MonitoredAppRepository
import javax.inject.Inject

/**
 * 등록된 주의 앱 목록 조회. 서버 우선, 실패하면 로컬 캐시로 답합니다.
 * 주의 앱 선택 UI 와 설정(PREF) 화면이 이 UseCase 로 목록을 받습니다.
 */
class GetMonitoredAppsUseCase @Inject constructor(
    private val monitoredAppRepository: MonitoredAppRepository,
) {
    suspend operator fun invoke(): Result<List<MonitoredApp>> =
        monitoredAppRepository.getMonitoredApps()
}
