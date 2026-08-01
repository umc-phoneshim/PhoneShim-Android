package com.phoneshim.android.data.api.common

sealed class ApiException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {

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
}
