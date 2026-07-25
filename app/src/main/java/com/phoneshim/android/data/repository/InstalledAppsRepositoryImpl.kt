package com.phoneshim.android.data.repository

import android.content.Context
import android.content.Intent
import com.phoneshim.android.domain.model.InstalledApp
import com.phoneshim.android.domain.repository.InstalledAppsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class InstalledAppsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : InstalledAppsRepository {

    // 런처(MAIN/LAUNCHER) 액티비티를 가진 앱만 조회합니다.
    // manifest의 <queries> 선언으로 QUERY_ALL_PACKAGES 없이도 목록을 볼 수 있습니다.
    override suspend fun getInstalledApps(): List<InstalledApp> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        pm.queryIntentActivities(intent, 0)
            .asSequence()
            .map { it.activityInfo }
            .filter { it.packageName != context.packageName } // 폰쉼 자기 자신 제외
            .distinctBy { it.packageName }
            .map { info ->
                InstalledApp(
                    packageName = info.packageName,
                    label = info.loadLabel(pm).toString(),
                )
            }
            .sortedBy { it.label.lowercase() }
            .toList()
    }
}
