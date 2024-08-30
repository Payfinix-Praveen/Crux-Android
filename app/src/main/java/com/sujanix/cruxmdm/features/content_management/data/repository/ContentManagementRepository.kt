package com.sujanix.cruxmdm.features.content_management.data.repository

import android.content.Context
import com.sujanix.cruxmdm.features.app_catalog.data.data_source.local.ApplicationDao
import com.sujanix.cruxmdm.features.content_management.data.data_source.local.ContentManagementDao
import com.sujanix.cruxmdm.features.content_management.data.data_source.local.FileData
import com.sujanix.cruxmdm.features.content_management.data.data_source.local.toFileData
import com.sujanix.cruxmdm.features.content_management.data.data_source.local.toFileDataEntity
import com.sujanix.cruxmdm.features.core.data.data_source.local.dao.LocationDataDao
import com.sujanix.cruxmdm.features.core.data.data_source.remote.CruxApi
import com.sujanix.cruxmdm.features.core.data.data_source.remote.LocationApi
import com.sujanix.cruxmdm.features.core.data.model.OrganizationData
import com.sujanix.cruxmdm.features.core.data.repository.CruxRepository
import com.sujanix.cruxmdm.features.core.utlis.UserPreferences
import com.sujanix.cruxmdm.features.core.utlis.battery.BatteryDataHelper
import com.sujanix.cruxmdm.features.content_management.data.model.request.DownloadSharedFileRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ContentManagementRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: CruxApi,
    private val locationApi: LocationApi,
    private val appDao: ApplicationDao,
    private val contentDao: ContentManagementDao,
    private val locationDao: LocationDataDao,
    private val userPreference: UserPreferences,
    private val batteryDataHelper: BatteryDataHelper
): CruxRepository(context, api, locationApi, appDao, locationDao, userPreference, batteryDataHelper){

    suspend fun getSharedFileJson(organizationData: OrganizationData) = safeApiCall {
        api.getSharedFileJson(organizationData)
    }

    suspend fun getSharedFileUrl(fileKey: String) = safeApiCall {
        val filePath = DownloadSharedFileRequest(fileKey)
        api.getSharedFileUrl(filePath)
    }

    suspend fun insertFile(fileData: FileData){
        contentDao.insertFileData(fileData.toFileDataEntity())
    }

    suspend fun getAllFile() = contentDao.getAllFileData().map { it.toFileData() }

    suspend fun deleteAllFile() {
        contentDao.deleteAllFileData()
    }

    suspend fun getFileByKey(key: String) = contentDao.getFileByKey(key)?.toFileData()

    suspend fun deleteFileByKey(key: String){
        contentDao.deleteFileByKey(key)
    }
}