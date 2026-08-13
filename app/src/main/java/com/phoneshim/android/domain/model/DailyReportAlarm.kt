package com.phoneshim.android.domain.model

/**
 * 데일리 리포트 알림 시각.
 *
 * 리포트 하루의 기준이 22:00 - 21:59 라서 이 시각은 "그 하루가 끝난 뒤 언제 알려줄지" 를 뜻합니다.
 * 서버에 알림 설정 API 가 아직 없어 기기 로컬(DataStore)에만 저장합니다.
 */
data class DailyReportAlarm(
    val hour: Int,
    val minute: Int,
) {
    /** 화면 표시용 "07" 같은 두 자리 문자열. */
    val hourLabel: String get() = "%02d".format(hour)
    val minuteLabel: String get() = "%02d".format(minute)

    companion object {
        val DEFAULT = DailyReportAlarm(hour = 0, minute = 0)

        /** 입력값이 범위를 벗어나면 기본값으로 떨어뜨립니다. */
        fun of(hour: Int, minute: Int): DailyReportAlarm =
            if (hour in 0..23 && minute in 0..59) DailyReportAlarm(hour, minute) else DEFAULT
    }
}
