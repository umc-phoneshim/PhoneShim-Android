package com.phoneshim.android.ui.features.setgoal.viewmodel

import com.phoneshim.android.domain.model.AppGoal
import com.phoneshim.android.domain.model.Goal

// 온보딩 목표 설정 UI 상태 → 공통 도메인 Goal 변환.
// selectedApps가 InstalledApp(packageName+label)이라 실제 패키지명이 그대로 저장됩니다.
fun SetGoalUiState.toGoal(): Goal = Goal(
    gender = gender,
    ageGroup = ageGroup,
    dailyGoalMinutes = goalTime.totalMinutes,
    blockAfterGoal = blockAfterGoal,
    apps = selectedApps.map { app ->
        val setting = appSettings[app.packageName] ?: AppGoalSetting()
        AppGoal(
            packageName = app.packageName,
            appName = app.label,
            goalMinutes = setting.timeInput.totalMinutes,
            accessLimited = setting.accessLimited,
        )
    },
)
