package com.phoneshim.android.ui.features.setgoal.component

// 시/분 입력 상한. 04-2 총 목표 시간과 04-4 어플별 목표 시간 입력이 공유합니다.
//
// 입력 정제 자체는 공용 ui.common.sanitizeTimeSegment 가 담당합니다.
// 같은 규칙을 setgoal 에서 따로 구현하고 있었으나 공용 시간 입력 컴포넌트가
// 생기면서 일원화했고, 여기에는 setgoal 이 넘기는 상한값만 남깁니다.
const val MAX_HOUR_VALUE = 23
const val MAX_MINUTE_VALUE = 59
