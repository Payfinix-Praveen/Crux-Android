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
import com.sujanix.cruxmdm.features.core.data.model.response.LocationDataResponse
import com.sujanix.cruxmdm.features.core.data.repository.CruxRepository
import com.sujanix.cruxmdm.features.core.utlis.Constant
import com.sujanix.cruxmdm.features.core.utlis.Resource
import com.sujanix.cruxmdm.features.core.utlis.location.LocationUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

@HiltWorker
class LocationManagementWorker @AssistedInject constructor(
    private val repository: CruxRepository,
    @Assisted
    private val context: Context,
    @Assisted
    params: WorkerParameters
) : Worker(context, params) {

    private val TAG: String = "LocationManagementWorker"

    override fun doWork(): Result {

        return try {
            CoroutineScope(Dispatchers.IO).launch {

                val locationHistory = repository.getLocationDataByType(Constant.LOCATION_HISTORY)
                val locationHistoryByDate = LocationUtils.groupLocationsByDate(locationHistory)
                val gpsBreadcrumbs = repository.getLocationDataByType(Constant.LOCATION_BREADCRUMBS)
                val gpsBreadcrumbsByDate = LocationUtils.groupLocationsByDate(gpsBreadcrumbs)

                repository.getUserData().collect{ deviceUserData ->
                    repository.getDeviceId().collect { deviceId ->
                        Log.d(TAG, "LocationManagementWorker: deviceUserData $deviceUserData")
                        if(deviceUserData != null) {
                            if(deviceId != null) {
                                if (locationHistoryByDate.isNotEmpty()) {
                                    LocationUtils.organizeAndSyncLocationData(
                                        locationHistoryByDate,
                                        deviceUserData,
                                        deviceId,
                                        Constant.LOCATION_HISTORY
                                    ){ locationData, trackingType ->
                                        CoroutineScope(Dispatchers.IO).launch {
                                            val response = repository.sendLocationData(
                                                locationData,
                                                trackingType
                                            )
                                            handleResponse(response)
                                        }
                                    }

                                    Log.d(TAG, "LocationManagementWorker: locationHistory list is not empty")
                                }
                                if (gpsBreadcrumbsByDate.isNotEmpty()) {
                                    LocationUtils.organizeAndSyncLocationData(
                                        gpsBreadcrumbsByDate,
                                        deviceUserData,
                                        deviceId,
                                        Constant.LOCATION_BREADCRUMBS
                                    ){ locationData, trackingType ->
                                        CoroutineScope(Dispatchers.IO).launch {
                                            val response = repository.sendLocationData(
                                                locationData,
                                                trackingType
                                            )
                                            handleResponse(response)
                                        }
                                    }
                                    Log.d(TAG, "LocationManagementWorker: locationBreadcrumb list is not empty")
                                }
                            }
                        }
                    }
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun handleResponse(response: Resource<LocationDataResponse>) {
        when (response) {
            is Resource.Success -> {
                if (!response.value.status) {
                    throw Exception("Failed to send location breadcrumbs")
                }
            }

            is Resource.Failure -> throw Exception("Failed to send location breadcrumbs")
            Resource.Loading -> TODO()
        }
    }

    companion object {
        // Minimal interval is 15 minutes as per docs
        private const val FIRE_PERIOD_MINS = 24L

        private const val WORK_TAG_SCHEDULED_UPDATES =
            "com.sujanix.cruxmdm.WORK_TAG_LOCATION_DATA_SYNC"

        fun schedule(context: Context) {
            Log.d(
                "FATAL",
                "Scheduled app updates worker runs each $FIRE_PERIOD_MINS mins LocationManagementWorker"
            )
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val queryRequest: PeriodicWorkRequest =
                PeriodicWorkRequest.Builder(LocationManagementWorker::class.java, FIRE_PERIOD_MINS, TimeUnit.HOURS)
                    .addTag(Constant.WORK_TAG_COMMON)
                    .setConstraints(constraints)
                    .setInitialDelay(getInitialDelayToNext12AM(), TimeUnit.MILLISECONDS)
                    .build()
            WorkManager.getInstance(context.applicationContext)
                .enqueueUniquePeriodicWork(
                    WORK_TAG_SCHEDULED_UPDATES,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    queryRequest
                )
        }

        private fun getInitialDelayToNext12AM(): Long {
            val currentDateTime = Calendar.getInstance()
            val twoAMDateTime = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return twoAMDateTime.timeInMillis - currentDateTime.timeInMillis
        }
    }
}