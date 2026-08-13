package com.phoneshim.android.data.mapper

import com.phoneshim.android.data.api.AlertSettingResponse
import com.phoneshim.android.domain.model.AlertSetting
import java.time.Instant

fun AlertSettingResponse.toDomain(): AlertSetting = try {
    AlertSetting(
        id = id,
        userId = userId,
        enabled = enabled,
        alertTimeMinutes = alertTimeMinutes,
        createdAt = Instant.parse(createdAt),
        updatedAt = Instant.parse(updatedAt),
    )
} catch (error: Exception) {
    throw AlertSettingMappingException(error)
}

class AlertSettingMappingException(cause: Throwable) : IllegalArgumentException(cause)
