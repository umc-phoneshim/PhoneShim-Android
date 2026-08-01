package com.phoneshim.android.data.demo

import com.phoneshim.android.data.database.dao.GoalDao
import com.phoneshim.android.data.database.dao.UserProfileDao
import com.phoneshim.android.data.database.entity.AppGoalEntity
import com.phoneshim.android.data.database.entity.PhoneGoalEntity
import com.phoneshim.android.data.database.entity.UserProfileEntity
import com.phoneshim.android.domain.model.AppGoal
import com.phoneshim.android.domain.model.AppUsage
import com.phoneshim.android.domain.model.DailyReport
import com.phoneshim.android.domain.model.Goal
import com.phoneshim.android.domain.model.ReasonCalendarDay
import com.phoneshim.android.domain.model.ReasonKeyword
import com.phoneshim.android.domain.model.ReportAppUsage
import com.phoneshim.android.domain.model.ReportRange
import com.phoneshim.android.domain.model.ReportSummary
import com.phoneshim.android.domain.model.RestSuggestion
import com.phoneshim.android.domain.model.UsageReasonEntry
import com.phoneshim.android.domain.model.User
import com.phoneshim.android.domain.model.UserStatus
import com.phoneshim.android.domain.model.WithdrawalResult
import com.phoneshim.android.domain.repository.MainRepository
import com.phoneshim.android.domain.repository.GoalRepository
import com.phoneshim.android.domain.repository.MyPageRepository
import com.phoneshim.android.domain.repository.ReportRepository
import com.phoneshim.android.domain.repository.ReportUsageReasonRepository
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DemoGoalRepository @Inject constructor(
    private val goalDao: GoalDao,
    private val userProfileDao: UserProfileDao,
) : GoalRepository {
    override suspend fun getGoal(): Result<Goal?> = Result.success(
        goalDao.getPhoneGoal()?.let { phone ->
            val profile = userProfileDao.getProfile()
            Goal(
                gender = profile?.gender,
                ageGroup = profile?.ageGroup,
                dailyGoalMinutes = phone.goalMinutes,
                blockAfterGoal = phone.limitEnabled,
                apps = goalDao.getAppGoals().map { app ->
                    AppGoal(app.packageName, app.appLabel, app.goalMinutes, app.limitEnabled)
                },
            )
        },
    )

    override suspend fun saveGoal(goal: Goal): Result<Unit> = runCatching {
        goalDao.upsertPhoneGoal(PhoneGoalEntity(goalMinutes = goal.dailyGoalMinutes, limitEnabled = goal.blockAfterGoal))
        goalDao.clearAppGoals()
        goalDao.upsertAppGoals(
            goal.apps.map { app ->
                AppGoalEntity(app.packageName, app.appName, app.goalMinutes, app.accessLimited)
            },
        )
        userProfileDao.upsertProfile(UserProfileEntity(gender = goal.gender, ageGroup = goal.ageGroup))
    }
}

@Singleton
class DemoMainRepository @Inject constructor(
    private val goalDao: GoalDao,
) : MainRepository {
    override suspend fun getTodayUsage(): Result<List<AppUsage>> = Result.success(
        goalDao.getAppGoals().mapIndexed { index, goal ->
            AppUsage(goal.packageName, goal.appLabel, listOf(41, 28, 72, 19, 34)[index % 5])
        }.ifEmpty {
            listOf(
            AppUsage("com.google.android.youtube", "YouTube", 72),
            AppUsage("com.kakao.talk", "카카오톡", 41),
            AppUsage("com.instagram.android", "Instagram", 28),
            )
        },
    )
}

@Singleton
class DemoReportRepository @Inject constructor(
    private val goalDao: GoalDao,
) : ReportRepository {
    override suspend fun getDailyReport(date: String, isToday: Boolean): Result<DailyReport> = Result.success(
        DailyReport(
            date = date,
            appUsages = goalDao.getAppGoals().mapIndexed { index, goal ->
                ReportAppUsage(
                    monitoredAppId = "demo-${index + 1}",
                    appName = goal.appLabel,
                    packageName = goal.packageName,
                    usedMinutes = listOf(41, 28, 72, 19, 34)[index % 5],
                    entryCount = listOf(5, 8, 4, 3, 6)[index % 5],
                    targetMinutes = goal.goalMinutes,
                    targetCount = index + 4,
                )
            }.ifEmpty {
                listOf(
                    ReportAppUsage("youtube", "YouTube", "com.google.android.youtube", 72, 5, 90, 6),
                    ReportAppUsage("kakao", "카카오톡", "com.kakao.talk", 41, 8, 60, 10),
                    ReportAppUsage("instagram", "Instagram", "com.instagram.android", 28, 4, 45, 5),
                )
            },
        ),
    )

    override suspend fun getReportSummary(range: ReportRange, date: String?): Result<ReportSummary> {
        val end = date?.let(LocalDate::parse) ?: LocalDate.now()
        val from = if (range == ReportRange.WEEK) end.minusDays(6) else end.withDayOfMonth(1)
        return Result.success(
            ReportSummary(
                range = range,
                from = from.toString(),
                to = end.toString(),
                keywords = listOf(ReasonKeyword("습관적으로", 7), ReasonKeyword("연락", 5), ReasonKeyword("휴식", 3)),
                summary = "짧게 확인하려던 사용이 길어지는 경우가 많았어요. 자주 여는 앱부터 쉬는 시간을 정해 보세요.",
            ),
        )
    }

    override suspend fun getRestSuggestion(date: String?): Result<RestSuggestion> = Result.success(
        RestSuggestion(
            date = date ?: LocalDate.now().toString(),
            message = "오후 사용량이 가장 높아요. 저녁에는 알림을 잠시 끄고 20분 산책을 해보는 건 어떨까요?",
        ),
    )
}

@Singleton
class DemoMyPageRepository @Inject constructor(
    private val store: DemoScenarioStore,
) : MyPageRepository {
    override suspend fun getMyInfo(): Result<User> = Result.success(store.user())
    override suspend fun updateMyInfo(name: String?, motivation: String?): Result<User> =
        Result.success(store.updateUser(name, motivation))
    override suspend fun withdraw(): Result<WithdrawalResult> = Result.success(
        WithdrawalResult(
            status = UserStatus.WITHDRAWAL_PENDING,
            recoverableUntil = LocalDate.now().plusDays(14).toString(),
        ),
    )
}

@Singleton
class DemoReportUsageReasonRepository @Inject constructor(
    private val store: DemoScenarioStore,
) : ReportUsageReasonRepository {
    override suspend fun submitUsageReason(entry: UsageReasonEntry): Result<Unit> {
        store.addReasonDate(entry.date)
        return Result.success(Unit)
    }

    override suspend fun getReasonCalendar(month: String): Result<List<ReasonCalendarDay>> = Result.success(
        store.reasonDates(month).sorted().map { ReasonCalendarDay(date = it, hasReason = true) },
    )
}
