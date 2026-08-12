package com.phoneshim.android.data.api.common

sealed class ApiException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {

    val code: String?
        get() = when (this) {
            is Http -> error?.code
            is Server -> error.code
            else -> null
        }

    val httpStatus: Int?
        get() = (this as? Http)?.statusCode

    val isUnauthorized: Boolean
        get() = httpStatus == HTTP_UNAUTHORIZED

    val isInsufficientData: Boolean
        get() = httpStatus == HTTP_UNPROCESSABLE_ENTITY &&
            code?.startsWith("INSUFFICIENT_") == true

    class Server(
        val error: ApiError,
    ) : ApiException(error.message)

    class Http(
        val statusCode: Int,
        val error: ApiError?,
        cause: Throwable,
    ) : ApiException(
        message = error?.message ?: "HTTP request failed with status $statusCode.",
        cause = cause,
    )

    class Network(
        cause: Throwable,
    ) : ApiException("Unable to connect to the server.", cause)

    class Serialization(
        cause: Throwable,
    ) : ApiException("Unable to parse the server response.", cause)

    class InvalidResponse(
        message: String,
    ) : ApiException(message)

    class Unexpected(
        cause: Throwable,
    ) : ApiException(cause.message ?: "An unexpected error occurred.", cause)

    private companion object {
        const val HTTP_UNAUTHORIZED = 401
        const val HTTP_UNPROCESSABLE_ENTITY = 422
    }
}

object ApiErrorCodes {
    const val ACCESS_TOKEN_REQUIRED = "ACCESS_TOKEN_REQUIRED"
    const val ID_TOKEN_REQUIRED = "ID_TOKEN_REQUIRED"
    const val EMAIL_PERMISSION_REQUIRED = "EMAIL_PERMISSION_REQUIRED"
    const val EMAIL_NOT_VERIFIED = "EMAIL_NOT_VERIFIED"
    const val INVALID_GOOGLE_ID_TOKEN = "INVALID_GOOGLE_ID_TOKEN"
    const val INSUFFICIENT_PREFIX = "INSUFFICIENT_"
    const val UNAUTHORIZED = "UNAUTHORIZED"
    const val INVALID_TOKEN = "INVALID_TOKEN"
    const val VALIDATION_ERROR = "VALIDATION_ERROR"
    const val USER_NOT_FOUND = "USER_NOT_FOUND"
    const val WITHDRAWAL_EXPIRED = "WITHDRAWAL_EXPIRED"
    const val USAGE_REASON_TIME_FORBIDDEN = "USAGE_REASON_TIME_FORBIDDEN"
    const val MONITORED_APP_NOT_FOUND = "MONITORED_APP_NOT_FOUND"
    const val INVALID_REPORT_RANGE = "INVALID_REPORT_RANGE"
    const val INSUFFICIENT_REPORT_DATA = "INSUFFICIENT_REPORT_DATA"
    const val INSUFFICIENT_AI_FEEDBACK_DATA = "INSUFFICIENT_AI_FEEDBACK_DATA"
}
