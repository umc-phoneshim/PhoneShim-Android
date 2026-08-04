package com.phoneshim.android.blocking.upload

import android.content.Context
import com.phoneshim.android.blocking.detection.AppUsageSnapshot
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 아직 서버에 못 올린 날짜별 마지막 스냅샷.
 *
 * 서버 upsert 가 누적값 덮어쓰기라, 하루 중의 업로드 실패는 다음 주기 업로드가 흡수한다.
 * 유실되는 건 그 날짜의 마지막 전송뿐이므로 날짜당 1건만 들고 있다가
 * 네트워크가 돌아오면 date 를 명시해 다시 보낸다. (이슈 #45)
 *
 * 덮어쓰기 특성상 나중에 도착한 요청이 이기므로, 보존 대상은 그 날짜의 최신 1건으로 한정한다.
 * 더 낮은 값이 최신 값을 되돌리는 경로를 두지 않기 위해서다.
 */
@Singleton
class PendingUsageUploadStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun save(payload: UsageUploadPayload) {
        val dates = (dates() + payload.date).sorted().takeLast(MAX_KEPT_DAYS).toSet()
        prefs.edit()
            .putStringSet(KEY_DATES, dates)
            .putString(keyOf(payload.date), encode(payload))
            .apply()
    }

    fun remove(date: String) {
        prefs.edit()
            .putStringSet(KEY_DATES, (dates() - date).toSet())
            .remove(keyOf(date))
            .apply()
    }

    /** 보관 중인 스냅샷 전체. [exceptDate] 는 지금 주기가 어차피 다시 보내므로 건너뛴다. */
    fun pending(exceptDate: String?): List<UsageUploadPayload> =
        dates()
            .filter { it != exceptDate }
            .sorted()
            .mapNotNull { date -> prefs.getString(keyOf(date), null)?.let { decode(date, it) } }

    private fun dates(): Set<String> = prefs.getStringSet(KEY_DATES, emptySet()).orEmpty().toSet()

    private fun keyOf(date: String) = "$KEY_PAYLOAD_PREFIX$date"

    // "전체분|패키지:분:횟수;패키지:분:횟수"
    private fun encode(p: UsageUploadPayload): String {
        val apps = p.apps.joinToString(";") { "${it.packageName}:${it.usedMinutes}:${it.entryCount}" }
        return "${p.phoneMinutes}|$apps"
    }

    private fun decode(date: String, raw: String): UsageUploadPayload? {
        val head = raw.substringBefore('|')
        val phone = head.toIntOrNull() ?: return null
        val apps = raw.substringAfter('|', "")
            .split(';')
            .filter { it.isNotBlank() }
            .mapNotNull { entry ->
                val parts = entry.split(':')
                if (parts.size != 3) return@mapNotNull null
                val minutes = parts[1].toIntOrNull() ?: return@mapNotNull null
                val count = parts[2].toIntOrNull() ?: return@mapNotNull null
                AppUsageSnapshot(parts[0], minutes, count)
            }
        return UsageUploadPayload(date = date, phoneMinutes = phone, apps = apps)
    }

    private companion object {
        const val PREFS_NAME = "usage_upload"
        const val KEY_DATES = "pending_dates"
        const val KEY_PAYLOAD_PREFIX = "pending_"

        /** 오래 오프라인이어도 무한정 쌓지 않는다. 일주일이면 충분. */
        const val MAX_KEPT_DAYS = 7
    }
}