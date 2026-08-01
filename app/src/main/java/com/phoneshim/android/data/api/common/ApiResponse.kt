package com.phoneshim.android.data.api.common

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ApiError? = null,
)

data class ApiError(
    val code: String,
    val message: String,
)
