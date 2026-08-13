package com.phoneshim.android.domain.model

/**
 * Reminder 의 restrictedAppIds(서버 monitoredAppId 목록)를 차단 엔진이 쓰는
 * packageName 으로 바꾼 결과.
 *
 * [변환 못 한 id 를 함께 돌려주는 이유]
 *   packageName 목록만 주면 "제한 없음"과 "제한을 걸었는데 하나도 변환 못 함"이
 *   똑같이 빈 목록으로 보입니다. 차단 엔진은 foregroundPackage 가 목록에 있는지로만
 *   판정하므로 두 경우를 구분할 수 없고, 후자는 사용자가 제한을 걸어놨는데 아무것도
 *   막히지 않는 상태가 됩니다. 그것도 조용히.
 *
 *   그래서 변환하지 못한 id 를 [unresolvedIds] 로 함께 실어 보냅니다. 그 상황에서
 *   이전 정책을 유지할지, 안전하게 전체를 막을지, 그냥 넘어갈지는 차단 정책의
 *   문제이므로 엔진 쪽에서 정합니다.
 *
 * [언제 변환에 실패하나]
 *   - 서버에서 삭제된 주의 앱. 영구적이라 차단할 대상이 없습니다.
 *   - 캐시와 서버가 잠깐 어긋난 구간. 일시적이라 다음 조회에서 풀립니다.
 *   지금은 둘을 구분하지 않습니다. 서버 목록을 새로 받아왔는지 여부로만 갈리는데,
 *   구분이 필요해지면 [unresolvedIds] 를 세분화하면 됩니다.
 */
data class ResolvedRestrictedApps(
    val packageNames: List<String>,
    val unresolvedIds: List<String>,
) {
    /** 제한 대상이 있었는데 하나도 변환하지 못한 상태. 조용히 넘기면 안 되는 경우입니다. */
    val isFullyUnresolved: Boolean
        get() = packageNames.isEmpty() && unresolvedIds.isNotEmpty()
}
