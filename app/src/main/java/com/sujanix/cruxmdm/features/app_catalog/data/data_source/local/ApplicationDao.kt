package com.sujanix.cruxmdm.features.app_catalog.data.data_source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sujanix.cruxmdm.features.app_catalog.data.data_source.local.entity.ApplicationEntity
import com.sujanix.cruxmdm.features.app_catalog.data.data_source.local.entity.RequestedAppEntity
import com.sujanix.cruxmdm.features.core.utlis.Constant

@Dao
interface ApplicationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplication(application: ApplicationEntity)

    @Query("SELECT * FROM application_table")
    suspend fun getAllApplications(): List<ApplicationEntity>

    @Query("SELECT * FROM application_table WHERE type = :type")
    suspend fun getSelfHostedApplications(type: String = Constant.SELF_HOSTED_APP): List<ApplicationEntity>

    @Query("DELETE FROM application_table")
    suspend fun deleteAllApplications()

    @Query("UPDATE application_table SET apkPath = :filePath, isDownloaded = :isDownloaded WHERE pkg = :packageName")
    suspend fun setApplicationDownloaded(filePath: String, packageName: String, isDownloaded: Boolean)

    @Query("UPDATE application_table SET isInstalled = :installed WHERE pkg = :packageName")
    suspend fun setApplicationInstalled(packageName: String, installed: Boolean)

    @Query("UPDATE application_table SET isUpdateAvailable = :isUpdateAvailable WHERE pkg = :packageName")
    suspend fun setApplicationUpdateAvailable(packageName: String, isUpdateAvailable: Boolean)

//    @Update
//    suspend fun updateApplication(application: ApplicationEntity)

    @Query("""
        UPDATE application_table SET type = :type, name = :name, url = :url,
        version = :version, versionCode = :versionCode, iconUrl = :iconUrl, 
        isDownloaded = :isDownloaded, isUpdateAvailable = :isUpdateAvailable WHERE pkg = :packageName
    """)
    suspend fun updateApplication(packageName: String, type: String, name: String, url: String?,
                                  version: String, versionCode: Int?, iconUrl: String,
                                  isDownloaded: Boolean = false, isUpdateAvailable: Boolean = false
    )

    /** ======================== REQUESTED APPS ======================== **/

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequestedApp(requestedApp: RequestedAppEntity)

    @Query("SELECT * FROM requested_app_table")
    suspend fun getAllRequestedApps(): List<RequestedAppEntity>

    @Query("DELETE FROM requested_app_table")
    suspend fun deleteAllRequestedApps()

    @Query("DELETE FROM requested_app_table WHERE packageName = :packageName")
    suspend fun deleteRequestedApp(packageName: String)

    @Query("SELECT * FROM requested_app_table WHERE packageName = :packageName")
    suspend fun getRequestedAppByPackageName(packageName: String): RequestedAppEntity

}