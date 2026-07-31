package com.phoneshim.android.ui.features.setgoal.component

// 시/분 입력 상한. 04-2 총 목표 시간(ClockField)과 04-4 어플별 목표 시간(TimeField)이 공유합니다.
const val MAX_HOUR_VALUE = 23
const val MAX_MINUTE_VALUE = 59

// 시/분 입력 정제 규칙:
//  - 숫자만 남기고 2자리까지 받습니다.
//  - 다 지우면 빈 문자열로 둡니다. 지웠다가 다시 칠 수 있어야 하고, 표시할 때 "00"으로 폴백합니다.
//  - 상한을 넘는 입력은 직전 값을 유지합니다. 상한으로 바꿔치기하면 "95"를 치다가 값이
//    갑자기 "23"으로 바뀌어 오히려 혼란스럽기 때문에, 넘는 키 입력은 무시하는 쪽을 택했습니다.
fun sanitizeTimeInput(raw: String, current: String, maxValue: Int): String {
    val digits = raw.filter(Char::isDigit).take(2)
    if (digits.isEmpty()) return ""
    val parsed = digits.toIntOrNull() ?: return current
    return if (parsed > maxValue) current else digits
}
