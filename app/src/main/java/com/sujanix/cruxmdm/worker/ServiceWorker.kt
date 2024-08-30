package com.sujanix.cruxmdm.worker

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.sujanix.cruxmdm.features.core.utlis.Constant
import com.sujanix.cruxmdm.service.LocationService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

class ServiceWorker @AssistedInject constructor(
    @Assisted
    private val context: Context,
    @Assisted
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val intent = Intent(applicationContext, LocationService::class.java)
        context.stopService(intent)
        ContextCompat.startForegroundService(applicationContext, intent)
        return Result.success()
    }

    companion object {

        fun startLocationService(context: Context) {
            val downloadWorkRequest = OneTimeWorkRequestBuilder<ServiceWorker>()
                .addTag(Constant.WORK_TAG_COMMON)
                .build()

            WorkManager.getInstance(context.applicationContext)
                .enqueue(downloadWorkRequest)
        }
    }
}