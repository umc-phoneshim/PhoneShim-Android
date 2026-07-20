package com.phoneshim.android.ui.common

import androidx.annotation.DrawableRes
import com.phoneshim.android.R

enum class BottomBarTab(val label: String, @DrawableRes val iconRes: Int) {
    MAIN("메인", R.drawable.ic_main),
    REMINDER("리마인더", R.drawable.ic_reminder),
    REPORT("리포트", R.drawable.ic_report),
}
