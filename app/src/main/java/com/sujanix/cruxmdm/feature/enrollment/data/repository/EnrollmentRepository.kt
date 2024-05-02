package com.sujanix.cruxmdm.feature.enrollment.data.repository

import android.content.Context
import com.sujanix.cruxmdm.feature.app_catalog.data.data_source.local.ApplicationDao
import com.sujanix.cruxmdm.feature.common.data.data_source.remote.CruxApi
import com.sujanix.cruxmdm.feature.common.data.repository.BaseRepository
import com.sujanix.cruxmdm.feature.common.utlis.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class EnrollmentRepository@Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: CruxApi,
    private val appDao: ApplicationDao,
    private val userPreference: UserPreferences
): BaseRepository() {

    fun getEnrollmentDevice() = userPreference.getDeviceEnrollment

    suspend fun setDeviceEnrolled() {
        userPreference.setDeviceEnrolled()
    }
}