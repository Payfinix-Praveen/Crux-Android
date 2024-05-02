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
import com.sujanix.cruxmdm.feature.app_catalog.data.model.Application.Companion.toApplication
import com.sujanix.cruxmdm.feature.app_catalog.data.model.Application.Companion.toApplicationEntity
import com.sujanix.cruxmdm.feature.app_catalog.data.repository.AppCatalogRepository
import com.sujanix.cruxmdm.feature.common.data.model.OrganizationData
import com.sujanix.cruxmdm.feature.common.data.repository.CruxRepository
import com.sujanix.cruxmdm.feature.common.utlis.Constant
import com.sujanix.cruxmdm.feature.app_catalog.utlis.InstallUtils
import com.sujanix.cruxmdm.feature.common.utlis.Resource
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
                Log.d("FATAL", "doWork: Called")
                val response = repository.getApplicationList(
                    OrganizationData(
                        enterprise_id = "enterprises/LC01d3znq7",
                        device_id = "392e658a30aced6a"
                    )
                )

                when (response) {
                    is Resource.Success -> {
                        Log.d("FATAL", "Resource.Success: ${response.value}")
                        response.value.filter {
                            !InstallUtils.isInList(
                                repository.getAllApplication(),
                                it.toApplication()?.toApplicationEntity()!!
                            )
                        }.forEach {
                            repository.insertApplication(it.toApplication()!!)
                            InstallUtils.downloadAndInstallApk(context, it.toApplication()!!){ filePath ->
                                if(filePath.endsWith(".apk", true)) {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        repository.apkDownloaded(
                                            filePath,
                                            it.bundle_identifier,
                                            true
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Resource.Loading -> {}

                    else -> {
                        Result.retry()
                    }
                }
            }
            Result.success()
        } catch (e: Exception) {
            Log.d("FATAL", "doWork: ${e.message}")
            Result.failure()
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
                    .setInitialDelay(5, TimeUnit.MINUTES)
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