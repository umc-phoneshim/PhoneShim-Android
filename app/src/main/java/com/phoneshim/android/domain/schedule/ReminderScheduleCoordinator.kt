package com.phoneshim.android.domain.schedule

/**
 * 리마인더 일정 제한의 예약·해제 공개 계약.
 *
 * 리마인더 Repository 가 차단 엔진 내부(AlarmManager, BlockerService)에 직접 의존하지 않도록
 * 두는 경계다. 구현은 엔진(`blocking/schedule`)에 있고, 호출자는 이 인터페이스만 안다.
 *
 * ## 호출 시점
 *
 * | 시점 | 호출 |
 * |---|---|
 * | 생성 성공 후 | [schedule] |
 * | 수정 성공 후 | [reschedule] |
 * | 삭제 성공 후 | [cancel] |
 * | 날짜별 목록 동기화 후 | [refreshToday] |
 *
 * 앱 재실행·기기 재부팅·자정 롤오버는 엔진이 자체적으로 [refreshToday] 를 부르므로
 * 호출자가 신경 쓰지 않아도 된다.
 *
 * ## 선행 조건
 *
 * 이 계약은 `reminder_restriction_cache` 를 원본으로 읽는다.
 * 호출 전에 그 캐시가 최신 상태여야 한다. 즉 CRUD 성공 후
 * **캐시를 갱신하고 나서** 이 인터페이스를 부른다.
 *
 * 캐시의 `restrictedPackages` 는 패키지명이다. 서버의 `restrictedAppIds`(monitoredApp UUID)를
 * 그대로 넣으면 안 된다. 엔진은 오프라인에서도 판정해야 해서 매 판정마다 UUID 를 변환할 수 없다.
 *
 * ## 멱등성
 *
 * 네 메서드 모두 같은 인자로 여러 번 불러도 결과가 같다.
 * 실패 시 예외를 던지지 않으므로 호출자가 try/catch 하지 않아도 된다.
 */
interface ReminderScheduleCoordinator {

    /**
     * 새로 만든 일정을 예약한다.
     *
     * 오늘이 아닌 날짜의 일정이면 지금 예약할 것이 없고, 그날 자정 롤오버 때 자동으로 예약된다.
     */
    suspend fun schedule(taskId: String)

    /**
     * 수정된 일정을 다시 예약한다. 기존 예약은 취소된다.
     *
     * 제한 모드가 `NONE` 으로 바뀌었거나 시각이 과거로 옮겨진 경우 예약만 취소되고 끝난다.
     */
    suspend fun reschedule(taskId: String)

    /**
     * 일정 예약을 취소한다.
     *
     * 그 일정 때문에 지금 제한이 걸려 있었다면 즉시 해제한다(명세 요구사항).
     * 다음 판정 주기를 기다리지 않는다.
     */
    suspend fun cancel(taskId: String)

    /**
     * 오늘 일정 전체를 다시 예약한다.
     *
     * 날짜별 목록을 서버와 동기화한 뒤처럼 여러 일정이 한꺼번에 바뀐 경우에 쓴다.
     * 이미 지난 시각은 예약하지 않는다.
     */
    suspend fun refreshToday()
}