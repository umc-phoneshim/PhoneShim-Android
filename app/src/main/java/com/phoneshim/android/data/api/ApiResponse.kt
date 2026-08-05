package com.phoneshim.android.data.api

import com.google.gson.Gson
import retrofit2.HttpException

/**
 * 서버 공통 응답 포맷. 모든 API 응답은 이 형태로 감싸여 옵니다.
 *
 * 성공: { "success": true, "data": {...} }
 * 실패: { "success": false, "error": { "code": "...", "message": "..." } }
 *
 * 사용 예)
 * ```
 * @GET("api/reminders")
 * suspend fun getReminders(@Query("date") date: String): ApiResponse<List<ReminderDto>>
 *
 * // Repository 에서
 * override suspend fun getReminders(date: String): Result<List<Reminder>> = runCatchingApi {
 *     reminderApi.getReminders(date).unwrap().map { it.toDomain() }
 * }
 * ```
 */
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ApiErrorBody? = null,
)

data class ApiErrorBody(
    val code: String,
    val message: String,
)

/** 서버가 내려준 에러 코드를 그대로 들고 다니는 예외. */
class ApiException(
    val code: String,
    val httpStatus: Int,
    override val message: String,
) : Exception(message) {

    /**
     * 422 INSUFFICIENT_*_DATA 계열인지 여부.
     * 이 경우는 "오류"가 아니라 "집계할 데이터가 아직 부족한 상태"로 화면에 표시해야 합니다.
     */
    val isInsufficientData: Boolean
        get() = httpStatus == 422 && code.startsWith("INSUFFICIENT_")

    /** 인증 만료/누락. 로그인 화면으로 보내야 하는 경우. */
    val isUnauthorized: Boolean
        get() = httpStatus == 401
}

object ApiErrorCodes {
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
    const val UNKNOWN = "UNKNOWN"
}

/** 껍데기를 벗겨 data 를 꺼냅니다. success 가 false 이거나 data 가 없으면 [ApiException]. */
fun <T> ApiResponse<T>.unwrap(): T {
    if (!success || data == null) {
        val body = error
        throw ApiException(
            code = body?.code ?: ApiErrorCodes.UNKNOWN,
            httpStatus = 200,
            message = body?.message ?: "알 수 없는 오류가 발생했습니다.",
        )
    }
    return data
}

/**
 * Repository 에서 API 호출을 감쌀 때 사용합니다.
 * HTTP 에러 응답의 body 를 파싱해 서버 에러 코드를 [ApiException] 으로 살려 보냅니다.
 * runCatching 대신 이걸 써야 에러 코드가 보존됩니다.
 */
inline fun <T> runCatchingApi(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (e: HttpException) {
    Result.failure(e.toApiException())
} catch (e: ApiException) {
    Result.failure(e)
} catch (e: Exception) {
    Result.failure(e)
}

fun HttpException.toApiException(): ApiException {
    val raw = runCatching { response()?.errorBody()?.string() }.getOrNull()
    val parsed = raw?.let {
        runCatching { Gson().fromJson(it, ApiResponse::class.java).error }.getOrNull()
    }
    return ApiException(
        code = parsed?.code ?: ApiErrorCodes.UNKNOWN,
        httpStatus = code(),
        message = parsed?.message ?: (message() ?: "네트워크 오류가 발생했습니다."),
    )
}
