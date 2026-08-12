package com.phoneshim.android.data.repository

import com.phoneshim.android.domain.model.InstalledApp
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 주의 앱 선택 목록 정렬.
 * 사용정보 접근 권한이 없으면 queryUsageStats 가 예외가 아니라 빈 결과를 주므로,
 * 빈 사용량이 곧 폴백 조건이다.
 */
class InstalledAppsSortTest {

    private val kakao = InstalledApp("com.kakao.talk", "카카오톡")
    private val youtube = InstalledApp("com.google.android.youtube", "YouTube")
    private val instagram = InstalledApp("com.instagram.android", "Instagram")
    private val apps = listOf(kakao, youtube, instagram)

    @Test
    fun `사용량이 많은 앱이 먼저 온다`() {
        val sorted = sortByUsage(
            apps,
            mapOf(
                "com.kakao.talk" to 1_000L,
                "com.google.android.youtube" to 9_000L,
                "com.instagram.android" to 5_000L,
            ),
        )

        assertEquals(listOf(youtube, instagram, kakao), sorted)
    }

    @Test
    fun `사용량을 못 읽으면 가나다순으로 돌아간다`() {
        val sorted = sortByUsage(apps, emptyMap())

        // label 기준 오름차순: Instagram, YouTube, 카카오톡
        assertEquals(listOf(instagram, youtube, kakao), sorted)
    }

    @Test
    fun `이력 없는 앱은 뒤로 가되 그 안에서는 가나다순을 지킨다`() {
        val sorted = sortByUsage(apps, mapOf("com.kakao.talk" to 1_000L))

        // 카카오톡만 이력이 있고, 나머지는 뒤에서 라벨순.
        assertEquals(listOf(kakao, instagram, youtube), sorted)
    }

    @Test
    fun `사용량이 같으면 가나다순으로 갈린다`() {
        val sorted = sortByUsage(
            apps,
            mapOf(
                "com.kakao.talk" to 5_000L,
                "com.google.android.youtube" to 5_000L,
                "com.instagram.android" to 5_000L,
            ),
        )

        assertEquals(listOf(instagram, youtube, kakao), sorted)
    }

    @Test
    fun `빈 목록도 그대로 처리한다`() {
        assertEquals(emptyList<InstalledApp>(), sortByUsage(emptyList(), emptyMap()))
    }
}
