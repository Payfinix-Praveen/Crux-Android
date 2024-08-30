package com.sujanix.cruxmdm.features.app_catalog.data.repository

import android.content.Context
import android.util.Log
import com.sujanix.cruxmdm.features.app_catalog.data.data_source.local.ApplicationDao
import com.sujanix.cruxmdm.features.app_catalog.data.data_source.local.entity.RequestedApp
import com.sujanix.cruxmdm.features.app_catalog.data.data_source.local.entity.toRequestedApp
import com.sujanix.cruxmdm.features.app_catalog.data.data_source.local.entity.toRequestedAppEntity
import com.sujanix.cruxmdm.features.core.data.data_source.remote.CruxApi
import com.sujanix.cruxmdm.features.app_catalog.data.model.Application
import com.sujanix.cruxmdm.features.app_catalog.data.model.response.application_data.ApplicationDataApi
import com.sujanix.cruxmdm.features.core.data.model.OrganizationData
import com.sujanix.cruxmdm.features.app_catalog.data.model.response.selfHosted.SelfHostedApplication
import com.sujanix.cruxmdm.features.app_catalog.utlis.InstallUtils
import com.sujanix.cruxmdm.features.core.data.data_source.local.dao.LocationDataDao
import com.sujanix.cruxmdm.features.core.data.data_source.remote.LocationApi
import com.sujanix.cruxmdm.features.core.data.model.response.ApkSignedUrlResponse
import com.sujanix.cruxmdm.features.core.data.repository.CruxRepository
import com.sujanix.cruxmdm.features.core.utlis.Resource
import com.sujanix.cruxmdm.features.core.utlis.UserPreferences
import com.sujanix.cruxmdm.features.core.utlis.battery.BatteryDataHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AppCatalogRepository@Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: CruxApi,
    private val locationApi: LocationApi,
    private val appDao: ApplicationDao,
    private val locationDao: LocationDataDao,
    private val userPreference: UserPreferences,
    private val batteryDataHelper: BatteryDataHelper
): CruxRepository(context, api, locationApi, appDao, locationDao, userPreference, batteryDataHelper){

    val TAG = "AppCatalogRepository"

    suspend fun getApplicationList(organisationData: OrganizationData): Resource<List<ApplicationDataApi>> {
        return safeApiCall { api.getEnterpriseAppList(organisationData) }
    }

    suspend fun getSelfHostedApplicationList(enterpriseId: String, deviceId: String): Resource<SelfHostedApplication> {
        return safeApiCall {
            api.getSelfHostedApplications(
                enterpriseId = enterpriseId.makeRequestBody(),
                deviceId = deviceId.makeRequestBody()
            )
        }
    }

    suspend fun getApkSignedUrl(enterpriseId: String, packageName: String): Resource<ApkSignedUrlResponse> {
        return safeApiCall {
            api.getApkSignedUrl(
                enterpriseId = enterpriseId.makeRequestBody(),
                packageName = packageName.makeRequestBody()
            )
        }
    }

    suspend fun insertApplication(application: Application) {
        val applicationEntity =
            InstallUtils.checkApplicationDownloadAndInstallStatus(context, application)
        Log.d(TAG, "insertApplication: $applicationEntity")
        appDao.insertApplication(applicationEntity)
    }

    suspend fun updateApplication(application: Application, pkgName: String, isUpdateAvailable: Boolean) {
        appDao.updateApplication(
            packageName = pkgName,
            type = application.type!!,
            name = application.name!!,
            url = application.url,
            version = application.version!!,
            versionCode = application.versionCode,
            iconUrl = application.iconUrl!!,
            isUpdateAvailable = isUpdateAvailable
        )
    }

    suspend fun getAllApplication() = appDao.getAllApplications()

    suspend fun getSelfHostedApplications() = appDao.getSelfHostedApplications()

    suspend fun deleteAllApplication(){
        appDao.deleteAllApplications()
    }

    suspend fun apkDownloaded(filePath: String, packageName: String, downloaded: Boolean) {
        Log.d(TAG, "apkDownloaded: $downloaded, $packageName, $filePath")
        appDao.setApplicationDownloaded(filePath, packageName, downloaded)
    }

    suspend fun setInstalled(packageName: String, installed: Boolean) {
        appDao.setApplicationInstalled(packageName, installed)
    }

    suspend fun setApplicationUpdateAvailable(packageName: String, updated: Boolean) {
        appDao.setApplicationUpdateAvailable(packageName, updated)
    }

    suspend fun insertRequestApp(requestedApp: RequestedApp){
        appDao.insertRequestedApp(requestedApp.toRequestedAppEntity())
    }

    suspend fun getRequestAppList() = appDao.getAllRequestedApps().map { it.toRequestedApp() }

    suspend fun deleteAllRequestedApps() {
        appDao.deleteAllRequestedApps()
    }

    suspend fun deleteRequestedApp(packageName: String) {
        appDao.deleteRequestedApp(packageName)
    }

    suspend fun getRequestedAppByPackageName(packageName: String) = appDao.getRequestedAppByPackageName(packageName)

}