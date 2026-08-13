package com.phoneshim.android.ui.features.pref.viewmodel

import com.phoneshim.android.domain.model.Goal
import com.phoneshim.android.domain.model.User

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

internal fun PrefSettings.withUserProfile(user: User): PrefSettings = copy(
    gender = user.gender.toGenderOrNull() ?: gender,
    ageGroup = user.ageGroup.toAgeGroupOrNull() ?: ageGroup,
)

internal fun Gender.toServerValue(): String = name

internal fun AgeGroup.toServerValue(): String = when (this) {
    AgeGroup.FIFTIES_OR_MORE -> "FIFTIES_PLUS"
    else -> name
}

private fun String?.toGenderOrNull(): Gender? =
    this?.let { value -> Gender.entries.firstOrNull { it.name == value } }

private fun String?.toAgeGroupOrNull(): AgeGroup? =
    when (this) {
        "FIFTIES_PLUS", "FIFTIES_OR_MORE" -> AgeGroup.FIFTIES_OR_MORE
        else -> this?.let { value -> AgeGroup.entries.firstOrNull { it.name == value } }
    }
