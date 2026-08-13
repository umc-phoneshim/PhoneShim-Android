package com.phoneshim.android.data.repository

import com.phoneshim.android.data.api.AlertSettingApi
import com.phoneshim.android.data.api.UpdateAlertSettingRequest
import com.phoneshim.android.data.api.common.ApiCallExecutor
import com.phoneshim.android.data.api.common.ApiException
import com.phoneshim.android.data.mapper.AlertSettingMappingException
import com.phoneshim.android.data.mapper.toDomain
import com.phoneshim.android.domain.model.AlertSetting
import com.phoneshim.android.domain.repository.AlertSettingRepository
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

class AlertSettingRepositoryImpl @Inject constructor(
    private val api: AlertSettingApi,
    private val apiCallExecutor: ApiCallExecutor,
) : AlertSettingRepository {

    override suspend fun getAlertSetting(): Result<AlertSetting> = resultOf {
        apiCallExecutor.execute { api.getAlertSetting() }.toDomain()
    }

    override suspend fun updateAlertSetting(alertTimeMinutes: Int): Result<AlertSetting> = resultOf {
        apiCallExecutor.execute {
            api.updateAlertSetting(UpdateAlertSettingRequest(alertTimeMinutes))
        }.toDomain()
    }

    private suspend fun <T> resultOf(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (error: CancellationException) {
        throw error
    } catch (error: AlertSettingMappingException) {
        Result.failure(ApiException.Serialization(error))
    } catch (error: Throwable) {
        Result.failure(error)
    }
}
