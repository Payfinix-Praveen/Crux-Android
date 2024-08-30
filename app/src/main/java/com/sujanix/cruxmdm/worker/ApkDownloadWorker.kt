package com.sujanix.cruxmdm.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.sujanix.cruxmdm.features.app_catalog.data.model.Application
import com.sujanix.cruxmdm.features.app_catalog.data.repository.AppCatalogRepository
import com.sujanix.cruxmdm.features.app_catalog.utlis.InstallUtils
import com.sujanix.cruxmdm.features.core.utlis.Constant
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltWorker
class ApkDownloadWorker @AssistedInject constructor(
    private val repository: AppCatalogRepository,
    @Assisted
    private val context: Context,
    @Assisted
    params: WorkerParameters
) : Worker(context, params) {

    private val TAG = "ApkDownloadWorker"


    override fun doWork(): Result {
        try {
//            val appString = inputData.getString("app") ?: return Result.failure()
            val type = inputData.getString("type")
            val name = inputData.getString("name")
            val pkg = inputData.getString("pkg")
            val url = inputData.getString("url")
            val version = inputData.getString("version")
            val versionCode = inputData.getInt("version_code", 0)
            val iconUrl = inputData.getString("app_logo_base64")

            val app = Application(
                type = type,
                name = name,
                pkg = pkg,
                url = url,
                version = version,
                versionCode = versionCode,
                iconUrl = iconUrl
            )
            Log.d(TAG, "doWork: $app")
            InstallUtils.downloadAndInstallApk(
                context,
                app
            ) { filePath ->
                if (filePath.endsWith(".apk", true)) {
                    CoroutineScope(Dispatchers.IO).launch {
                        repository.apkDownloaded(
                            filePath,
                            app.pkg!!,
                            true
                        )
                    }
                } else {
                    throw Exception("Failed to download apk")
                }
            }
            return Result.success()
        } catch (e: Exception) {
            Log.d(TAG, "doWork: $e")
            return Result.retry()
        }
    }

    companion object{

        fun downloadApk(context: Context, data: Data) {

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val downloadWorkRequest = OneTimeWorkRequestBuilder<ApkDownloadWorker>()
                .setConstraints(constraints)
                .setInputData(data)
                .addTag(Constant.WORK_TAG_COMMON)
                .build()

            WorkManager.getInstance(context.applicationContext)
                .enqueue(downloadWorkRequest)
        }
    }
}