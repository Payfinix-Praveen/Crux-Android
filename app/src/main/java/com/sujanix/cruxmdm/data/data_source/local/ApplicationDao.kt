package com.sujanix.cruxmdm.data.data_source.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sujanix.cruxmdm.data.data_source.local.entity.ApplicationEntity
import com.sujanix.cruxmdm.data.model.Application

@Dao
interface ApplicationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplication(application: ApplicationEntity)

    @Query("SELECT * FROM application_table")
    suspend fun getAllApplications(): List<ApplicationEntity>

    @Query("DELETE FROM application_table")
    suspend fun deleteAllApplications()

    @Query("UPDATE application_table SET isDownloaded = :downloaded AND apkPath = :filePath WHERE pkg = :packageName")
    suspend fun setApplicationDownloaded(filePath: String, packageName: String, downloaded: Boolean)

}