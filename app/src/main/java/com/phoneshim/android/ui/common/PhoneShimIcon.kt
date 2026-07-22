package com.phoneshim.android.ui.common

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.phoneshim.android.R

enum class PhoneShimIconType(@DrawableRes val drawableRes: Int) {
    Target(R.drawable.ic_goal),
    Person(R.drawable.ic_my),
    Home(R.drawable.ic_main),
    Clock(R.drawable.ic_target_time),
    Document(R.drawable.ic_report),
    Info(R.drawable.ic_question_fill),
    Bell(R.drawable.ic_target_alarm),
    ChevronRight(R.drawable.ic_chevron_right),
    ChevronLeft(R.drawable.ic_chevron_left),
}

@Composable
fun PhoneShimIcon(
    type: PhoneShimIconType,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
) {
    Icon(
        painter = painterResource(type.drawableRes),
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint,
    )
}
