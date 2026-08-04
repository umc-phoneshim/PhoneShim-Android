package com.phoneshim.android.data.api.common

import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.stream.MalformedJsonException
import java.io.EOFException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import retrofit2.Response

@Singleton
class ApiCallExecutor @Inject constructor(
    private val gson: Gson,
) {
    suspend fun <T : Any> execute(apiCall: suspend () -> ApiResponse<T>): T =
        try {
            apiCall().getRequiredData()
        } catch (error: CancellationException) {
            throw error
        } catch (error: ApiException) {
            throw error
        } catch (error: HttpException) {
            throw error.toApiException()
        } catch (error: JsonParseException) {
            throw ApiException.Serialization(error)
        } catch (error: MalformedJsonException) {
            throw ApiException.Serialization(error)
        } catch (error: EOFException) {
            throw ApiException.Serialization(error)
        } catch (error: IOException) {
            throw ApiException.Network(error)
        } catch (error: Throwable) {
            throw ApiException.Unexpected(error)
        }

    /** 204 No Content처럼 공통 응답 envelope가 없는 성공 응답을 처리한다. */
    suspend fun executeNoContent(apiCall: suspend () -> Response<Unit>) {
        try {
            val response = apiCall()
            if (!response.isSuccessful) throw HttpException(response)
        } catch (error: CancellationException) {
            throw error
        } catch (error: ApiException) {
            throw error
        } catch (error: HttpException) {
            throw error.toApiException()
        } catch (error: IOException) {
            throw ApiException.Network(error)
        } catch (error: Throwable) {
            throw ApiException.Unexpected(error)
        }
    }

    private fun <T : Any> ApiResponse<T>.getRequiredData(): T {
        if (success) {
            return data ?: throw ApiException.InvalidResponse(
                "Successful response did not contain data.",
            )
        }

        return error?.let { throw ApiException.Server(it) }
            ?: throw ApiException.InvalidResponse(
                "Failed response did not contain error details.",
            )
    }

    private fun HttpException.toApiException(): ApiException.Http {
        val apiError = response()
            ?.errorBody()
            ?.charStream()
            ?.use { reader ->
                runCatching {
                    gson.fromJson(reader, ErrorEnvelope::class.java).error
                }.getOrNull()
            }

        return ApiException.Http(
            statusCode = code(),
            error = apiError,
            cause = this,
        )
    }

    private data class ErrorEnvelope(
        val error: ApiError? = null,
    )
}
