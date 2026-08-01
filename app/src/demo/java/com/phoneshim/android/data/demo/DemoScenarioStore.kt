package com.phoneshim.android.data.demo

import android.content.Context
import com.phoneshim.android.domain.model.User
import com.phoneshim.android.domain.model.UserStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DemoScenarioStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun user(): User = User(
        id = "demo-user",
        email = "demo@phoneshim.app",
        nickname = preferences.getString(KEY_NAME, DEFAULT_NAME).orEmpty(),
        motivation = preferences.getString(KEY_MOTIVATION, DEFAULT_MOTIVATION),
        status = UserStatus.ACTIVE,
    )

    fun updateUser(name: String?, motivation: String?): User {
        preferences.edit().apply {
            name?.let { putString(KEY_NAME, it) }
            motivation?.let { putString(KEY_MOTIVATION, it) }
        }.apply()
        return user()
    }

    fun addReasonDate(date: String) {
        val dates = preferences.getStringSet(KEY_REASON_DATES, emptySet()).orEmpty().toMutableSet()
        dates += date
        preferences.edit().putStringSet(KEY_REASON_DATES, dates).apply()
    }

    fun reasonDates(month: String): Set<String> {
        val saved = preferences.getStringSet(KEY_REASON_DATES, emptySet()).orEmpty()
        val seeded = setOf(LocalDate.now().minusDays(1).toString(), LocalDate.now().minusDays(3).toString())
        return (saved + seeded).filterTo(mutableSetOf()) { it.startsWith(month) }
    }

    fun reset() = preferences.edit().clear().apply()

    private companion object {
        const val PREFERENCES_NAME = "phoneshim_demo_scenario"
        const val KEY_NAME = "name"
        const val KEY_MOTIVATION = "motivation"
        const val KEY_REASON_DATES = "reason_dates"
        const val DEFAULT_NAME = "유리"
        const val DEFAULT_MOTIVATION = "오늘도 폰을 내려놓고 나에게 집중하기"
    }
}
