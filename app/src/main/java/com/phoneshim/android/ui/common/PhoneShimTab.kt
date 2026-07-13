package com.phoneshim.android.ui.common

import androidx.annotation.DrawableRes
import com.phoneshim.android.R

// 상단 바 배리언트와 하단 바 탭이 1:1 대응되는 공용 탭 정의
enum class PhoneShimTab(val label: String, @DrawableRes val iconRes: Int) {
    MAIN("메인", R.drawable.main),
    REMINDER("리마인더", R.drawable.reminder),
    REPORT("리포트", R.drawable.report)
}
