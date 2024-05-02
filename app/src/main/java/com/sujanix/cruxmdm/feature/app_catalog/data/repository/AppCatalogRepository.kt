package com.sujanix.cruxmdm.feature.app_catalog.data.repository

import android.content.Context
import android.util.Log
import com.sujanix.cruxmdm.feature.app_catalog.data.data_source.local.ApplicationDao
import com.sujanix.cruxmdm.feature.common.data.data_source.remote.CruxApi
import com.sujanix.cruxmdm.feature.app_catalog.data.model.Application
import com.sujanix.cruxmdm.feature.app_catalog.data.model.response.application_data.ApplicationDataApi
import com.sujanix.cruxmdm.feature.common.data.model.OrganizationData
import com.sujanix.cruxmdm.feature.app_catalog.data.model.response.selfHosted.SelfHostedApplication
import com.sujanix.cruxmdm.feature.common.data.repository.BaseRepository
import com.sujanix.cruxmdm.feature.app_catalog.utlis.InstallUtils
import com.sujanix.cruxmdm.feature.common.utlis.Resource
import com.sujanix.cruxmdm.feature.common.utlis.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AppCatalogRepository@Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: CruxApi,
    private val appDao: ApplicationDao,
    private val userPreference: UserPreferences
): BaseRepository() {

    suspend fun getApplicationList(organisationData: OrganizationData): Resource<List<ApplicationDataApi>> {
        return safeApiCall { api.getEnterpriseAppList(organisationData) }
    }

    suspend fun getSelfHostedApplicationList(organisationData: OrganizationData): Resource<SelfHostedApplication> {
        return safeApiCall {
            api.getSelfHostedApplications(
                enterpriseId = organisationData.enterprise_id!!.makeRequestBody(),
                deviceId = organisationData.device_id!!.makeRequestBody()
            )
        }
    }

    suspend fun insertApplication(application: Application) {
        val applicationEntity =
            InstallUtils.checkApplicationDownloadAndInstallStatus(context, application)
        Log.d("FATAL", "insertApplication: $applicationEntity")
        appDao.insertApplication(applicationEntity)
    }

    suspend fun getAllApplication() = appDao.getAllApplications()

    suspend fun deleteAllApplication(){
        appDao.deleteAllApplications()
    }

    suspend fun apkDownloaded(filePath: String, packageName: String, downloaded: Boolean) {
        appDao.setApplicationDownloaded(filePath, packageName, downloaded)
    }
}