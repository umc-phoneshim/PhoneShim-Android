package com.phoneshim.android.ui.features.report.viewmodel

import com.phoneshim.android.domain.model.AlertSetting
import com.phoneshim.android.domain.model.DailyReport
import com.phoneshim.android.domain.model.ReportRange
import com.phoneshim.android.domain.model.ReportSummary
import com.phoneshim.android.domain.model.RestSuggestion
import com.phoneshim.android.domain.model.UsageSession
import com.phoneshim.android.domain.repository.AlertSettingRepository
import com.phoneshim.android.domain.repository.ReportPreferencesRepository
import com.phoneshim.android.domain.repository.ReportRepository
import com.phoneshim.android.domain.usecase.GetAlertSettingUseCase
import com.phoneshim.android.domain.usecase.GetDailyReportUseCase
import com.phoneshim.android.domain.usecase.GetReportSummaryUseCase
import com.phoneshim.android.domain.usecase.GetRestSuggestionUseCase
import com.phoneshim.android.domain.usecase.GetUsageSessionsUseCase
import com.phoneshim.android.domain.usecase.UpdateAlertSettingUseCase
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReportAlertSettingViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var alertRepository: FakeAlertSettingRepository
    private lateinit var viewModel: ReportViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        alertRepository = FakeAlertSettingRepository()
        val reportRepository = EmptyReportRepository()
        viewModel = ReportViewModel(
            getDailyReportUseCase = GetDailyReportUseCase(reportRepository),
            getUsageSessionsUseCase = GetUsageSessionsUseCase(reportRepository),
            getReportSummaryUseCase = GetReportSummaryUseCase(reportRepository),
            getRestSuggestionUseCase = GetRestSuggestionUseCase(reportRepository),
            reportPreferencesRepository = FakeReportPreferencesRepository(),
            getAlertSettingUseCase = GetAlertSettingUseCase(alertRepository),
            updateAlertSettingUseCase = UpdateAlertSettingUseCase(alertRepository),
        )
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `서버 AlertSetting을 읽어 팝업 초깃값으로 사용한다`() = runTest {
        viewModel.onEvent(ReportUiEvent.ScreenEntered(com.phoneshim.android.ui.features.report.component.ReportTab.SUMMARY))
        runCurrent()
        viewModel.onEvent(ReportUiEvent.AlarmSettingsClicked)

        val state = viewModel.uiState.value
        assertEquals("22", state.alarmHourDraft)
        assertEquals("00", state.alarmMinuteDraft)
        assertNotNull(state.alertSetting)
    }

    @Test
    fun `허용 범위 밖이면 팝업을 유지하고 PATCH하지 않는다`() = runTest {
        viewModel.onEvent(ReportUiEvent.AlarmSettingsClicked)
        viewModel.onEvent(ReportUiEvent.AlarmHourChanged("21"))
        viewModel.onEvent(ReportUiEvent.AlarmMinuteChanged("59"))

        viewModel.onEvent(ReportUiEvent.AlarmConfirmed)

        val state = viewModel.uiState.value
        assertTrue(state.isAlarmDialogVisible)
        assertNotNull(state.alarmInputError)
        assertTrue(alertRepository.updatedMinutes.isEmpty())
    }

    @Test
    fun `유효한 시간을 PATCH하고 성공 응답으로 상태를 갱신한다`() = runTest {
        viewModel.onEvent(ReportUiEvent.AlarmSettingsClicked)
        viewModel.onEvent(ReportUiEvent.AlarmHourChanged("23"))
        viewModel.onEvent(ReportUiEvent.AlarmMinuteChanged("30"))

        viewModel.onEvent(ReportUiEvent.AlarmConfirmed)
        runCurrent()

        val state = viewModel.uiState.value
        assertEquals(listOf(1410), alertRepository.updatedMinutes)
        assertEquals(1410, state.alertSetting?.alertTimeMinutes)
        assertFalse(state.isAlarmDialogVisible)
        assertFalse(state.isAlertSettingSaving)
    }

    private class FakeAlertSettingRepository : AlertSettingRepository {
        val updatedMinutes = mutableListOf<Int>()

        override suspend fun getAlertSetting(): Result<AlertSetting> = Result.success(setting(1320))

        override suspend fun updateAlertSetting(alertTimeMinutes: Int): Result<AlertSetting> {
            updatedMinutes += alertTimeMinutes
            return Result.success(setting(alertTimeMinutes))
        }

        private fun setting(minutes: Int) = AlertSetting(
            id = "alert-1",
            userId = "user-1",
            enabled = true,
            alertTimeMinutes = minutes,
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH,
        )
    }

    private class FakeReportPreferencesRepository : ReportPreferencesRepository {
        override suspend fun isCalendarTooltipDismissed(): Boolean = true
        override suspend fun dismissCalendarTooltip() = Unit
    }

    private class EmptyReportRepository : ReportRepository {
        override suspend fun getDailyReport(date: String, isToday: Boolean): Result<DailyReport> =
            Result.success(DailyReport(date, emptyList()))

        override suspend fun getUsageSessions(date: String): Result<List<UsageSession>> =
            Result.success(emptyList())

        override suspend fun uploadUsageSession(
            monitoredAppId: String,
            startTime: String,
            endTime: String,
        ): Result<Unit> = Result.success(Unit)

        override suspend fun getReportSummary(
            range: ReportRange,
            date: String?,
        ): Result<ReportSummary> = Result.failure(UnsupportedOperationException())

        override suspend fun getAchievedDates(month: String): Result<List<String>> =
            Result.success(emptyList())

        override suspend fun getRestSuggestion(date: String?): Result<RestSuggestion> =
            Result.failure(UnsupportedOperationException())
    }
}
