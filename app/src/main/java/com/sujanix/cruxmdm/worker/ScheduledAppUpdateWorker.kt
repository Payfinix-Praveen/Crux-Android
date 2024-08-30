package com.sujanix.cruxmdm.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.sujanix.cruxmdm.features.app_catalog.data.model.Application.Companion.toApplication
import com.sujanix.cruxmdm.features.app_catalog.data.model.Application.Companion.toApplicationEntity
import com.sujanix.cruxmdm.features.app_catalog.data.repository.AppCatalogRepository
import com.sujanix.cruxmdm.features.core.utlis.Constant
import com.sujanix.cruxmdm.features.app_catalog.utlis.InstallUtils
import com.sujanix.cruxmdm.features.core.utlis.Resource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@HiltWorker
class ScheduledAppUpdateWorker @AssistedInject constructor(
    private val repository: AppCatalogRepository,
    @Assisted
    private val context: Context,
    @Assisted
    params: WorkerParameters
) : Worker(context, params) {

//    @Inject
//    lateinit var repository: CruxRepository

    override fun doWork(): Result {
        return try {
            CoroutineScope(Dispatchers.IO).launch {
//            val deviceId = repository.getDeviceId().first()
//            val organizationData = repository.getOrganisationData().first()
//
//            if (deviceId != null && organizationData != null) {
//               val response = repository.getSelfHostedApplicationList(organizationData)
//
//            }

                repository.getUserData().collect {

                    if (it != null) {
                        Log.d("FATAL", "doWork: Called")
                        val response = repository.getSelfHostedApplicationList(
                            it.enterprise_id,
                            "389a89c6ab656ef0"
                        )

                        when (response) {
                            is Resource.Success -> {
                                Log.d("FATAL", "Resource.Success: ${response.value}")

                                if (response.value.STATUS && response.value.data.isNotEmpty()) {
                                    val appList = repository.getAllApplication()
                                    response.value.data.filter { data ->
                                        !InstallUtils.isInList(
                                            appList,
                                            data.toApplication().toApplicationEntity()
                                        )
                                    }.forEach { application ->
                                        val app = application.toApplication()
                                        val isAppPresent = appList.any { it.pkg == application.bundle_identifier }

                                        if( isAppPresent ) repository.updateApplication(app, application.bundle_identifier!!, true)
                                        else repository.insertApplication(app)
                                        InstallUtils.downloadAndInstallApk(
                                            context,
                                            app
                                        ) { filePath ->
                                            if (filePath.endsWith(".apk", true)) {
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    repository.apkDownloaded(
                                                        filePath,
                                                        application.bundle_identifier!!,
                                                        true
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Resource.Loading -> {}

                            else -> {
                                Result.failure()
                            }
                        }
                    }
                }
            }
            Result.success()
        } catch (e: Exception) {
            Log.d("FATAL", "doWork: ${e.message}")
            Result.retry()
        }
    }

    companion object {
        // Minimal interval is 15 minutes as per docs
        private const val FIRE_PERIOD_MINS = 20L

        private const val WORK_TAG_SCHEDULED_UPDATES =
            "com.sujanix.cruxmdm.WORK_TAG_SCHEDULED_UPDATES"

        fun schedule(context: Context) {
            Log.d(
                "FATAL",
                "Scheduled app updates worker runs each $FIRE_PERIOD_MINS mins"
            )
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val queryRequest: PeriodicWorkRequest =
                PeriodicWorkRequest.Builder(ScheduledAppUpdateWorker::class.java, FIRE_PERIOD_MINS, TimeUnit.MINUTES)
                    .addTag(Constant.WORK_TAG_COMMON)
                    .setConstraints(constraints)
                    .build()
            WorkManager.getInstance(context.applicationContext)
                .enqueueUniquePeriodicWork(
                    WORK_TAG_SCHEDULED_UPDATES,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    queryRequest
            )
        }
    }
}