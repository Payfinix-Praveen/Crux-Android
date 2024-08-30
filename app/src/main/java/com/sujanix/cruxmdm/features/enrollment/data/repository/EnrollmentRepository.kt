package com.sujanix.cruxmdm.features.enrollment.data.repository

import android.content.Context
import com.sujanix.cruxmdm.features.app_catalog.data.data_source.local.ApplicationDao
import com.sujanix.cruxmdm.features.core.data.data_source.local.dao.LocationDataDao
import com.sujanix.cruxmdm.features.core.data.data_source.remote.CruxApi
import com.sujanix.cruxmdm.features.core.data.data_source.remote.LocationApi
import com.sujanix.cruxmdm.features.core.data.model.OrganizationData
import com.sujanix.cruxmdm.features.core.data.repository.CruxRepository
import com.sujanix.cruxmdm.features.core.utlis.UserPreferences
import com.sujanix.cruxmdm.features.core.utlis.battery.BatteryDataHelper
import com.sujanix.cruxmdm.features.enrollment.data.model.request.EnrollmentData
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class EnrollmentRepository@Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: CruxApi,
    private val locationApi: LocationApi,
    private val appDao: ApplicationDao,
    private val locationDao: LocationDataDao,
    private val userPreference: UserPreferences,
    private val batteryDataHelper: BatteryDataHelper
): CruxRepository(context, api, locationApi, appDao, locationDao, userPreference, batteryDataHelper) {

    fun getEnrollmentDevice() = userPreference.getDeviceEnrollment

    suspend fun generateQrCode(enrollmentData: EnrollmentData) = safeApiCall{
        api.generateQrCode(enrollmentData)
    }

    suspend fun getEnterpriseGroupList(enterpriseId: OrganizationData) = safeApiCall {
        api.getEnterpriseGroupList(enterpriseId)
    }
}