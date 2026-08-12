package com.phoneshim.android.ui.features.pref.viewmodel

import com.phoneshim.android.domain.model.Goal

internal fun Goal.toPrefSettings(fallback: PrefSettings): PrefSettings = PrefSettings(
    gender = gender.toGenderOrNull() ?: fallback.gender,
    ageGroup = ageGroup.toAgeGroupOrNull() ?: fallback.ageGroup,
    totalGoalMinutes = dailyGoalMinutes,
    isTotalLimitEnabled = blockAfterGoal,
    appGoals = apps.map { app ->
        AppGoal(
            id = app.packageName,
            packageName = app.packageName,
            appName = app.appName,
            goalMinutes = app.goalMinutes,
            isLimitEnabled = app.accessLimited,
            targetCount = app.targetCount,
            goalDescription = app.goalReason.orEmpty(),
        )
    },
    goalId = id,
)

internal fun PrefSettings.toGoal(): Goal = Goal(
    id = goalId,
    gender = gender.name,
    ageGroup = ageGroup.name,
    dailyGoalMinutes = totalGoalMinutes,
    blockAfterGoal = isTotalLimitEnabled,
    apps = appGoals.map { app ->
        com.phoneshim.android.domain.model.AppGoal(
            packageName = app.packageName,
            appName = app.appName,
            goalMinutes = app.goalMinutes,
            accessLimited = app.isLimitEnabled,
            targetCount = app.targetCount,
            goalReason = app.goalDescription.trim().ifEmpty { null },
        )
    },
)

private fun String?.toGenderOrNull(): Gender? =
    this?.let { value -> Gender.entries.firstOrNull { it.name == value } }

private fun String?.toAgeGroupOrNull(): AgeGroup? =
    this?.let { value -> AgeGroup.entries.firstOrNull { it.name == value } }
