package com.phoneshim.android.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.phoneshim.android.domain.model.DailyReportAlarm
import com.phoneshim.android.domain.repository.ReportPreferencesRepository
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ReportDataStore

/**
 * 데일리 리포트 화면의 기기 로컬 설정 저장소.
 *
 * 인증 토큰과 달리 민감한 값이 아니라 암호화 없이 그대로 둡니다.
 * 토큰용 DataStore 와 파일을 분리해서, 로그아웃 시 세션만 지워도 이 설정은 남게 합니다.
 */
@Singleton
class ReportPreferencesStore @Inject constructor(
    @ReportDataStore private val dataStore: DataStore<Preferences>,
) : ReportPreferencesRepository {

    override suspend fun isCalendarTooltipDismissed(): Boolean =
        dataStore.data.first()[CALENDAR_TOOLTIP_DISMISSED] ?: false

    override suspend fun dismissCalendarTooltip() {
        dataStore.edit { preferences -> preferences[CALENDAR_TOOLTIP_DISMISSED] = true }
    }

    /** 시/분이 둘 다 있어야 유효한 설정으로 봅니다. 하나만 있으면 저장이 깨진 것이라 무시합니다. */
    override suspend fun getDailyReportAlarm(): DailyReportAlarm? {
        val preferences = dataStore.data.first()
        val hour = preferences[ALARM_HOUR] ?: return null
        val minute = preferences[ALARM_MINUTE] ?: return null
        return DailyReportAlarm.of(hour, minute)
    }

    override suspend fun saveDailyReportAlarm(alarm: DailyReportAlarm) {
        dataStore.edit { preferences ->
            preferences[ALARM_HOUR] = alarm.hour
            preferences[ALARM_MINUTE] = alarm.minute
        }
    }

    private companion object {
        val CALENDAR_TOOLTIP_DISMISSED = booleanPreferencesKey("report_calendar_tooltip_dismissed")
        val ALARM_HOUR = intPreferencesKey("daily_report_alarm_hour")
        val ALARM_MINUTE = intPreferencesKey("daily_report_alarm_minute")
    }
}
